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

public class CustomerDialogController {

    @FXML
    private Label dialogTitle;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField houseNumberField;
    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField countyField;
    @FXML
    private TextField postcodeField;
    @FXML
    private ComboBox<MeterType> meterTypeCombo;
    @FXML
    private ComboBox<Tariff> tariffCombo;

    @FXML
    private Label errorLabel;

    private final CustomerService customerService;
    private final TariffService tariffService;
    private Mode mode = Mode.ADD;
    private Customer customer;
    private Stage dialogStage;
    private boolean saved = false;

    public enum Mode {
        ADD, EDIT
    }

    public CustomerDialogController() {
        this.customerService = CustomerService.getInstance();
        this.tariffService = TariffService.getInstance();
    }

    @FXML
    public void initialize() {
        meterTypeCombo.setItems(FXCollections.observableArrayList(MeterType.values()));
        meterTypeCombo.setValue(MeterType.ELECTRICITY);



        loadTariffs();
        meterTypeCombo.setOnAction(e -> loadTariffs());

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

        hideError();
    }

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

    public void setMode(Mode mode) {
        this.mode = mode;
        dialogTitle.setText(mode == Mode.ADD ? "Add New Customer" : "Edit Customer");
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            populateFields(customer);
        }
    }

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



        if (!customer.getMeters().isEmpty()) {
            meterTypeCombo.setValue(customer.getMeters().get(0).getMeterType());
            meterTypeCombo.setDisable(true);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSave() {
        hideError();

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

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (getText(firstNameField).isEmpty()) {
            errors.append("First name is required\n");
        }
        if (getText(lastNameField).isEmpty()) {
            errors.append("Last name is required\n");
        }
        if (getText(emailField).isEmpty()) {
            errors.append("Email is required\n");
        }
        if (getText(phoneField).isEmpty()) {
            errors.append("Phone number is required\n");
        }
        if (getText(houseNumberField).isEmpty()) {
            errors.append("House number is required\n");
        }
        if (getText(streetField).isEmpty()) {
            errors.append("Street is required\n");
        }
        if (getText(cityField).isEmpty()) {
            errors.append("City is required\n");
        }
        if (getText(postcodeField).isEmpty()) {
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

    private String getText(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }

    private void createCustomer() throws ValidationException, DuplicateAccountException, DataPersistenceException {
        Address address = new Address(
                getText(houseNumberField),
                getText(streetField),
                getText(cityField),
                getText(countyField),
                getText(postcodeField).toUpperCase());

        Customer newCustomer = customerService.createCustomer(
                getText(firstNameField),
                getText(lastNameField),
                getText(emailField),
                getText(phoneField),
                address,
                meterTypeCombo.getValue(),
                tariffCombo.getValue() != null ? tariffCombo.getValue().getTariffId() : null);


        try {
            customerService.updateCustomer(newCustomer);
        } catch (com.utilitybill.exception.CustomerNotFoundException e) {
            throw new DataPersistenceException("Failed to update new customer", "",
                    com.utilitybill.exception.DataPersistenceException.Operation.WRITE, e);
        }
    }

    private void updateCustomer() throws ValidationException, DataPersistenceException {
        customer.setFirstName(getText(firstNameField));
        customer.setLastName(getText(lastNameField));
        customer.setEmail(getText(emailField));
        customer.setPhone(getText(phoneField));

        Address address = customer.getServiceAddress();
        if (address == null) {
            address = new Address();
        }
        address.setHouseNumber(getText(houseNumberField));
        address.setStreet(getText(streetField));
        address.setCity(getText(cityField));
        address.setCounty(getText(countyField));
        address.setPostcode(getText(postcodeField).toUpperCase());
        customer.setServiceAddress(address);



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
