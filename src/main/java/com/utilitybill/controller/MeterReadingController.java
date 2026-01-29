package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.UtilityBillException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.MeterReadingRecord;
import com.utilitybill.model.MeterType;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.MeterReadingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;

public class MeterReadingController {

    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private TableView<MeterReadingRecord> readingsTable;
    @FXML private TableColumn<MeterReadingRecord, String> readingIdCol;
    @FXML private TableColumn<MeterReadingRecord, String> customerCol;
    @FXML private TableColumn<MeterReadingRecord, String> meterIdCol;
    @FXML private TableColumn<MeterReadingRecord, String> readingCol;
    @FXML private TableColumn<MeterReadingRecord, String> previousCol;
    @FXML private TableColumn<MeterReadingRecord, String> consumptionCol;
    @FXML private TableColumn<MeterReadingRecord, String> dateCol;
    @FXML private TableColumn<MeterReadingRecord, String> statusCol;

    private final MeterReadingService service = MeterReadingService.getInstance();
    private final CustomerService customerService = CustomerService.getInstance();

    @FXML
    public void initialize() {
        readingIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReadingId()));
        customerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountNumber()));
        meterIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMeterType().toString()));
        readingCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getClosingRead())));
        previousCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getOpeningRead())));
        consumptionCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getKWh())));
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClosingDate().toString()));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty("Billed")); // Simplified status

        refresh();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            refresh();
            return;
        }
        List<MeterReadingRecord> filtered = service.getReadingsForCustomer(query.trim());
        readingsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleImport() {
        // Placeholder for import logic
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Import");
        info.setHeaderText("Import Meter Readings");
        info.setContentText("Import functionality is not yet implemented.");
        info.showAndWait();
    }

    @FXML
    private void showAddReadingDialog() {
        Dialog<MeterReadingRecord> dialog = new Dialog<>();
        dialog.setTitle("Add Meter Reading");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Customer> customerCombo = new ComboBox<>();
        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
            customerCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Customer c) {
                    if (c == null) return "";
                    return c.getFullName() + " (" + c.getAccountNumber() + ")";
                }
                @Override public Customer fromString(String s) { return null; }
            });
        } catch (DataPersistenceException e) {
            showError("Failed to load customers: " + e.getMessage());
        }

        ComboBox<MeterType> meterTypeCombo = new ComboBox<>();
        
        customerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            meterTypeCombo.getItems().clear();
            if (newVal != null) {
                if (newVal.hasElectricityMeter()) {
                    meterTypeCombo.getItems().add(MeterType.ELECTRICITY);
                }
                if (newVal.hasGasMeter()) {
                    meterTypeCombo.getItems().add(MeterType.GAS);
                }
                if (!meterTypeCombo.getItems().isEmpty()) {
                    meterTypeCombo.getSelectionModel().selectFirst();
                }
            }
        });

        DatePicker openDate = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker closeDate = new DatePicker(LocalDate.now());
        TextField openRead = new TextField();
        TextField closeRead = new TextField();

        grid.addRow(0, new Label("Customer:"), customerCombo);
        grid.addRow(1, new Label("Meter Type:"), meterTypeCombo);
        grid.addRow(2, new Label("Opening Date:"), openDate);
        grid.addRow(3, new Label("Closing Date:"), closeDate);
        grid.addRow(4, new Label("Opening Read:"), openRead);
        grid.addRow(5, new Label("Closing Read:"), closeRead);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            try {
                Customer selectedCustomer = customerCombo.getValue();
                if (selectedCustomer == null) {
                    showError("Please select a customer.");
                    return null;
                }

                MeterReadingRecord r = new MeterReadingRecord();
                r.setAccountNumber(selectedCustomer.getAccountNumber());
                r.setMeterType(meterTypeCombo.getValue());
                r.setOpeningDate(openDate.getValue());
                r.setClosingDate(closeDate.getValue());
                r.setOpeningRead(Double.parseDouble(openRead.getText().trim()));
                r.setClosingRead(Double.parseDouble(closeRead.getText().trim()));
                
                if (r.getMeterType() == MeterType.GAS) {
                    r.setGasImperial(false);
                    r.setCorrectionFactor(1.02264);
                    r.setCalorificValue(39.4);
                }

                r.recalc();
                return r;
            } catch (Exception e) {
                showError("Invalid input. Please check numbers and dates.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(r -> {
            try {
                service.addReading(r);
                refresh();
            } catch (UtilityBillException e) {
                showError(e.getMessage());
            }
        });
    }

    private void refresh() {
        readingsTable.setItems(FXCollections.observableArrayList());
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
