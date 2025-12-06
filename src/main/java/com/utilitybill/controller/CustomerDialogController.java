package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.DuplicateAccountException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.*;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.TariffService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the customer add/edit dialog.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class CustomerDialogController {

    @FXML private Label dialogTitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField houseNumberField;
    @FXML private TextField streetField;
    @FXML private TextField cityField;
    @FXML private TextField countyField;
    @FXML private TextField postcodeField;
    @FXML private ComboBox<MeterType> meterTypeCombo;
    @FXML private ComboBox<Tariff> tariffCombo;
    @FXML private ComboBox<Customer.CustomerType> customerTypeCombo;
    @FXML private Label errorLabel;

    /** Services */
    private final CustomerService customerService;
    private final TariffService tariffService;

    /** Current mode */
    private Mode mode = Mode.ADD;

    /** Customer being edited (null for add mode) */
    private Customer customer;

    /** Dialog stage reference */
    private Stage dialogStage;

    /** Dialog result */
    private boolean saved = false;

    /**
     * Mode enum for the dialog.
     */
    public enum Mode {
        ADD, EDIT
    }

    /**
     * Constructs a new CustomerDialogController.
     */
    public CustomerDialogController() {
        this.customerService = CustomerService.getInstance();
        this.tariffService = TariffService.getInstance();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Setup meter type combo
        meterTypeCombo.setItems(FXCollections.observableArrayList(MeterType.values()));
        meterTypeCombo.setValue(MeterType.ELECTRICITY);

        // Setup customer type combo
        customerTypeCombo.setItems(FXCollections.observableArrayList(Customer.CustomerType.values()));
        customerTypeCombo.setValue(Customer.CustomerType.RESIDENTIAL);

        // Load tariffs
        loadTariffs();

        // Update tariffs when meter type changes
        meterTypeCombo.setOnAction(e -> loadTariffs());

        // Setup combo box display
        tariffCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tariff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        tariffCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tariff item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Hide error initially
        hideError();
    }

    /**
     * Loads tariffs based on selected meter type.
     */
    private void loadTariffs() {
        try {
            MeterType meterType = meterTypeCombo.getValue();
            List<Tariff> tariffs = tariffService.getActiveTariffsByMeterType(meterType);
            tariffCombo.setItems(FXCollections.observableArrayList(tariffs));
            if (!tariffs.isEmpty()) {
                tariffCombo.setValue(tariffs.get(0));
            }
        } catch (DataPersistenceException e) {
            showError("Failed to load tariffs");
        }
    }

    /**
     * Sets the dialog mode.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        dialogTitle.setText(mode == Mode.ADD ? "Add New Customer" : "Edit Customer");
    }

    /**
     * Sets the customer to edit.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            populateFields(customer);
        }
    }

    /**
     * Populates fields with customer data.
     */
    private void populateFields(Customer customer) {
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());

        if (customer.getServiceAddress() != null) {
            Address address = customer.getServiceAddress();
            houseNumberField.setText(address.getHouseNumber());
            streetField.setText(address.getStreet());
            cityField.setText(address.getCity());
            countyField.setText(address.getCounty());
            postcodeField.setText(address.getPostcode());
        }

        customerTypeCombo.setValue(customer.getCustomerType());

        // Disable meter type for edit mode
        if (!customer.getMeters().isEmpty()) {
            meterTypeCombo.setValue(customer.getMeters().get(0).getMeterType());
            meterTypeCombo.setDisable(true);
        }
    }

    /**
     * Sets the dialog stage reference.
     *
     * @param dialogStage the stage for this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        hideError();

        // Validate fields
        if (!validateFields()) {
            return;
        }

        try {
            if (mode == Mode.ADD) {
                createCustomer();
            } else {
                updateCustomer();
            }
            saved = true;

            // Close the dialog
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (DuplicateAccountException e) {
            showError(e.getMessage());
        } catch (DataPersistenceException e) {
            showError("Failed to save customer: " + e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Validates the form fields.
     */
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("First name is required\n");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("Last name is required\n");
        }
        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        }
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Phone number is required\n");
        }
        if (houseNumberField.getText().trim().isEmpty()) {
            errors.append("House number is required\n");
        }
        if (streetField.getText().trim().isEmpty()) {
            errors.append("Street is required\n");
        }
        if (cityField.getText().trim().isEmpty()) {
            errors.append("City is required\n");
        }
        if (postcodeField.getText().trim().isEmpty()) {
            errors.append("Postcode is required\n");
        }
        if (tariffCombo.getValue() == null) {
            errors.append("Please select a tariff\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString().trim());
            return false;
        }
        return true;
    }

    /**
     * Creates a new customer.
     */
    private void createCustomer() throws ValidationException, DuplicateAccountException, DataPersistenceException {
        Address address = new Address(
                houseNumberField.getText().trim(),
                streetField.getText().trim(),
                cityField.getText().trim(),
                countyField.getText().trim(),
                postcodeField.getText().trim().toUpperCase()
        );

        Customer newCustomer = customerService.createCustomer(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                address,
                meterTypeCombo.getValue(),
                tariffCombo.getValue() != null ? tariffCombo.getValue().getTariffId() : null
        );

        newCustomer.setCustomerType(customerTypeCombo.getValue());
        try {
            customerService.updateCustomer(newCustomer);
        } catch (com.utilitybill.exception.CustomerNotFoundException e) {
            // This shouldn't happen for newly created customer, but handle gracefully
            throw new DataPersistenceException("Failed to update new customer", "", 
                    com.utilitybill.exception.DataPersistenceException.Operation.WRITE, e);
        }
    }

    /**
     * Updates an existing customer.
     */
    private void updateCustomer() throws ValidationException, DataPersistenceException {
        customer.setFirstName(firstNameField.getText().trim());
        customer.setLastName(lastNameField.getText().trim());
        customer.setEmail(emailField.getText().trim());
        customer.setPhone(phoneField.getText().trim());

        Address address = customer.getServiceAddress();
        if (address == null) {
            address = new Address();
        }
        address.setHouseNumber(houseNumberField.getText().trim());
        address.setStreet(streetField.getText().trim());
        address.setCity(cityField.getText().trim());
        address.setCounty(countyField.getText().trim());
        address.setPostcode(postcodeField.getText().trim().toUpperCase());
        customer.setServiceAddress(address);

        customer.setCustomerType(customerTypeCombo.getValue());

        if (tariffCombo.getValue() != null) {
            customer.setTariffId(tariffCombo.getValue().getTariffId());
        }

        try {
            customerService.updateCustomer(customer);
        } catch (com.utilitybill.exception.CustomerNotFoundException e) {
            throw new DataPersistenceException("Customer not found", "", 
                    com.utilitybill.exception.DataPersistenceException.Operation.WRITE, e);
        }
    }

    /**
     * Handles the cancel button action.
     */
    @FXML
    private void handleCancel() {
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides the error message.
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Returns whether the dialog was saved.
     */
    public boolean isSaved() {
        return saved;
    }
}

