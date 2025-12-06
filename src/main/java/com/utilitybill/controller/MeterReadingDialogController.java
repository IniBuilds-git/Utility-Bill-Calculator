package com.utilitybill.controller;

import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InvalidMeterReadingException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Meter;
import com.utilitybill.model.MeterReading;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the meter reading add dialog.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class MeterReadingDialogController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Meter> meterCombo;
    @FXML private TextField readingField;
    @FXML private Label previousReadingLabel;
    @FXML private ComboBox<MeterReading.ReadingType> readingTypeCombo;
    @FXML private DatePicker readingDatePicker;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;

    private final CustomerService customerService;
    private final BillingService billingService;
    private Stage dialogStage;
    private boolean saved = false;

    public MeterReadingDialogController() {
        this.customerService = CustomerService.getInstance();
        this.billingService = BillingService.getInstance();
    }

    @FXML
    public void initialize() {
        setupCustomerCombo();
        setupReadingTypeCombo();
        readingDatePicker.setValue(LocalDate.now());
        hideError();

        // When customer changes, load their meters
        customerCombo.setOnAction(e -> loadMetersForCustomer());
        
        // When meter changes, show previous reading
        meterCombo.setOnAction(e -> showPreviousReading());
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

    private void setupReadingTypeCombo() {
        readingTypeCombo.setItems(FXCollections.observableArrayList(
            MeterReading.ReadingType.ACTUAL,
            MeterReading.ReadingType.ESTIMATED,
            MeterReading.ReadingType.SMART
        ));
        readingTypeCombo.setValue(MeterReading.ReadingType.ACTUAL);
        
        readingTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(MeterReading.ReadingType type) {
                return type == null ? "" : type.getDisplayName();
            }

            @Override
            public MeterReading.ReadingType fromString(String string) {
                return null;
            }
        });
    }

    private void loadMetersForCustomer() {
        Customer customer = customerCombo.getValue();
        if (customer == null) {
            meterCombo.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Meter> meters = customer.getMeters();
        meterCombo.setItems(FXCollections.observableArrayList(meters));
        
        meterCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Meter meter) {
                return meter == null ? "" : meter.getMeterId() + " (" + meter.getMeterType().getDisplayName() + ")";
            }

            @Override
            public Meter fromString(String string) {
                return null;
            }
        });

        if (!meters.isEmpty()) {
            meterCombo.setValue(meters.get(0));
            showPreviousReading();
        }
    }

    private void showPreviousReading() {
        Meter meter = meterCombo.getValue();
        if (meter == null) {
            previousReadingLabel.setText("N/A");
            return;
        }

        try {
            MeterReading lastReading = billingService.getLatestReading(meter.getMeterId());
            if (lastReading != null) {
                previousReadingLabel.setText(String.format("%.2f kWh", lastReading.getReadingValue()));
            } else {
                previousReadingLabel.setText(String.format("%.2f kWh (meter current)", meter.getCurrentReading()));
            }
        } catch (DataPersistenceException e) {
            previousReadingLabel.setText("Error loading");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSave() {
        hideError();

        // Validate
        if (customerCombo.getValue() == null) {
            showError("Please select a customer");
            return;
        }
        if (meterCombo.getValue() == null) {
            showError("Please select a meter");
            return;
        }
        if (readingField.getText().trim().isEmpty()) {
            showError("Please enter a reading value");
            return;
        }
        if (readingDatePicker.getValue() == null) {
            showError("Please select a date");
            return;
        }

        double readingValue;
        try {
            readingValue = Double.parseDouble(readingField.getText().trim());
            if (readingValue < 0) {
                showError("Reading value cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
            return;
        }

        try {
            Customer customer = customerCombo.getValue();
            Meter meter = meterCombo.getValue();
            
            MeterReading reading = billingService.recordMeterReading(
                customer.getCustomerId(),
                meter.getMeterId(),
                readingValue,
                readingDatePicker.getValue(),
                readingTypeCombo.getValue()
            );

            if (notesArea.getText() != null && !notesArea.getText().trim().isEmpty()) {
                reading.setNotes(notesArea.getText().trim());
            }

            saved = true;
            if (dialogStage != null) {
                dialogStage.close();
            }

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

