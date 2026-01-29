package com.utilitybill.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.Customer;
import com.utilitybill.util.AppLogger;

import com.utilitybill.exception.DocumentGenerationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class PdfService implements InvoiceGenerator {

    private static final String CLASS_NAME = PdfService.class.getName();
    private static volatile PdfService instance;
    private final CustomerService customerService;

    private PdfService() {
        this.customerService = CustomerService.getInstance();
    }

    public static PdfService getInstance() {
        if (instance == null) {
            synchronized (PdfService.class) {
                if (instance == null) {
                    instance = new PdfService();
                }
            }
        }
        return instance;
    }

    @Override
    public void generateInvoice(Invoice invoice, File destination) throws DocumentGenerationException {
        try {
            PdfWriter writer = new PdfWriter(destination);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Get customer details
            Customer customer = customerService.getCustomerById(invoice.getCustomerId());

            // Header
            document.add(new Paragraph("INVOICE")
                    .setFontSize(20)
                    .setBold());

            document.add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Date: " + invoice.getIssueDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
            document.add(new Paragraph("Due Date: " + invoice.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
            document.add(new Paragraph("\n"));

            // Customer Info
            if (customer != null) {
                document.add(new Paragraph("Bill To:"));
                document.add(new Paragraph(customer.getFullName()));
                document.add(new Paragraph(customer.getEmail()));
                if (customer.getServiceAddress() != null) {
                    document.add(new Paragraph(customer.getServiceAddress().getInlineAddress()));
                }
                document.add(new Paragraph("\n"));
            }

            // Line Items Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2})).useAllAvailableWidth();
            table.addHeaderCell("Description");
            table.addHeaderCell("Quantity");
            table.addHeaderCell("Unit Price");
            table.addHeaderCell("Total");

            for (Invoice.InvoiceLineItem item : invoice.getLineItems()) {
                table.addCell(item.getDescription());
                table.addCell(String.format("%.2f %s", item.getQuantity(), item.getUnit()));
                table.addCell(String.format("£%.4f", item.getUnitPrice()));
                table.addCell(String.format("£%.2f", item.getAmount()));
            }

            document.add(table);

            // Totals
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(String.format("Subtotal: £%.2f", invoice.getUnitCost().add(invoice.getStandingChargeTotal()))));
            document.add(new Paragraph(String.format("VAT (%.0f%%): £%.2f", invoice.getVatRate(), invoice.getVatAmount())));
            document.add(new Paragraph(String.format("Total Amount: £%.2f", invoice.getTotalAmount())).setBold());

            document.close();
            AppLogger.info(CLASS_NAME, "PDF generated at: " + destination.getAbsolutePath());

        } catch (Exception e) {
            AppLogger.error(CLASS_NAME, "Failed to generate PDF", e);
            throw new DocumentGenerationException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
