package com.utilitybill.service;

import com.utilitybill.dao.InvoiceDAO;
import com.utilitybill.dao.MeterReadingDAO;
import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidMeterReadingException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.*;
import com.utilitybill.util.AppLogger;
import com.utilitybill.util.BillCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for billing operations including meter readings and invoice
 * generation.
 * 
 * <p>
 * This service provides the core billing functionality for the Utility Bill
 * Management System,
 * handling meter reading recording, invoice generation, and bill calculations
 * for both
 * electricity and gas utilities.
 * </p>
 * 
 * <p>
 * The service uses the Singleton pattern and can be obtained via
 * {@link #getInstance()}.
 * </p>
 * 
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * BillingService billingService = BillingService.getInstance();
 * 
 * // Record a meter reading
 * MeterReading reading = billingService.recordMeterReading(
 *         customerId, meterId, 1234.5, LocalDate.now(), ReadingType.ACTUAL);
 * 
 * // Generate an invoice
 * Invoice invoice = billingService.generateInvoice(
 *         customerId, periodStart, periodEnd);
 * }</pre>
 * 
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 * @see CustomerService
 * @see BillCalculator
 */
public class BillingService {

    private static final String CLASS_NAME = BillingService.class.getSimpleName();

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

    /**
     * Returns the singleton instance of the BillingService.
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
     * Records a new meter reading for a customer's meter.
     * 
     * <p>
     * Validates that the reading is non-negative and greater than or equal to the
     * previous reading. The reading is associated with the customer and meter, and
     * consumption is automatically calculated.
     * </p>
     * 
     * @param customerId   the unique identifier of the customer
     * @param meterId      the unique identifier of the meter
     * @param readingValue the meter reading value (must be >= previous reading)
     * @param readingDate  the date the reading was taken
     * @param readingType  the type of reading (ACTUAL, ESTIMATED, etc.)
     * @return the newly created MeterReading object
     * @throws InvalidMeterReadingException if the reading value is invalid
     * @throws CustomerNotFoundException    if the customer is not found
     * @throws DataPersistenceException     if there is a data storage error
     */
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

        AppLogger.info(CLASS_NAME, "Meter reading recorded: meterId=" + meterId +
                " value=" + readingValue + " consumption=" + reading.getConsumption());

