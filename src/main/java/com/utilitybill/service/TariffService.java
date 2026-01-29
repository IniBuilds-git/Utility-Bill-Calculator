package com.utilitybill.service;

import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.GasTariff;
import com.utilitybill.model.MeterType;
import com.utilitybill.model.Tariff;

import com.utilitybill.util.AppLogger;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TariffService {

    private static final String CLASS_NAME = TariffService.class.getName();

    private static volatile TariffService instance;
    private final TariffDAO tariffDAO;

    private TariffService() {
        this.tariffDAO = TariffDAO.getInstance();
        initializeDefaultTariffs();
    }

    public static TariffService getInstance() {
        if (instance == null) {
            synchronized (TariffService.class) {
                if (instance == null) {
                    instance = new TariffService();
                }
            }
        }
        return instance;
    }

    private void initializeDefaultTariffs() {
        try {
            if (tariffDAO.count() == 0) {
                ElectricityTariff elecTariff = new ElectricityTariff(
                        "Flexible 6 Direct Debit eBill",
                        new BigDecimal("23.76"), // 23.76p standing charge per day
                        new BigDecimal("20.316"), // Day rate: 20.316p per kWh
                        new BigDecimal("20.316") // Night rate: 20.316p per kWh
                );
                elecTariff.setDescription("Direct Debit, prices include VAT");
                tariffDAO.save(elecTariff);

                GasTariff gasTariff = new GasTariff(
                        "Flexible 6 Direct Debit eBill",
                        new BigDecimal("26.11"), // 26.11p standing charge per day
                        new BigDecimal("3.987") // Unit rate: 3.987p per kWh
                );
                gasTariff.setDescription("Direct Debit, prices include VAT");
                tariffDAO.save(gasTariff);

                AppLogger.info(CLASS_NAME, "Default tariffs created successfully");
            }
        } catch (DataPersistenceException e) {
            AppLogger.warning(CLASS_NAME, "Could not initialize default tariffs: " + e.getMessage(), e);
        }
    }

    public ElectricityTariff createElectricityTariff(String name, BigDecimal standingCharge,
            BigDecimal dayRate, BigDecimal nightRate,
            String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, dayRate);

        ElectricityTariff tariff = new ElectricityTariff(name, standingCharge, dayRate, nightRate);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    // Overload for backward compatibility with annualUsage parameter (ignored)
    @Deprecated
    public ElectricityTariff createElectricityTariff(String name, BigDecimal standingCharge,
            double annualUsage, BigDecimal dayRate,
            BigDecimal nightRate, String description)
            throws ValidationException, DataPersistenceException {
        // Ignore annualUsage parameter - it's customer-specific, not tariff-specific
        return createElectricityTariff(name, standingCharge, dayRate, nightRate, description);
    }

    // Legacy method for flat rate (backward compatibility)
    public ElectricityTariff createElectricityTariff(String name, BigDecimal standingCharge,
            BigDecimal unitRatePence, String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, unitRatePence);

        ElectricityTariff tariff = new ElectricityTariff(name, standingCharge, unitRatePence);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    public ElectricityTariff createTieredElectricityTariff(String name, BigDecimal standingCharge,
            double tier1Threshold, BigDecimal tier1Rate,
            BigDecimal tier2Rate, String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, tier1Rate);

        if (tier1Threshold <= 0) {
            throw new ValidationException("tier1Threshold", "Threshold must be positive");
        }

        ElectricityTariff tariff = ElectricityTariff.createTieredTariff(name, standingCharge, tier1Threshold, tier1Rate,
                tier2Rate);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    public GasTariff createGasTariff(String name, BigDecimal standingCharge,
            BigDecimal unitRatePence, String description)
            throws ValidationException, DataPersistenceException {

        // Validate name, standing charge, and unit rate
        validateTariffInputs(name, standingCharge, unitRatePence);

        GasTariff tariff = new GasTariff(name, standingCharge, unitRatePence);

        // Set description if provided
        if (description != null && !description.trim().isEmpty()) {
            tariff.setDescription(description.trim());
        }

        // Save tariff
        tariffDAO.save(tariff);

        return tariff;
    }

    // Overload for backward compatibility with annualUsage parameter (ignored)
    @Deprecated
    public GasTariff createGasTariff(String name, BigDecimal standingCharge, double annualUsage,
            BigDecimal unitRatePence, String description)
            throws ValidationException, DataPersistenceException {
        // Ignore annualUsage parameter - it's customer-specific, not tariff-specific
        return createGasTariff(name, standingCharge, unitRatePence, description);
    }

    private void validateTariffInputs(String name, BigDecimal standingCharge, BigDecimal unitRate)
            throws ValidationException {

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "Tariff name is required");
        }
        if (standingCharge == null || standingCharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("standingCharge", "Standing charge cannot be negative");
        }
        if (unitRate == null || unitRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("unitRate", "Unit rate must be positive");
        }
    }

    public Tariff getTariffById(String tariffId) throws DataPersistenceException {
        return tariffDAO.findById(tariffId).orElse(null);
    }

    public Tariff getTariffByName(String name) throws DataPersistenceException {
        return tariffDAO.findByName(name).orElse(null);
    }

    public List<Tariff> getAllTariffs() throws DataPersistenceException {
        return tariffDAO.findAll();
    }

    public List<Tariff> getActiveTariffs() throws DataPersistenceException {
        return tariffDAO.findAllActive();
    }

    public List<Tariff> getTariffsByMeterType(MeterType meterType) throws DataPersistenceException {
        return tariffDAO.findByMeterType(meterType);
    }

    public List<Tariff> getActiveTariffsByMeterType(MeterType meterType) throws DataPersistenceException {
        return tariffDAO.findActiveByMeterType(meterType);
    }

    public void updateTariff(Tariff tariff) throws DataPersistenceException {
        tariffDAO.update(tariff);
    }

    public void deactivateTariff(String tariffId) throws DataPersistenceException {
        Optional<Tariff> tariffOpt = tariffDAO.findById(tariffId);
        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            tariff.setActive(false);
            tariffDAO.update(tariff);
        }
    }

    public void reactivateTariff(String tariffId) throws DataPersistenceException {
        Optional<Tariff> tariffOpt = tariffDAO.findById(tariffId);
        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            tariff.setActive(true);
            tariffDAO.update(tariff);
        }
    }

    public void createTariff(Tariff tariff) throws DataPersistenceException {
        tariffDAO.save(tariff);
    }

    public void deleteTariff(String tariffId) throws DataPersistenceException {
        tariffDAO.delete(tariffId);
    }
}
