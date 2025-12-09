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

public class TariffService {

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
                        "Standard Electricity",
                        new BigDecimal("0.45"), // 45p standing charge per day
                        new BigDecimal("28.62") // 28.62p per kWh
                );
                elecTariff.setDescription("Standard variable rate electricity tariff");
                tariffDAO.save(elecTariff);

                ElectricityTariff tieredElec = new ElectricityTariff(
                        "Economy Electricity",
                        new BigDecimal("0.40"),
                        1000, // First 1000 kWh at lower rate
                        new BigDecimal("25.50"),
                        new BigDecimal("30.00")
                );
                tieredElec.setDescription("Tiered pricing - lower rate for first 1000 kWh");
                tariffDAO.save(tieredElec);

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

        ElectricityTariff tariff = new ElectricityTariff(name, standingCharge, tier1Threshold, tier1Rate, tier2Rate);
        tariff.setDescription(description);
        tariffDAO.save(tariff);

        return tariff;
    }

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

