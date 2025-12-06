package com.utilitybill.service;

import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.GasTariff;
import com.utilitybill.model.MeterType;
import com.utilitybill.model.Tariff;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for tariff management operations.
 * Handles tariff creation, updates, and retrieval.
 *
 * <p>Design Pattern: Singleton and Factory - Only one instance manages tariffs,
 * and factory methods create different tariff types.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class TariffService {

    /** Singleton instance */
    private static volatile TariffService instance;

    /** Data access object for tariffs */
    private final TariffDAO tariffDAO;

    /**
     * Private constructor for singleton pattern.
     */
    private TariffService() {
        this.tariffDAO = TariffDAO.getInstance();
        initializeDefaultTariffs();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the TariffService instance
     */
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

    /**
     * Initializes default tariffs if none exist.
     */
    private void initializeDefaultTariffs() {
        try {
            if (tariffDAO.count() == 0) {
                // Create default electricity tariff
                ElectricityTariff elecTariff = new ElectricityTariff(
                        "Standard Electricity",
                        new BigDecimal("0.45"), // 45p standing charge per day
                        new BigDecimal("28.62") // 28.62p per kWh
                );
                elecTariff.setDescription("Standard variable rate electricity tariff");
                tariffDAO.save(elecTariff);

                // Create tiered electricity tariff
                ElectricityTariff tieredElec = new ElectricityTariff(
                        "Economy Electricity",
                        new BigDecimal("0.40"),
                        1000, // First 1000 kWh at lower rate
                        new BigDecimal("25.50"),
                        new BigDecimal("30.00")
                );
                tieredElec.setDescription("Tiered pricing - lower rate for first 1000 kWh");
                tariffDAO.save(tieredElec);

                // Create default gas tariff
                GasTariff gasTariff = new GasTariff(
                        "Standard Gas",
                        new BigDecimal("0.30"), // 30p standing charge per day
                        new BigDecimal("7.42") // 7.42p per kWh
                );
                gasTariff.setDescription("Standard variable rate gas tariff");
                tariffDAO.save(gasTariff);

                System.out.println("Default tariffs created successfully");
            }
        } catch (DataPersistenceException e) {
            System.err.println("Warning: Could not initialize default tariffs: " + e.getMessage());
        }
    }

    /**
     * Creates a new flat-rate electricity tariff.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge
     * @param unitRatePence  the unit rate in pence
     * @param description    the tariff description
     * @return the created tariff
     * @throws ValidationException      if validation fails
     * @throws DataPersistenceException if data access fails
     */
    public ElectricityTariff createElectricityTariff(String name, BigDecimal standingCharge,
                                                      BigDecimal unitRatePence, String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, unitRatePence);

        ElectricityTariff tariff = new ElectricityTariff(name, standingCharge, unitRatePence);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    /**
     * Creates a new tiered electricity tariff.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge
     * @param tier1Threshold the threshold for tier 1
     * @param tier1Rate      the rate for tier 1
     * @param tier2Rate      the rate for tier 2
     * @param description    the tariff description
     * @return the created tariff
     * @throws ValidationException      if validation fails
     * @throws DataPersistenceException if data access fails
     */
    public ElectricityTariff createTieredElectricityTariff(String name, BigDecimal standingCharge,
                                                            double tier1Threshold, BigDecimal tier1Rate,
                                                            BigDecimal tier2Rate, String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, tier1Rate);

        if (tier1Threshold <= 0) {
            throw new ValidationException("tier1Threshold", "Threshold must be positive");
        }

        ElectricityTariff tariff = new ElectricityTariff(name, standingCharge, tier1Threshold, tier1Rate, tier2Rate);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    /**
     * Creates a new gas tariff.
     *
     * @param name           the tariff name
     * @param standingCharge the daily standing charge
     * @param unitRatePence  the unit rate in pence
     * @param calorificValue the calorific value (optional, defaults to 39.5)
     * @param description    the tariff description
     * @return the created tariff
     * @throws ValidationException      if validation fails
     * @throws DataPersistenceException if data access fails
     */
    public GasTariff createGasTariff(String name, BigDecimal standingCharge, BigDecimal unitRatePence,
                                      BigDecimal calorificValue, String description)
            throws ValidationException, DataPersistenceException {

        validateTariffInputs(name, standingCharge, unitRatePence);

        GasTariff tariff;
        if (calorificValue != null && calorificValue.compareTo(BigDecimal.ZERO) > 0) {
            tariff = new GasTariff(name, standingCharge, unitRatePence, calorificValue);
        } else {
            tariff = new GasTariff(name, standingCharge, unitRatePence);
        }
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

    /**
     * Validates common tariff inputs.
     */
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

    /**
     * Gets a tariff by ID.
     *
     * @param tariffId the tariff ID
     * @return the tariff, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Tariff getTariffById(String tariffId) throws DataPersistenceException {
        return tariffDAO.findById(tariffId).orElse(null);
    }

    /**
     * Gets a tariff by name.
     *
     * @param name the tariff name
     * @return the tariff, or null if not found
     * @throws DataPersistenceException if data access fails
     */
    public Tariff getTariffByName(String name) throws DataPersistenceException {
        return tariffDAO.findByName(name).orElse(null);
    }

    /**
     * Gets all tariffs.
     *
     * @return list of all tariffs
     * @throws DataPersistenceException if data access fails
     */
    public List<Tariff> getAllTariffs() throws DataPersistenceException {
        return tariffDAO.findAll();
    }

    /**
     * Gets all active tariffs.
     *
     * @return list of active tariffs
     * @throws DataPersistenceException if data access fails
     */
    public List<Tariff> getActiveTariffs() throws DataPersistenceException {
        return tariffDAO.findAllActive();
    }

    /**
     * Gets tariffs by meter type.
     *
     * @param meterType the meter type
     * @return list of tariffs for that meter type
     * @throws DataPersistenceException if data access fails
     */
    public List<Tariff> getTariffsByMeterType(MeterType meterType) throws DataPersistenceException {
        return tariffDAO.findByMeterType(meterType);
    }

    /**
     * Gets active tariffs by meter type.
     *
     * @param meterType the meter type
     * @return list of active tariffs for that meter type
     * @throws DataPersistenceException if data access fails
     */
    public List<Tariff> getActiveTariffsByMeterType(MeterType meterType) throws DataPersistenceException {
        return tariffDAO.findActiveByMeterType(meterType);
    }

    /**
     * Updates a tariff.
     *
     * @param tariff the tariff to update
     * @throws DataPersistenceException if data access fails
     */
    public void updateTariff(Tariff tariff) throws DataPersistenceException {
        tariffDAO.update(tariff);
    }

    /**
     * Deactivates a tariff.
     *
     * @param tariffId the tariff ID
     * @throws DataPersistenceException if data access fails
     */
    public void deactivateTariff(String tariffId) throws DataPersistenceException {
        Optional<Tariff> tariffOpt = tariffDAO.findById(tariffId);
        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            tariff.setActive(false);
            tariffDAO.update(tariff);
        }
    }

    /**
     * Reactivates a tariff.
     *
     * @param tariffId the tariff ID
     * @throws DataPersistenceException if data access fails
     */
    public void reactivateTariff(String tariffId) throws DataPersistenceException {
        Optional<Tariff> tariffOpt = tariffDAO.findById(tariffId);
        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            tariff.setActive(true);
            tariffDAO.update(tariff);
        }
    }

    /**
     * Creates a new tariff (generic method for any tariff type).
     *
     * @param tariff the tariff to create
     * @throws DataPersistenceException if data access fails
     */
    public void createTariff(Tariff tariff) throws DataPersistenceException {
        tariffDAO.save(tariff);
    }

    /**
     * Deletes a tariff.
     *
     * @param tariffId the tariff ID
     * @throws DataPersistenceException if data access fails
     */
    public void deleteTariff(String tariffId) throws DataPersistenceException {
        tariffDAO.delete(tariffId);
    }
}

