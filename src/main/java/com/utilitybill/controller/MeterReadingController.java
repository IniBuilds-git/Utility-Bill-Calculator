package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.MeterReading;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeterReadingController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> readingTypeCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<MeterReading> readingsTable;
    @FXML private TableColumn<MeterReading, String> readingIdCol;
    @FXML private TableColumn<MeterReading, String> customerCol;
    @FXML private TableColumn<MeterReading, String> meterIdCol;
    @FXML private TableColumn<MeterReading, String> readingCol;
    @FXML private TableColumn<MeterReading, String> previousCol;
    @FXML private TableColumn<MeterReading, String> consumptionCol;
    @FXML private TableColumn<MeterReading, String> typeCol;
    @FXML private TableColumn<MeterReading, String> dateCol;
    @FXML private TableColumn<MeterReading, String> statusCol;

    private final BillingService billingService;
    private final CustomerService customerService;
    private ObservableList<MeterReading> readingsList;

    public MeterReadingController() {
        this.billingService = BillingService.getInstance();
        this.customerService = CustomerService.getInstance();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        refreshData();
    }

    private void setupTableColumns() {
        readingIdCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getReadingId().substring(0, 8) + "..."));
        
        customerCol.setCellValueFactory(data -> {
            try {
                Customer customer = customerService.getCustomerById(data.getValue().getCustomerId());
                return new SimpleStringProperty(customer.getFullName());
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });
        
        meterIdCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getMeterId()));
        
        readingCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("%.2f", data.getValue().getReadingValue())));
        
        previousCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("%.2f", data.getValue().getPreviousReadingValue())));
        
        consumptionCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("%.2f", data.getValue().getConsumption())));
        
        typeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getReadingType().getDisplayName()));
        
        dateCol.setCellValueFactory(data -> 
            new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getReadingDate())));
        
        statusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isBilled() ? "Billed" : "Pending"));

        // Style the status column
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Billed".equals(item)) {
                        setStyle("-fx-text-fill: #10b981;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b;");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        readingTypeCombo.setItems(FXCollections.observableArrayList(
            "All Types", "Actual", "Estimated", "Smart Meter"
        ));
        readingTypeCombo.setValue("All Types");
    }

    @FXML
    public void refreshData() {
        try {
            List<MeterReading> allReadings = new ArrayList<>();

            for (Customer customer : customerService.getAllCustomers()) {
                allReadings.addAll(billingService.getCustomerReadings(customer.getCustomerId()));
            }
            
            readingsList = FXCollections.observableArrayList(allReadings);
            readingsTable.setItems(readingsList);
        } catch (DataPersistenceException e) {
            showError("Failed to load readings: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String typeFilter = readingTypeCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            List<MeterReading> allReadings = new ArrayList<>();
            for (Customer customer : customerService.getAllCustomers()) {
                allReadings.addAll(billingService.getCustomerReadings(customer.getCustomerId()));
            }

            List<MeterReading> filtered = allReadings.stream()
                .filter(r -> {
                    if (!searchText.isEmpty()) {
                        try {
                            Customer c = customerService.getCustomerById(r.getCustomerId());
                            if (!c.getFullName().toLowerCase().contains(searchText) &&
                                !r.getMeterId().toLowerCase().contains(searchText)) {
                                return false;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    if (typeFilter != null && !"All Types".equals(typeFilter)) {
                        String type = r.getReadingType().getDisplayName();
                        if (!type.toLowerCase().contains(typeFilter.toLowerCase())) {
                            return false;
                        }
                    }

                    if (fromDate != null && r.getReadingDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && r.getReadingDate().isAfter(toDate)) {
                        return false;
                    }
                    
                    return true;
                })
                .toList();

            readingsList = FXCollections.observableArrayList(filtered);
            readingsTable.setItems(readingsList);
        } catch (DataPersistenceException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void showAddReadingDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitybill/view/meter-reading-dialog.fxml"));
            Parent root = loader.load();
            MeterReadingDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Record New Meter Reading");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (IOException e) {
            showError("Failed to open dialog: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

