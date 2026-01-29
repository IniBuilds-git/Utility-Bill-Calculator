package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.UtilityBillException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.MeterReading;
import com.utilitybill.model.Meter;
import com.utilitybill.model.MeterType;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.BillingService;
import com.utilitybill.util.AppLogger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import com.utilitybill.model.Tariff;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.ElectricityMeter;
import com.utilitybill.service.TariffService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MeterReadingController {

    private static final String CLASS_NAME = MeterReadingController.class.getName();

    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private TableView<MeterReading> readingsTable;
    @FXML private TableColumn<MeterReading, String> readingIdCol;
    @FXML private TableColumn<MeterReading, String> customerCol;
    @FXML private TableColumn<MeterReading, String> meterIdCol;
    @FXML private TableColumn<MeterReading, String> readingCol;
    @FXML private TableColumn<MeterReading, String> previousCol;
    @FXML private TableColumn<MeterReading, String> consumptionCol;
    @FXML private TableColumn<MeterReading, String> dateCol;
    @FXML private TableColumn<MeterReading, String> statusCol;

    private final BillingService service = BillingService.getInstance();
    private final CustomerService customerService = CustomerService.getInstance();

    @FXML
    public void initialize() {
        readingIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReadingId().substring(0, 8) + "..."));
        customerCol.setCellValueFactory(d -> {
            try {
                Customer c = customerService.getCustomerById(d.getValue().getCustomerId());
                return new SimpleStringProperty(c != null ? c.getAccountNumber() : d.getValue().getCustomerId());
            } catch (DataPersistenceException | com.utilitybill.exception.CustomerNotFoundException e) {
                return new SimpleStringProperty(d.getValue().getCustomerId());
            }
        });
        meterIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMeterId()));
        
        readingCol.setCellValueFactory(d -> {
            MeterReading r = d.getValue();
            if (r.hasDayNightReadings()) {
                return new SimpleStringProperty(String.format("D: %.2f\nN: %.2f", r.getDayReading(), r.getNightReading()));
            }
            return new SimpleStringProperty(String.format("%.2f", r.getReadingValue()));
        });
        
        previousCol.setCellValueFactory(d -> {
             MeterReading r = d.getValue();
             if (r.hasDayNightReadings()) {
                 return new SimpleStringProperty(String.format("D: %.2f\nN: %.2f", r.getPreviousDayReading(), r.getPreviousNightReading()));
             }
             return new SimpleStringProperty(String.format("%.2f", r.getPreviousReadingValue()));
        });
        
        consumptionCol.setCellValueFactory(d -> {
             MeterReading r = d.getValue();
             if (r.hasDayNightReadings()) {
                 return new SimpleStringProperty(String.format("D: %.2f\nN: %.2f\nTotal: %.2f", 
                    r.getDayConsumption(), r.getNightConsumption(), r.getConsumption()));
             }
             return new SimpleStringProperty(String.format("%.2f", r.getConsumption()));
        });
        
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReadingDate().toString()));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isBilled() ? "Billed" : "Pending"));

        refresh();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            refresh();
            return;
        }
        
        try {
            List<MeterReading> filtered = service.getCustomerReadings(query.trim());
            if (filtered.isEmpty()) {
                // Try searching by Meter ID if customer ID search yields nothing
                filtered = service.getMeterReadings(query.trim());
            }
            readingsTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (DataPersistenceException e) {
            AppLogger.error(CLASS_NAME, "Search error: " + e.getMessage(), e);
            showError("Failed to search readings: " + e.getMessage());
        }
    }

   

    @FXML
    private void showAddReadingDialog() {
        Dialog<MeterReading> dialog = new Dialog<>();
        dialog.setTitle("Add Meter Reading");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Customer> customerCombo = new ComboBox<>();
        ComboBox<Meter> meterCombo = new ComboBox<>();
        
        // Single Rate Fields
        Label lblRead = new Label("Reading Value:");
        TextField txtRead = new TextField();
        Label lblOpen = new Label("Opening Value:");
        TextField txtOpen = new TextField(); // Optional explicit opening
        
        // Day/Night Rate Fields
        Label lblDay = new Label("Day Closing:");
        TextField txtDay = new TextField();
        Label lblDayOpen = new Label("Day Opening:");
        TextField txtDayOpen = new TextField();
        
        Label lblNight = new Label("Night Closing:");
        TextField txtNight = new TextField();
        Label lblNightOpen = new Label("Night Opening:");
        TextField txtNightOpen = new TextField();
        
        DatePicker openDate = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker closeDate = new DatePicker(LocalDate.now());

        // Helper to toggle fields
        Runnable updateFields = () -> {
            grid.getChildren().clear();
            grid.addRow(0, new Label("Customer:"), customerCombo);
            grid.addRow(1, new Label("Meter:"), meterCombo);
            grid.addRow(2, new Label("Period Start:"), openDate);
            grid.addRow(3, new Label("Reading Date:"), closeDate);
            
            Customer c = customerCombo.getValue();
            Meter m = meterCombo.getValue();
            boolean showSplit = false;
            
            if (c != null && m != null && m.getMeterType() == MeterType.ELECTRICITY) {
                // Check Tariff
                 try {
                    TariffService ts = TariffService.getInstance();
                    Tariff t = ts.getTariffById(c.getTariffId());
                    if (t instanceof ElectricityTariff) {
                        ElectricityTariff et = (ElectricityTariff) t;
                        if (et.getDayRate() != null && et.getNightRate() != null) {
                            showSplit = true;
                        }
                    }
                    // Also check if meter is ALREADY split
                    if (m instanceof ElectricityMeter && ((ElectricityMeter)m).isDayNightMeter()) {
                        showSplit = true;
                    }
                } catch (Exception e) { /* ignore */ }
            }
            
            if (showSplit) {
                grid.addRow(4, lblDayOpen, txtDayOpen);
                grid.addRow(5, lblDay, txtDay);
                grid.addRow(6, lblNightOpen, txtNightOpen);
                grid.addRow(7, lblNight, txtNight);
                // Pre-fill opening if possible
                if (m instanceof ElectricityMeter) {
                    ElectricityMeter em = (ElectricityMeter) m;
                    txtDayOpen.setText(String.valueOf(em.getCurrentDayReading()));
                    txtNightOpen.setText(String.valueOf(em.getCurrentNightReading()));
                }
            } else {
                grid.addRow(4, lblOpen, txtOpen);
                grid.addRow(5, lblRead, txtRead);
                // Pre-fill opening
                if (m != null) {
                    txtOpen.setText(String.valueOf(m.getCurrentReading()));
                }
            }
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        };

        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
            customerCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Customer c) { return c == null ? "" : c.getFullName(); }
                @Override public Customer fromString(String s) { return null; }
            });
        } catch (DataPersistenceException e) {
            showError("Failed to load customers: " + e.getMessage());
        }

        customerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            meterCombo.getItems().clear();
            if (newVal != null) {
                meterCombo.setItems(FXCollections.observableArrayList(newVal.getMeters()));
                if (!meterCombo.getItems().isEmpty()) {
                    meterCombo.getSelectionModel().selectFirst();
                }
            }
            updateFields.run();
        });
        
        meterCombo.valueProperty().addListener((o, old, newV) -> updateFields.run());

        meterCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Meter m) {
                if (m == null) return "";
                return m.getMeterType() + " [" + m.getMeterId() + "]";
            }
            @Override public Meter fromString(String s) { return null; }
        });

        updateFields.run(); // Initial Layout
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            try {
                Customer selectedCustomer = customerCombo.getValue();
                Meter selectedMeter = meterCombo.getValue();
                if (selectedCustomer == null || selectedMeter == null) {
                    showError("Please select a customer and a meter.");
                    return null;
                }
                
                // Determine if we are in split mode (reuse logic or check visibility)
                boolean isSplit = txtDay.getScene() != null && txtDay.getParent() != null; // fast check if attached
                
                if (isSplit) {
                     double dOpen = Double.parseDouble(txtDayOpen.getText().trim());
                     double dClose = Double.parseDouble(txtDay.getText().trim());
                     double nOpen = Double.parseDouble(txtNightOpen.getText().trim());
                     double nClose = Double.parseDouble(txtNight.getText().trim());
                     
                     return service.recordMeterReading(
                        selectedCustomer.getCustomerId(),
                        selectedMeter.getMeterId(),
                        dClose, dOpen, nClose, nOpen,
                        openDate.getValue(),
                        closeDate.getValue()
                     );
                } else {
                    double value = Double.parseDouble(txtRead.getText().trim());
                    // Check if explicit opening provided
                    if (!txtOpen.getText().trim().isEmpty()) {
                        double openVal = Double.parseDouble(txtOpen.getText().trim());
                         return service.recordMeterReading(
                            selectedCustomer.getCustomerId(),
                            selectedMeter.getMeterId(),
                            value,
                            openVal,
                            openDate.getValue(),
                            closeDate.getValue()
                        );
                    } else {
                         return service.recordMeterReading(
                            selectedCustomer.getCustomerId(),
                            selectedMeter.getMeterId(),
                            value,
                            openDate.getValue(),
                            closeDate.getValue()
                        );
                    }
                }
            } catch (Exception e) {
                showError("Error recording reading: " + e.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(r -> refresh());
    }

    private void refresh() {
        try {
            // Fetch all readings across all customers for the global view
            List<Customer> customers = customerService.getAllCustomers();
            List<MeterReading> allReadings = new ArrayList<>();
            for (Customer c : customers) {
                allReadings.addAll(service.getCustomerReadings(c.getCustomerId()));
            }
            // Sort by date descending
            allReadings.sort((r1, r2) -> r2.getReadingDate().compareTo(r1.getReadingDate()));
            readingsTable.setItems(FXCollections.observableArrayList(allReadings));
        } catch (DataPersistenceException e) {
            AppLogger.error(CLASS_NAME, "Failed to refresh readings: " + e.getMessage(), e);
            showError("Failed to load readings: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
