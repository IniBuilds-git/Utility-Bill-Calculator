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

public class BillingService {

    private static volatile BillingService instance;
    private final InvoiceDAO invoiceDAO;
    private final MeterReadingDAO meterReadingDAO;
    private final TariffDAO tariffDAO;
    private final CustomerService customerService;

    private BillingService() {
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.meterReadingDAO = MeterReadingDAO.getInstance();
        this.tariffDAO = TariffDAO.getInstance();
        this.customerService = CustomerService.getInstance();
    }

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

    public MeterReading recordMeterReading(String customerId, String meterId, double readingValue,
                                            LocalDate readingDate, MeterReading.ReadingType readingType)
            throws InvalidMeterReadingException, CustomerNotFoundException, DataPersistenceException {

        Customer customer = customerService.getCustomerById(customerId);

        Meter meter = customer.getMeterById(meterId);
        if (meter == null) {
            throw new InvalidMeterReadingException(meterId, readingValue, "MTR004",
                    "Meter not found for customer");
        }

        double previousReading = meterReadingDAO.getPreviousReadingValue(meterId);

        if (readingValue < 0) {
            throw InvalidMeterReadingException.negativeReading(meterId, readingValue);
        }
        if (readingValue < previousReading) {
            throw new InvalidMeterReadingException(meterId, readingValue, previousReading);
        }

        MeterReading reading = new MeterReading(meterId, customerId, readingValue, previousReading, readingType);
        reading.setReadingDate(readingDate);

        meterReadingDAO.save(reading);

        meter.setCurrentReading(readingValue);

        return reading;
    }

    public Invoice generateInvoice(String customerId, LocalDate periodStart, LocalDate periodEnd)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {

        Customer customer = customerService.getCustomerById(customerId);

        if (customer.getTariffId() == null) {
            throw new ValidationException("tariff", "Customer has no assigned tariff");
        }

        Optional<Tariff> tariffOpt = tariffDAO.findById(customer.getTariffId());
        if (tariffOpt.isEmpty()) {
            throw new ValidationException("tariff", "Assigned tariff not found");
        }
        Tariff tariff = tariffOpt.get();

        Invoice invoice = new Invoice(customerId, customer.getAccountNumber(), periodStart, periodEnd);
        invoice.setTariffId(tariff.getTariffId());
        invoice.setTariffName(tariff.getName());
        invoice.setMeterType(tariff.getMeterType());
        invoice.setVatRate(tariff.getVatRate());

        List<Meter> meters = customer.getMetersByType(tariff.getMeterType());
        if (meters.isEmpty()) {
            throw new ValidationException("meter", "Customer has no meter for tariff type");
        }

        double totalUnits = 0;
        for (Meter meter : meters) {
            List<MeterReading> readings = meterReadingDAO.findByMeterIdAndDateRange(
                    meter.getMeterId(), periodStart, periodEnd);

            if (!readings.isEmpty()) {
                MeterReading firstReading = readings.get(0);
                MeterReading lastReading = readings.get(readings.size() - 1);

                invoice.setOpeningReading(firstReading.getPreviousReadingValue());
                invoice.setClosingReading(lastReading.getReadingValue());

                for (MeterReading reading : readings) {
                    totalUnits += reading.getConsumption();
                    reading.markAsBilled();
                    meterReadingDAO.update(reading);
                }
            }
        }

        invoice.setUnitsConsumed(totalUnits);
        invoice.setUnitRate(tariff.getUnitRate());

        int billingDays = BillCalculator.calculateBillingDays(periodStart, periodEnd);

        BigDecimal unitCost = tariff.calculateUnitCost(totalUnits);
        invoice.setUnitCost(unitCost);

        BigDecimal standingCharge = BillCalculator.calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        invoice.setStandingChargeTotal(standingCharge);

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

        invoice.calculateTotals();

        invoiceDAO.save(invoice);

        customerService.debitAccount(customerId, invoice.getTotalAmount());

        return invoice;
    }

    public Invoice getInvoiceById(String invoiceId) throws DataPersistenceException {
        return invoiceDAO.findById(invoiceId).orElse(null);
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) throws DataPersistenceException {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber).orElse(null);
    }

    public List<Invoice> getCustomerInvoices(String customerId) throws DataPersistenceException {
        return invoiceDAO.findByCustomerId(customerId);
    }

    public List<Invoice> getUnpaidInvoices() throws DataPersistenceException {
        return invoiceDAO.findUnpaid();
    }

    public List<Invoice> getOverdueInvoices() throws DataPersistenceException {
        return invoiceDAO.findOverdue();
    }

    public List<MeterReading> getCustomerReadings(String customerId) throws DataPersistenceException {
        return meterReadingDAO.findByCustomerId(customerId);
    }

    public List<MeterReading> getMeterReadings(String meterId) throws DataPersistenceException {
        return meterReadingDAO.findByMeterId(meterId);
    }

    public MeterReading getLatestReading(String meterId) throws DataPersistenceException {
        return meterReadingDAO.findLatestByMeterId(meterId).orElse(null);
    }

    public void cancelInvoice(String invoiceId) throws DataPersistenceException {
        Optional<Invoice> invoiceOpt = invoiceDAO.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
            invoiceDAO.update(invoice);

            try {
                customerService.creditAccount(invoice.getCustomerId(), invoice.getTotalAmount());
            } catch (CustomerNotFoundException | ValidationException e) {
                System.err.println("Warning: Could not credit customer: " + e.getMessage());
            }
        }
    }
}

