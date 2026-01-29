package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.DocumentGenerationException;
import com.utilitybill.exception.NotificationException;
import com.utilitybill.model.*;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.EmailService;
import com.utilitybill.service.PaymentService;
import com.utilitybill.service.PdfService;
import com.utilitybill.service.TariffService;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.io.File;
import com.utilitybill.util.AppLogger;
import com.utilitybill.util.DateUtil;
import com.utilitybill.util.FormatUtil;
import com.utilitybill.util.ViewUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CustomerDetailsController {

    private static final String CLASS_NAME = CustomerDetailsController.class.getName();

    // Header Labels
    @FXML private Label customerNameLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Label balanceLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;

    // Overview Tab
    @FXML private Label tariffNameLabel;
    @FXML private Label meterCountLabel;
    @FXML private Label lastPaymentLabel;
    @FXML private Label predictedUsageLabel;
    @FXML private BarChart<String, Number> usageChart;

    // Meter Readings Tab
    @FXML private TableView<MeterReading> readingsTable;
    @FXML private TableColumn<MeterReading, String> readingDateCol;
    @FXML private TableColumn<MeterReading, String> readingMeterIdCol;
    @FXML private TableColumn<MeterReading, String> readingValueCol;
    @FXML private TableColumn<MeterReading, String> consumptionCol;

    // Invoices Tab
    @FXML private TableView<Invoice> invoicesTable;
    @FXML private TableColumn<Invoice, String> invoiceNumberCol;
    @FXML private TableColumn<Invoice, String> invoiceDateCol;
    @FXML private TableColumn<Invoice, String> invoiceAmountCol;
    @FXML private TableColumn<Invoice, String> invoiceStatusCol;
    @FXML private TableColumn<Invoice, String> invoiceDueDateCol;
    @FXML private TableColumn<Invoice, Void> invoiceActionsCol;

    // Payments Tab
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> paymentDateCol;
    @FXML private TableColumn<Payment, String> paymentAmountCol;
    @FXML private TableColumn<Payment, String> paymentMethodCol;
    @FXML private TableColumn<Payment, String> paymentRefCol;

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final TariffService tariffService;
    private final PdfService pdfService;
    private final EmailService emailService;
    private Customer currentCustomer;

    public CustomerDetailsController() {
        this.billingService = BillingService.getInstance();
        this.paymentService = PaymentService.getInstance();
        this.tariffService = TariffService.getInstance();
        this.pdfService = PdfService.getInstance();
        this.emailService = EmailService.getInstance();
    }

    @FXML
    public void initialize() {
        setupReadingsTable();
        setupInvoicesTable();
        setupPaymentsTable();
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            updateHeader();
            refreshData();
        }
    }

    private void updateHeader() {
        customerNameLabel.setText(currentCustomer.getFullName());
        accountNumberLabel.setText(currentCustomer.getAccountNumber());
        balanceLabel.setText(FormatUtil.formatCurrency(currentCustomer.getAccountBalance()));
        emailLabel.setText(currentCustomer.getEmail());
        phoneLabel.setText(currentCustomer.getPhone());
        addressLabel.setText(currentCustomer.getServiceAddress() != null ? currentCustomer.getServiceAddress().getInlineAddress() : "No Address");

        // Style balance based on debt
        if (currentCustomer.hasDebt()) {
            balanceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        } else {
            balanceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        }
    }

    private void refreshData() {
        loadOverviewStats();
        loadReadings();
        loadInvoices();
        loadPayments();
    }

    private void loadOverviewStats() {
        try {
            // Tariff Name
            if (currentCustomer.getTariffId() != null) {
                Tariff tariff = tariffService.getTariffById(currentCustomer.getTariffId());
                tariffNameLabel.setText(tariff != null ? tariff.getName() : "Unknown Tariff");
            } else {
                tariffNameLabel.setText("No Tariff Assigned");
            }

            // Meter Count
            meterCountLabel.setText(String.valueOf(currentCustomer.getMeters().size()));

            // Last Payment
            List<Payment> payments = new java.util.ArrayList<>(paymentService.getCustomerPayments(currentCustomer.getCustomerId()));
            if (!payments.isEmpty()) {
                // Assuming newer payments are at the end or sorting needed? 
                // Let's sort to be safe
                payments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());
                Payment lastPayment = payments.get(0);
                lastPaymentLabel.setText(FormatUtil.formatCurrency(lastPayment.getAmount()));
            } else {
                lastPaymentLabel.setText("None");
            }

        } catch (DataPersistenceException e) {
            AppLogger.error(CLASS_NAME, "Error loading stats", e);
        }
    }

    private void setupReadingsTable() {
        readingDateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getReadingDate())));
        readingMeterIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMeterId()));
        readingValueCol.setCellValueFactory(data -> new SimpleStringProperty(FormatUtil.formatReading(data.getValue().getReadingValue())));
        consumptionCol.setCellValueFactory(data -> new SimpleStringProperty(FormatUtil.formatReading(data.getValue().getConsumption())));
    }

    private void loadReadings() {
        try {
            List<MeterReading> readings = new java.util.ArrayList<>(billingService.getCustomerReadings(currentCustomer.getCustomerId()));
            readings.sort(Comparator.comparing(MeterReading::getReadingDate).reversed());
            readingsTable.setItems(FXCollections.observableArrayList(readings));
            populateUsageChart(readings);
        } catch (DataPersistenceException e) {
             AppLogger.error(CLASS_NAME, "Error loading readings", e);
        }
    }

    private void populateUsageChart(List<MeterReading> readings) {
        usageChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Consumption (Last 12 Months)");

        // Group readings by Month/Year and sum consumption
        LocalDate oneYearAgo = LocalDate.now().minusMonths(11).withDayOfMonth(1); // 12 months window including current

        Map<YearMonth, Double> monthlyConsumptionMap = new TreeMap<>();
        
        // Initialize map with 0.0 for last 12 months
        for (int i = 0; i < 12; i++) {
             monthlyConsumptionMap.put(YearMonth.from(LocalDate.now().minusMonths(i)), 0.0);
        }

        readings.stream()
                .filter(r -> !r.getReadingDate().isBefore(oneYearAgo))
                .forEach(r -> {
                    YearMonth ym = YearMonth.from(r.getReadingDate());
                    monthlyConsumptionMap.merge(ym, r.getConsumption(), Double::sum);
                });

        monthlyConsumptionMap.forEach((ym, consumption) -> {
            series.getData().add(new XYChart.Data<>(ym.format(DateTimeFormatter.ofPattern("MMM yy")), consumption));
        });

        usageChart.getData().add(series);
        
        // Calculate and set prediction
        calculateAndSetPrediction(readings);
    }

    private void calculateAndSetPrediction(List<MeterReading> readings) {
        if (readings.isEmpty()) {
            predictedUsageLabel.setText("N/A");
            return;
        }

        // Simple prediction: Average of last 3 months
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        
        double totalRecentConsumption = readings.stream()
            .filter(r -> r.getReadingDate().isAfter(threeMonthsAgo))
            .mapToDouble(MeterReading::getConsumption)
            .sum();
            
        long count = readings.stream()
            .filter(r -> r.getReadingDate().isAfter(threeMonthsAgo))
            .count();
            
        if (count == 0) {
             // Fallback to last reading if no recent history
             MeterReading last = readings.get(0); // Sorted desc
             predictedUsageLabel.setText(String.format("%.0f kWh", last.getConsumption()));
        } else {
             // Average per reading * 1 (assuming readings are monthly)
             // Better: Average per month.
             // Let's assume readings are roughly monthly for this simple prediction
             double avg = totalRecentConsumption / (count > 0 ? count : 1);
             predictedUsageLabel.setText(String.format("~ %.0f kWh", avg));
        }
    }


    private void setupInvoicesTable() {
        invoiceNumberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        invoiceDateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getIssueDate())));
        invoiceAmountCol.setCellValueFactory(data -> new SimpleStringProperty(FormatUtil.formatCurrency(data.getValue().getTotalAmount())));
        invoiceStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        invoiceDueDateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getDueDate())));
        
        invoiceStatusCol.setCellFactory(col -> new TableCell<>() {
             @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Paid" -> setStyle("-fx-text-fill: #10b981;");
                        case "Overdue" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        case "Pending" -> setStyle("-fx-text-fill: #f59e0b;");
                        default -> setStyle("");
                    }
                }
            }
        });

        invoiceActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button emailBtn = new Button("Email");

            {
                emailBtn.setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #0d9488; -fx-font-size: 11px; -fx-cursor: hand;");
                emailBtn.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    emailInvoice(invoice);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : emailBtn);
            }
        });
    }

    private void emailInvoice(Invoice invoice) {
        try {
            File pdfFile = File.createTempFile("invoice_" + invoice.getInvoiceNumber(), ".pdf");
            pdfService.generateInvoice(invoice, pdfFile);

            String subject = "Invoice #" + invoice.getInvoiceNumber();
            String body = "Dear " + currentCustomer.getFullName() + ",\n\nPlease find attached your invoice.\n\nThank you.";
            
            emailService.sendNotification(currentCustomer.getEmail(), subject, body, pdfFile);
            
            ViewUtil.showInfo("Success", "Invoice emailed to " + currentCustomer.getEmail());
            
        } catch (DocumentGenerationException e) {
            AppLogger.error(CLASS_NAME, "Failed to generate PDF for email", e);
            ViewUtil.showError("Generation Error", "Could not create invoice PDF: " + e.getMessage());
        } catch (NotificationException e) {
            AppLogger.error(CLASS_NAME, "Failed to send email", e);
            ViewUtil.showError("Email Failed", "Could not send email notification: " + e.getMessage());
        } catch (Exception e) {
            AppLogger.error(CLASS_NAME, "Unexpected error email invoice", e);
            ViewUtil.showError("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void loadInvoices() {
        try {
            List<Invoice> invoices = new java.util.ArrayList<>(billingService.getCustomerInvoices(currentCustomer.getCustomerId()));
            invoices.sort(Comparator.comparing(Invoice::getIssueDate).reversed());
            invoicesTable.setItems(FXCollections.observableArrayList(invoices));
        } catch (DataPersistenceException e) {
             AppLogger.error(CLASS_NAME, "Error loading invoices", e);
        }
    }

    private void setupPaymentsTable() {
        paymentDateCol.setCellValueFactory(data -> new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getPaymentDate())));
        paymentAmountCol.setCellValueFactory(data -> new SimpleStringProperty(FormatUtil.formatCurrency(data.getValue().getAmount())));
        paymentMethodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMethod().toString())); // Enum toString or display name?
        paymentRefCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReferenceNumber()));
    }

    private void loadPayments() {
        try {
            List<Payment> payments = new java.util.ArrayList<>(paymentService.getCustomerPayments(currentCustomer.getCustomerId()));
            payments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());
            paymentsTable.setItems(FXCollections.observableArrayList(payments));
        } catch (DataPersistenceException e) {
             AppLogger.error(CLASS_NAME, "Error loading payments", e);
        }
    }

    @FXML
    public void close() {
        Stage stage = (Stage) customerNameLabel.getScene().getWindow();
        stage.close();
    }
}
