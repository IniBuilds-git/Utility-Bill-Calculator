package com.utilitybill.service;

import com.utilitybill.dao.InvoiceDAO;
import com.utilitybill.dao.MeterReadingDAO;
import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidMeterReadingException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.*;
import com.utilitybill.util.BillCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for billing operations.
 * Handles meter readings, invoice generation, and billing calculations.
 *
 * <p>Design Pattern: Singleton - Only one instance manages billing operations.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class BillingService {

    /** Singleton instance */
    private static volatile BillingService instance;

    /** Data access objects */
    private final InvoiceDAO invoiceDAO;
    private final MeterReadingDAO meterReadingDAO;
    private final TariffDAO tariffDAO;
    private final CustomerService customerService;

    /**
     * Private constructor for singleton pattern.
     */
    private BillingService() {
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.meterReadingDAO = MeterReadingDAO.getInstance();
        this.tariffDAO = TariffDAO.getInstance();
        this.customerService = CustomerService.getInstance();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the BillingService instance
     */
    public static BillingService getInstance() {
        if (instance == null) {
            synchronized (BillingService.class) {
                if (instance == null) {
                    instance = new BillingService();
                }
            }
        }
        return instance;
    }

    /**
     * Records a new meter reading.
     *
     * @param customerId   the customer ID
     * @param meterId      the meter ID
     * @param readingValue the meter reading value
     * @param readingDate  the date the reading was taken
     * @param readingType  the type of reading
     * @return the created meter reading
     * @throws InvalidMeterReadingException if the reading is invalid
     * @throws CustomerNotFoundException    if customer not found
     * @throws DataPersistenceException     if data access fails
     */
    public MeterReading recordMeterReading(String customerId, String meterId, double readingValue,
                                            LocalDate readingDate, MeterReading.ReadingType readingType)
            throws InvalidMeterReadingException, CustomerNotFoundException, DataPersistenceException {

        // Validate customer exists
        Customer customer = customerService.getCustomerById(customerId);

        // Validate meter belongs to customer
        Meter meter = customer.getMeterById(meterId);
        if (meter == null) {
            throw new InvalidMeterReadingException(meterId, readingValue, "MTR004",
                    "Meter not found for customer");
        }

        // Get previous reading
        double previousReading = meterReadingDAO.getPreviousReadingValue(meterId);

        // Validate reading value
        if (readingValue < 0) {
            throw InvalidMeterReadingException.negativeReading(meterId, readingValue);
        }
        if (readingValue < previousReading) {
            throw new InvalidMeterReadingException(meterId, readingValue, previousReading);
        }

        // Create meter reading
        MeterReading reading = new MeterReading(meterId, customerId, readingValue, previousReading, readingType);
        reading.setReadingDate(readingDate);

        // Save reading
        meterReadingDAO.save(reading);

        // Update meter's current reading
        meter.setCurrentReading(readingValue);

        return reading;
    }

    /**
     * Generates an invoice for a customer.
     *
     * @param customerId  the customer ID
     * @param periodStart the billing period start date
     * @param periodEnd   the billing period end date
     * @return the generated invoice
     * @throws CustomerNotFoundException if customer not found
     * @throws ValidationException       if tariff not found or no readings
     * @throws DataPersistenceException  if data access fails
     */
    public Invoice generateInvoice(String customerId, LocalDate periodStart, LocalDate periodEnd)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {

        // Get customer
        Customer customer = customerService.getCustomerById(customerId);

        // Get tariff
        if (customer.getTariffId() == null) {
            throw new ValidationException("tariff", "Customer has no assigned tariff");
        }

        Optional<Tariff> tariffOpt = tariffDAO.findById(customer.getTariffId());
        if (tariffOpt.isEmpty()) {
            throw new ValidationException("tariff", "Assigned tariff not found");
        }
        Tariff tariff = tariffOpt.get();

        // Create invoice
        Invoice invoice = new Invoice(customerId, customer.getAccountNumber(), periodStart, periodEnd);
        invoice.setTariffId(tariff.getTariffId());
        invoice.setTariffName(tariff.getName());
        invoice.setMeterType(tariff.getMeterType());
        invoice.setVatRate(tariff.getVatRate());

        // Get meter readings for the period
        List<Meter> meters = customer.getMetersByType(tariff.getMeterType());
        if (meters.isEmpty()) {
            throw new ValidationException("meter", "Customer has no meter for tariff type");
        }

        // Process each meter
        double totalUnits = 0;
        for (Meter meter : meters) {
            List<MeterReading> readings = meterReadingDAO.findByMeterIdAndDateRange(
                    meter.getMeterId(), periodStart, periodEnd);

            if (!readings.isEmpty()) {
                MeterReading firstReading = readings.get(0);
                MeterReading lastReading = readings.get(readings.size() - 1);

                invoice.setOpeningReading(firstReading.getPreviousReadingValue());
                invoice.setClosingReading(lastReading.getReadingValue());

                // Calculate consumption from readings
                for (MeterReading reading : readings) {
                    totalUnits += reading.getConsumption();
                    reading.markAsBilled();
                    meterReadingDAO.update(reading);
                }
            }
        }

        invoice.setUnitsConsumed(totalUnits);
        invoice.setUnitRate(tariff.getUnitRate());

        // Calculate billing days
        int billingDays = BillCalculator.calculateBillingDays(periodStart, periodEnd);

        // Calculate costs using the tariff
        BigDecimal unitCost = tariff.calculateUnitCost(totalUnits);
        invoice.setUnitCost(unitCost);

        BigDecimal standingCharge = BillCalculator.calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        invoice.setStandingChargeTotal(standingCharge);

        // Add line items
        invoice.addLineItem(new Invoice.InvoiceLineItem(
                tariff.getMeterType().getDisplayName() + " usage",
                totalUnits,
                "kWh",
                tariff.getUnitRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                unitCost
        ));

        invoice.addLineItem(new Invoice.InvoiceLineItem(
                "Standing charge",
                billingDays,
                "days",
                tariff.getStandingCharge(),
                standingCharge
        ));

        // Calculate totals
        invoice.calculateTotals();

        // Save invoice
        invoiceDAO.save(invoice);

        // Debit customer account
        customerService.debitAccount(customerId, invoice.getTotalAmount());

        return invoice;
    }

    /**
     * Gets an invoice by ID.
     *
     * @param invoiceId the invoice ID
     * @return the invoice, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Invoice getInvoiceById(String invoiceId) throws DataPersistenceException {
        return invoiceDAO.findById(invoiceId).orElse(null);
    }

    /**
     * Gets an invoice by invoice number.
     *
     * @param invoiceNumber the invoice number
     * @return the invoice, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Invoice getInvoiceByNumber(String invoiceNumber) throws DataPersistenceException {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber).orElse(null);
    }

    /**
     * Gets all invoices for a customer.
     *
     * @param customerId the customer ID
     * @return list of invoices
     * @throws DataPersistenceException if data access fails
     */
    public List<Invoice> getCustomerInvoices(String customerId) throws DataPersistenceException {
        return invoiceDAO.findByCustomerId(customerId);
    }

    /**
     * Gets all unpaid invoices.
     *
     * @return list of unpaid invoices
     * @throws DataPersistenceException if data access fails
     */
    public List<Invoice> getUnpaidInvoices() throws DataPersistenceException {
        return invoiceDAO.findUnpaid();
    }

    /**
     * Gets all overdue invoices.
     *
     * @return list of overdue invoices
     * @throws DataPersistenceException if data access fails
     */
    public List<Invoice> getOverdueInvoices() throws DataPersistenceException {
        return invoiceDAO.findOverdue();
    }

    /**
     * Gets meter readings for a customer.
     *
     * @param customerId the customer ID
     * @return list of meter readings
     * @throws DataPersistenceException if data access fails
     */
    public List<MeterReading> getCustomerReadings(String customerId) throws DataPersistenceException {
        return meterReadingDAO.findByCustomerId(customerId);
    }

    /**
     * Gets meter readings for a specific meter.
     *
     * @param meterId the meter ID
     * @return list of meter readings
     * @throws DataPersistenceException if data access fails
     */
    public List<MeterReading> getMeterReadings(String meterId) throws DataPersistenceException {
        return meterReadingDAO.findByMeterId(meterId);
    }

    /**
     * Gets the latest reading for a meter.
     *
     * @param meterId the meter ID
     * @return the latest reading, or null if none
     * @throws DataPersistenceException if data access fails
     */
    public MeterReading getLatestReading(String meterId) throws DataPersistenceException {
        return meterReadingDAO.findLatestByMeterId(meterId).orElse(null);
    }

    /**
     * Cancels an invoice.
     *
     * @param invoiceId the invoice ID
     * @throws DataPersistenceException if data access fails
     */
    public void cancelInvoice(String invoiceId) throws DataPersistenceException {
        Optional<Invoice> invoiceOpt = invoiceDAO.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
            invoiceDAO.update(invoice);

            // Credit back the amount to customer
            try {
                customerService.creditAccount(invoice.getCustomerId(), invoice.getTotalAmount());
            } catch (CustomerNotFoundException | ValidationException e) {
                // Log but don't fail
                System.err.println("Warning: Could not credit customer: " + e.getMessage());
            }
        }
    }
}

