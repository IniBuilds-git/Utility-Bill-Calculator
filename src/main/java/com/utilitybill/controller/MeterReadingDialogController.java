package com.utilitybill.controller;

import com.utilitybill.dao.TariffDAO;
import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidMeterReadingException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.ElectricityMeter;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.Meter;
import com.utilitybill.model.MeterReading;
import com.utilitybill.model.Tariff;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class MeterReadingDialogController {

    @FXML
    private ComboBox<Customer> customerCombo;
    @FXML
    private Label selectedCustomerLabel;
    
    @FXML
    private ListView<Meter> meterList;
    @FXML
    private Label selectedMeterLabel;
    
    @FXML
    private VBox standardReadingBox;
    @FXML
    private VBox dayNightReadingBox;
    @FXML
    private TextField dayReadingField; // Closing
    @FXML
    private TextField dayOpeningField; // Opening
    @FXML
    private TextField nightReadingField; // Closing
    @FXML
    private TextField nightOpeningField; // Opening

    @FXML
    private TextField readingField; // Closing
    @FXML
    private TextField openingReadingField; // Opening
    @FXML
    private DatePicker periodStartDatePicker;
    @FXML
    private DatePicker readingDatePicker;

    @FXML
    private VBox previewBox;
    @FXML
    private VBox gasPreviewBox;
    @FXML
    private Label gasUnitsLabel;
    @FXML
    private Label gasM3Label;
    @FXML
    private Label gasKwhLabel;
    @FXML
    private VBox elecPreviewBox;
    @FXML
    private Label elecDayUnitsLabel;
    @FXML
    private Label elecNightUnitsLabel;
    @FXML
    private Label elecKwhLabel;

    @FXML
    private Label errorLabel;
    @FXML
    private Label previousReadingLabel;

    private final CustomerService customerService;
    private final BillingService billingService;
    private final TariffDAO tariffDAO;
    private Stage dialogStage;
    private boolean saved = false;

    // Contextual State
    private Customer preSelectedCustomer;
    private Meter preSelectedMeter;

    public MeterReadingDialogController() {
        this.customerService = CustomerService.getInstance();
        this.billingService = BillingService.getInstance();
        this.tariffDAO = TariffDAO.getInstance();
    }

    @FXML
    public void initialize() {
        setupCustomerCombo();
        periodStartDatePicker.setValue(LocalDate.now().minusMonths(1));
        readingDatePicker.setValue(LocalDate.now());
        hideError();

        // Standard listeners for manual mode
        customerCombo.setOnAction(e -> loadMetersForCustomer());
        
        meterList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Meter meter, boolean empty) {
                super.updateItem(meter, empty);
                if (empty || meter == null) {
                    setText(null);
                } else {
                    setText(meter.getMeterId() + " (" + meter.getMeterType().getDisplayName() + ")");
                }
            }
        });

        meterList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                prefillOpeningReadings(newVal);
                updateInputVisibility(newVal);
                updatePreview();
            }
        });

        // Add listeners for real-time calculation
        readingField.textProperty().addListener((obs, old, nw) -> updatePreview());
        openingReadingField.textProperty().addListener((obs, old, nw) -> updatePreview());
        dayReadingField.textProperty().addListener((obs, old, nw) -> updatePreview());
        dayOpeningField.textProperty().addListener((obs, old, nw) -> updatePreview());
        nightReadingField.textProperty().addListener((obs, old, nw) -> updatePreview());
        nightOpeningField.textProperty().addListener((obs, old, nw) -> updatePreview());
    }

    private void updatePreview() {
        Meter meter = getSelectedMeter();
        if (meter == null) {
            previewBox.setVisible(false);
            previewBox.setManaged(false);
            return;
        }

        previewBox.setVisible(true);
        previewBox.setManaged(true);

        try {
            if (meter.getMeterType() == com.utilitybill.model.MeterType.GAS) {
                gasPreviewBox.setVisible(true);
                gasPreviewBox.setManaged(true);
                elecPreviewBox.setVisible(false);
                elecPreviewBox.setManaged(false);

                double closing = parseDouble(readingField.getText());
                double opening = parseDouble(openingReadingField.getText());
                double units = Math.max(0, closing - opening);
                
                // Get conversion factors from GasTariff or defaults
                double unitsToM3 = com.utilitybill.model.GasTariff.IMPERIAL_TO_METRIC;
                double correctionFactor = com.utilitybill.model.GasTariff.DEFAULT_CORRECTION_FACTOR;
                double calorificValue = com.utilitybill.model.GasTariff.DEFAULT_CALORIFIC_VALUE;
                
                boolean isImperial = meter instanceof com.utilitybill.model.GasMeter && ((com.utilitybill.model.GasMeter) meter).isImperialMeter();
                double m3 = isImperial ? units * unitsToM3 : units;
                double kwh = (m3 * correctionFactor * calorificValue) / 3.6;

                gasUnitsLabel.setText(String.format("%.2f", units));
                gasM3Label.setText(String.format("%.2f", m3));
                gasKwhLabel.setText(String.format("%.2f kWh", kwh));
            } else {
                gasPreviewBox.setVisible(false);
                gasPreviewBox.setManaged(false);
                elecPreviewBox.setVisible(true);
                elecPreviewBox.setManaged(true);

                if (isDayNightCapable(meter)) {
                    double dayClosing = parseDouble(dayReadingField.getText());
                    double dayOpening = parseDouble(dayOpeningField.getText());
                    double nightClosing = parseDouble(nightReadingField.getText());
                    double nightOpening = parseDouble(nightOpeningField.getText());
                    
                    double dayUnits = Math.max(0, dayClosing - dayOpening);
                    double nightUnits = Math.max(0, nightClosing - nightOpening);
                    double totalKwh = dayUnits + nightUnits;

                    elecDayUnitsLabel.setText(String.format("%.2f", dayUnits));
                    elecNightUnitsLabel.setText(String.format("%.2f", nightUnits));
                    elecKwhLabel.setText(String.format("%.2f kWh", totalKwh));
                } else {
                    double closing = parseDouble(readingField.getText());
                    double opening = parseDouble(openingReadingField.getText());
                    double usage = Math.max(0, closing - opening);

                    elecDayUnitsLabel.setText(String.format("%.2f", usage));
                    elecNightUnitsLabel.setText("0.00");
                    elecKwhLabel.setText(String.format("%.2f kWh", usage));
                }
            }
        } catch (NumberFormatException e) {
            // Partial input, clear totals but keep boxes visible
            gasUnitsLabel.setText("0.00");
            gasM3Label.setText("0.00");
            gasKwhLabel.setText("0.00 kWh");
            elecDayUnitsLabel.setText("0.00");
            elecNightUnitsLabel.setText("0.00");
            elecKwhLabel.setText("0.00 kWh");
        }
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Meter getSelectedMeter() {
        return preSelectedMeter != null ? preSelectedMeter : meterList.getSelectionModel().getSelectedItem();
    }

    /**
     * Sets a pre-selected customer, locking the dialog to this customer context.
     */
    public void setCustomer(Customer customer) {
        this.preSelectedCustomer = customer;
        if (customer != null) {
            // Contextual Mode: Hide combo, show label
            customerCombo.setVisible(false);
            customerCombo.setManaged(false);
            
            selectedCustomerLabel.setText(customer.getAccountNumber() + " - " + customer.getFullName());
            selectedCustomerLabel.setVisible(true);
            selectedCustomerLabel.setManaged(true);
            
            // Auto-load meters for this customer (hidden list populated for logic, or we manipulate list directly)
            loadMetersForCustomer(customer);
        }
    }

    /**
     * Sets a pre-selected meter, locking the dialog to this meter context.
     * Must call setCustomer first or ensure customer context is known.
     */
    public void setMeter(Meter meter) {
        this.preSelectedMeter = meter;
        if (meter != null) {
            // Contextual Mode: Hide list, show label
            meterList.setVisible(false);
            meterList.setManaged(false);
            
            selectedMeterLabel.setText(meter.getMeterId() + " (" + meter.getMeterType().getDisplayName() + ")");
            selectedMeterLabel.setVisible(true);
            selectedMeterLabel.setManaged(true);
            
            // Select in list (even if hidden) to trigger existing listeners logic or manually trigger
            meterList.getSelectionModel().select(meter);
            // Force trigger capability checks and UI updates just in case listener didn't fire due to timing
            prefillOpeningReadings(meter);
            updateInputVisibility(meter);
        }
    }

    private void updateInputVisibility(Meter meter) {
        boolean isDayNight = isDayNightCapable(meter);

        if (isDayNight) {
            standardReadingBox.setVisible(false);
            standardReadingBox.setManaged(false);
            dayNightReadingBox.setVisible(true);
            dayNightReadingBox.setManaged(true);
        } else {
            standardReadingBox.setVisible(true);
            standardReadingBox.setManaged(true);
            dayNightReadingBox.setVisible(false);
            dayNightReadingBox.setManaged(false);
        }
    }

    private void setupCustomerCombo() {
        try {
            List<Customer> customers = customerService.getActiveCustomers();
            customerCombo.setItems(FXCollections.observableArrayList(customers));

            customerCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Customer customer) {
                    return customer == null ? "" : customer.getAccountNumber() + " - " + customer.getFullName();
                }

                @Override
                public Customer fromString(String string) {
                    return null;
                }
            });
        } catch (DataPersistenceException e) {
            showError("Failed to load customers");
        }
    }

    private void loadMetersForCustomer() {
        loadMetersForCustomer(customerCombo.getValue());
    }

    private void loadMetersForCustomer(Customer customer) {
        if (customer == null) {
            meterList.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Meter> meters = customer.getMeters();
        meterList.setItems(FXCollections.observableArrayList(meters));

        if (!meters.isEmpty() && preSelectedMeter == null) {
            // In manual mode, maybe select first? Or let user select.
             meterList.getSelectionModel().selectFirst();
        }
    }

    private void prefillOpeningReadings(Meter meter) {
        if (meter == null) {
             // clear fields
             if (openingReadingField != null) openingReadingField.setText("");
             if (dayOpeningField != null) dayOpeningField.setText("");
             if (nightOpeningField != null) nightOpeningField.setText("");
             if (previousReadingLabel != null) previousReadingLabel.setText("N/A");
            return;
        }

        try {
            MeterReading lastReading = billingService.getLatestReading(meter.getMeterId());
            boolean isDayNight = isDayNightCapable(meter);

            // Standard
            double previousVal = (lastReading != null) ? lastReading.getReadingValue() : meter.getCurrentReading();

            // Update the previous reading label for user reference
            if (previousReadingLabel != null) {
                if (isDayNight) {
                    double prevDay, prevNight;
                    if (lastReading != null && lastReading.hasDayNightReadings()) {
                        prevDay = lastReading.getDayReading();
                        prevNight = lastReading.getNightReading();
                    } else {
                        ElectricityMeter em = (ElectricityMeter) meter;
                        prevDay = em.getCurrentDayReading();
                        prevNight = em.getCurrentNightReading();
                    }
                    previousReadingLabel.setText(String.format("Day: %.3f, Night: %.3f", prevDay, prevNight));
                } else {
                    previousReadingLabel.setText(String.format("%.3f", previousVal));
                }
            }

            if (!isDayNight) {
                if (openingReadingField != null) openingReadingField.setText(String.valueOf(previousVal));
            }

            // Day/Night special handling
            if (isDayNight) {
                double prevDay, prevNight;
                if (lastReading != null && lastReading.hasDayNightReadings()) {
                    prevDay = lastReading.getDayReading();
                    prevNight = lastReading.getNightReading();
                } else {
                    ElectricityMeter em = (ElectricityMeter) meter;
                    prevDay = em.getCurrentDayReading();
                    prevNight = em.getCurrentNightReading();
                }

                if (dayOpeningField != null) dayOpeningField.setText(String.valueOf(prevDay));
                if (nightOpeningField != null) nightOpeningField.setText(String.valueOf(prevNight));
            }
        } catch (DataPersistenceException e) {
            // Error loading previous, just leave empty or use default
            if (previousReadingLabel != null) previousReadingLabel.setText("N/A");
        }
    }

    private boolean isDayNightCapable(Meter meter) {
        if (meter instanceof ElectricityMeter) {
            if (((ElectricityMeter) meter).isDayNightMeter()) {
                return true;
            }
            // Fallback: Check tariff
            Customer customer = preSelectedCustomer != null ? preSelectedCustomer : customerCombo.getValue();
            if (customer != null && customer.getTariffId() != null) {
                 try {
                     java.util.Optional<Tariff> tariffOpt = tariffDAO.findById(customer.getTariffId());
                     if (tariffOpt.isPresent()) {
                         Tariff tariff = tariffOpt.get();
                         if (tariff instanceof ElectricityTariff) {
                             return ((ElectricityTariff) tariff).getNightRate() != null;
                         }
                     }
                 } catch (Exception e) {
                     // ignore
                 }
            }
        }
        return false;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSave() {
        hideError();
        
        Customer customer = preSelectedCustomer != null ? preSelectedCustomer : customerCombo.getValue();
        Meter meter = preSelectedMeter != null ? preSelectedMeter : meterList.getSelectionModel().getSelectedItem();

        if (customer == null) {
            showError("Please select a customer");
            return;
        }
        if (meter == null) {
            showError("Please select a meter");
            return;
        }
        if (periodStartDatePicker.getValue() == null) {
            showError("Please enter the Opening Date (Period Start)");
            return;
        }
        if (readingDatePicker.getValue() == null) {
            showError("Please enter the Closing Date (Reading Date)");
            return;
        }

        if (readingDatePicker.getValue().isBefore(periodStartDatePicker.getValue())) {
            showError("Closing Date cannot be before Opening Date");
            return;
        }

        try {
            boolean isDayNight = isDayNightCapable(meter);
            // ... rest is same
            if (isDayNight) {
                if (dayReadingField.getText().trim().isEmpty() || nightReadingField.getText().trim().isEmpty() ||
                    dayOpeningField.getText().trim().isEmpty() || nightOpeningField.getText().trim().isEmpty()) {
                    showError("Please enter all Day and Night readings (Opening and Closing)");
                    return;
                }
                
                double dayVal = Double.parseDouble(dayReadingField.getText().trim());
                double dayOpen = Double.parseDouble(dayOpeningField.getText().trim());
                double nightVal = Double.parseDouble(nightReadingField.getText().trim());
                double nightOpen = Double.parseDouble(nightOpeningField.getText().trim());
                
                billingService.recordMeterReading(
                    customer.getCustomerId(),
                    meter.getMeterId(),
                    dayVal,
                    dayOpen,
                    nightVal,
                    nightOpen,
                    periodStartDatePicker.getValue(),
                    readingDatePicker.getValue());
                    
            } else {
                if (readingField.getText().trim().isEmpty() || openingReadingField.getText().trim().isEmpty()) {
                    showError("Please enter Opening and Closing readings");
                    return;
                }
                
                double readingValue = Double.parseDouble(readingField.getText().trim());
                double openingValue = Double.parseDouble(openingReadingField.getText().trim());

                if (readingValue < 0 || openingValue < 0) {
                     showError("Readings cannot be negative");
                     return;
                }
                
                billingService.recordMeterReading(
                        customer.getCustomerId(),
                        meter.getMeterId(),
                        readingValue,
                        openingValue,
                        periodStartDatePicker.getValue(),
                        readingDatePicker.getValue());
            }

            saved = true;
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        } catch (InvalidMeterReadingException e) {
            showError(e.getMessage());
        } catch (CustomerNotFoundException e) {
            showError("Customer not found: " + e.getMessage());
        } catch (DataPersistenceException e) {
            showError("Failed to save reading: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public boolean isSaved() {
        return saved;
    }
}