        return reading;
    }

    /**
     * Generates an invoice for a customer covering a specified billing period.
     * 
     * <p>
     * This method calculates the bill based on meter readings within the period,
     * applies the customer's tariff rates, adds standing charges, and calculates
     * VAT.
     * The invoice is saved and the customer's account is debited.
     * </p>
     * 
     * @param customerId  the unique identifier of the customer
     * @param periodStart the start date of the billing period (inclusive)
     * @param periodEnd   the end date of the billing period (inclusive)
     * @return the generated Invoice with all charges calculated
     * @throws CustomerNotFoundException if the customer is not found
     * @throws ValidationException       if the customer has no tariff or invalid
     *                                   data
     * @throws DataPersistenceException  if there is a data storage error
     */
    public Invoice generateInvoice(String customerId, LocalDate periodStart, LocalDate periodEnd)
            throws CustomerNotFoundException, ValidationException, DataPersistenceException {

        // Validate date range
        if (periodStart == null || periodEnd == null) {
            throw new ValidationException("dates", "Billing period dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new ValidationException("dates",
                    "Period start date must be before or equal to end date");
        }
        if (periodEnd.isAfter(LocalDate.now())) {
            AppLogger.warning("Invoice generated with future end date: " + periodEnd);
        }

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

        // Set account balance information
        invoice.setPreviousBalance(customer.getAccountBalance());

        List<Meter> meters = customer.getMetersByType(tariff.getMeterType());
        if (meters.isEmpty()) {
            throw new ValidationException("meter", "Customer has no meter for tariff type");
        }

        int billingDays = BillCalculator.calculateBillingDays(periodStart, periodEnd);

        // Handle different meter types with specific logic
        if (tariff.getMeterType() == MeterType.ELECTRICITY) {
            processElectricityInvoice(invoice, meters, periodStart, periodEnd, (ElectricityTariff) tariff, billingDays);
        } else if (tariff.getMeterType() == MeterType.GAS) {
            processGasInvoice(invoice, meters, periodStart, periodEnd, (GasTariff) tariff, billingDays);
        }

        invoice.calculateTotals();
        invoice.setAccountBalanceAfter(customer.getAccountBalance().add(invoice.getTotalAmount()));

        invoiceDAO.save(invoice);

        customerService.debitAccount(customerId, invoice.getTotalAmount());

        AppLogger.info(CLASS_NAME, "Invoice generated: " + invoice.getInvoiceNumber() +
                " for customer " + customer.getAccountNumber() +
                " total=£" + invoice.getTotalAmount());

        return invoice;
    }

    private void processElectricityInvoice(Invoice invoice, List<Meter> meters, LocalDate periodStart,
            LocalDate periodEnd, ElectricityTariff tariff, int billingDays)
            throws DataPersistenceException {

        double totalDayUnits = 0;
        double totalNightUnits = 0;
        boolean hasDayNightReadings = false;

        for (Meter meter : meters) {
            List<MeterReading> readings = meterReadingDAO.findByMeterIdAndDateRange(
                    meter.getMeterId(), periodStart, periodEnd);

            if (!readings.isEmpty()) {
                MeterReading firstReading = readings.get(0);
                MeterReading lastReading = readings.get(readings.size() - 1);

                // Check if this meter has day/night readings
                boolean isDayNight = false;
                if (meter instanceof ElectricityMeter) {
                    isDayNight = ((ElectricityMeter) meter).isDayNightMeter();
                }

                if (isDayNight && lastReading.hasDayNightReadings()) {
                    hasDayNightReadings = true;

                    // Store opening/closing readings for invoice
                    // For opening, use the previous reading value or the first reading's value
                    Double dayOpening = firstReading.getPreviousDayReading() != null
                            && firstReading.getPreviousDayReading() > 0
                                    ? firstReading.getPreviousDayReading()
                                    : firstReading.getDayReading();
                    Double nightOpening = firstReading.getPreviousNightReading() != null
                            && firstReading.getPreviousNightReading() > 0
                                    ? firstReading.getPreviousNightReading()
                                    : firstReading.getNightReading();

                    invoice.setDayOpeningReading(dayOpening);
                    invoice.setDayClosingReading(lastReading.getDayReading());
                    invoice.setNightOpeningReading(nightOpening);
                    invoice.setNightClosingReading(lastReading.getNightReading());

                    // Calculate consumption from all readings
                    for (MeterReading reading : readings) {
                        totalDayUnits += reading.getDayConsumption();
                        totalNightUnits += reading.getNightConsumption();
                        reading.markAsBilled();
                        meterReadingDAO.update(reading);
                    }
                } else {
                    // Standard single-rate meter
                    invoice.setOpeningReading(firstReading.getPreviousReadingValue());
                    invoice.setClosingReading(lastReading.getReadingValue());

                    for (MeterReading reading : readings) {
                        totalDayUnits += reading.getConsumption();
                        reading.markAsBilled();
                        meterReadingDAO.update(reading);
                    }
                }
            }
        }

        double totalUnits = totalDayUnits + totalNightUnits;

        if (hasDayNightReadings && tariff.getDayRate() != null && tariff.getNightRate() != null) {
            // Day/night tariff calculation
            invoice.setDayUnitsConsumed(totalDayUnits);
            invoice.setNightUnitsConsumed(totalNightUnits);
            invoice.setDayUnitRate(tariff.getDayRate());
            invoice.setNightUnitRate(tariff.getNightRate());

            BigDecimal dayUnitCost = BillCalculator.calculateUnitCost(totalDayUnits, tariff.getDayRate());
            BigDecimal nightUnitCost = BillCalculator.calculateUnitCost(totalNightUnits, tariff.getNightRate());
            BigDecimal totalUnitCost = dayUnitCost.add(nightUnitCost);

            invoice.setUnitCost(totalUnitCost);
            invoice.setUnitsConsumed(totalUnits);
            invoice.setUnitRate(tariff.getDayRate()); // Store day rate as primary

            // Add line items for day and night
            invoice.addLineItem(new Invoice.InvoiceLineItem(
                    "Electricity Day usage",
                    totalDayUnits,
                    "kWh",
                    tariff.getDayRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                    dayUnitCost));

            invoice.addLineItem(new Invoice.InvoiceLineItem(
                    "Electricity Night usage",
                    totalNightUnits,
                    "kWh",
                    tariff.getNightRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                    nightUnitCost));
        } else {
            // Standard flat-rate calculation
            invoice.setUnitsConsumed(totalUnits);
            invoice.setUnitRate(tariff.getUnitRate());

            BigDecimal unitCost = tariff.calculateUnitCost(totalUnits);
            invoice.setUnitCost(unitCost);

            invoice.addLineItem(new Invoice.InvoiceLineItem(
                    "Electricity usage",
                    totalUnits,
                    "kWh",
                    tariff.getUnitRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                    unitCost));
        }

        // Add standing charge
        BigDecimal standingCharge = BillCalculator.calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        invoice.setStandingChargeTotal(standingCharge);

        invoice.addLineItem(new Invoice.InvoiceLineItem(
                "Standing charge",
                billingDays,
                "days",
                tariff.getStandingCharge().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                standingCharge));
    }

    private void processGasInvoice(Invoice invoice, List<Meter> meters, LocalDate periodStart,
            LocalDate periodEnd, GasTariff tariff, int billingDays)
            throws DataPersistenceException {

        double totalKwh = 0;
        Double meterUnits = null;
        Double cubicMeters = null;
        Double correctedVolume = null;
        boolean isImperial = false;

        for (Meter meter : meters) {
            List<MeterReading> readings = meterReadingDAO.findByMeterIdAndDateRange(
                    meter.getMeterId(), periodStart, periodEnd);

            if (!readings.isEmpty()) {
                MeterReading firstReading = readings.get(0);
                MeterReading lastReading = readings.get(readings.size() - 1);

                invoice.setOpeningReading(firstReading.getPreviousReadingValue());
                invoice.setClosingReading(lastReading.getReadingValue());

                // Get raw meter units - use reading value as opening if previousReadingValue is
                // 0 (OPENING reading)
                double openingValue = firstReading.getPreviousReadingValue() > 0
                        ? firstReading.getPreviousReadingValue()
                        : firstReading.getReadingValue();
                double rawMeterUnits = lastReading.getReadingValue() - openingValue;
                meterUnits = rawMeterUnits;

                // Check if meter is imperial
                if (meter instanceof GasMeter) {
                    isImperial = ((GasMeter) meter).isImperialMeter();
                }

                // Convert to cubic meters
                double m3 = isImperial ? rawMeterUnits * GasTariff.IMPERIAL_TO_METRIC : rawMeterUnits;
                cubicMeters = m3;

                // Apply volume correction factor
                double corrected = m3 * tariff.getCorrectionFactor();
                correctedVolume = corrected;

                // Convert to kWh using calorific value
                double kwh = (corrected * tariff.getCalorificValue()) / GasTariff.KWH_DIVISOR.doubleValue();
                totalKwh += kwh;

                // Mark readings as billed
                for (MeterReading reading : readings) {
                    reading.setImperialMeter(isImperial);
                    reading.setCubicMeters(cubicMeters);
                    reading.setCalorificValue(tariff.getCalorificValue());
                    reading.markAsBilled();
                    meterReadingDAO.update(reading);
                }
            }
        }

        // Store gas conversion details in invoice
        invoice.setMeterUnits(meterUnits);
        invoice.setCubicMeters(cubicMeters);
        invoice.setCorrectedVolume(correctedVolume);
        invoice.setCalorificValue(tariff.getCalorificValue());
        invoice.setKwhFromGas(totalKwh);
        invoice.setImperialMeter(isImperial);

        invoice.setUnitsConsumed(totalKwh);
        invoice.setUnitRate(tariff.getUnitRate());

        // Calculate costs based on kWh
        BigDecimal unitCost = tariff.calculateUnitCost(totalKwh);
        invoice.setUnitCost(unitCost);

        invoice.addLineItem(new Invoice.InvoiceLineItem(
                String.format("Gas usage (%.1f kWh)", totalKwh),
                totalKwh,
                "kWh",
                tariff.getUnitRate().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                unitCost));

        // Add standing charge
        BigDecimal standingCharge = BillCalculator.calculateStandingCharge(tariff.getStandingCharge(), billingDays);
        invoice.setStandingChargeTotal(standingCharge);

        invoice.addLineItem(new Invoice.InvoiceLineItem(
                "Standing charge",
                billingDays,
                "days",
                tariff.getStandingCharge().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP),
                standingCharge));
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
                AppLogger.error(CLASS_NAME, "Warning: Could not credit customer: " + e.getMessage(), e);
            }
        }
    }
}
