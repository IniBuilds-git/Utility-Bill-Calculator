package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.GasTariff;
import com.utilitybill.model.MeterType;
import com.utilitybill.model.Tariff;
import com.utilitybill.service.TariffService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for tariff management view.
 * Handles CRUD operations for electricity and gas tariffs.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class TariffController {

    // Electricity table
    @FXML private TableView<Tariff> electricityTable;
    @FXML private TableColumn<Tariff, String> elecNameCol;
    @FXML private TableColumn<Tariff, String> elecTypeCol;
    @FXML private TableColumn<Tariff, String> elecStandingCol;
    @FXML private TableColumn<Tariff, String> elecUnitCol;
    @FXML private TableColumn<Tariff, String> elecTier1Col;
    @FXML private TableColumn<Tariff, String> elecTier2Col;
    @FXML private TableColumn<Tariff, String> elecStatusCol;
    @FXML private TableColumn<Tariff, Void> elecActionsCol;

    // Gas table
    @FXML private TableView<Tariff> gasTable;
    @FXML private TableColumn<Tariff, String> gasNameCol;
    @FXML private TableColumn<Tariff, String> gasStandingCol;
    @FXML private TableColumn<Tariff, String> gasUnitCol;
    @FXML private TableColumn<Tariff, String> gasCalorificCol;
    @FXML private TableColumn<Tariff, String> gasStatusCol;
    @FXML private TableColumn<Tariff, Void> gasActionsCol;

    private final TariffService tariffService;

    public TariffController() {
        this.tariffService = TariffService.getInstance();
    }

    @FXML
    public void initialize() {
        setupElectricityTable();
        setupGasTable();
        refreshData();
    }

    private void setupElectricityTable() {
        elecNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        
        elecTypeCol.setCellValueFactory(data -> {
            Tariff t = data.getValue();
            if (t instanceof ElectricityTariff et) {
                return new SimpleStringProperty(et.isTieredPricing() ? "Tiered" : "Flat");
            }
            return new SimpleStringProperty("Flat");
        });
        
        elecStandingCol.setCellValueFactory(data -> {
            BigDecimal sc = data.getValue().getStandingCharge();
            return new SimpleStringProperty(sc != null ? sc + "p/day" : "N/A");
        });
        
        elecUnitCol.setCellValueFactory(data -> {
            BigDecimal ur = data.getValue().getUnitRate();
            return new SimpleStringProperty(ur != null ? ur + "p/kWh" : "N/A");
        });
        
        elecTier1Col.setCellValueFactory(data -> {
            if (data.getValue() instanceof ElectricityTariff et && et.isTieredPricing()) {
                return new SimpleStringProperty(et.getTier1Rate() + "p");
            }
            return new SimpleStringProperty("-");
        });
        
        elecTier2Col.setCellValueFactory(data -> {
            if (data.getValue() instanceof ElectricityTariff et && et.isTieredPricing()) {
                return new SimpleStringProperty(et.getTier2Rate() + "p");
            }
            return new SimpleStringProperty("-");
        });
        
        elecStatusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));

        // Style status
        elecStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Active".equals(item) ? 
                        "-fx-text-fill: #10b981; -fx-font-weight: bold;" : 
                        "-fx-text-fill: #94a3b8;");
                }
            }
        });

        // Actions column
        elecActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                editBtn.setOnAction(e -> editTariff(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteTariff(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupGasTable() {
        gasNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        
        gasStandingCol.setCellValueFactory(data -> {
            BigDecimal sc = data.getValue().getStandingCharge();
            return new SimpleStringProperty(sc != null ? sc + "p/day" : "N/A");
        });
        
        gasUnitCol.setCellValueFactory(data -> {
            BigDecimal ur = data.getValue().getUnitRate();
            return new SimpleStringProperty(ur != null ? ur + "p/kWh" : "N/A");
        });
        
        gasCalorificCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof GasTariff gt) {
                return new SimpleStringProperty(String.valueOf(gt.getCalorificValue()));
            }
            return new SimpleStringProperty("39.4");
        });
        
        gasStatusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));

        gasStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Active".equals(item) ? 
                        "-fx-text-fill: #10b981; -fx-font-weight: bold;" : 
                        "-fx-text-fill: #94a3b8;");
                }
            }
        });

        gasActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                editBtn.setOnAction(e -> editTariff(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteTariff(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    public void refreshData() {
        try {
            List<Tariff> electricityTariffs = tariffService.getTariffsByMeterType(MeterType.ELECTRICITY);
            List<Tariff> gasTariffs = tariffService.getTariffsByMeterType(MeterType.GAS);

            electricityTable.setItems(FXCollections.observableArrayList(electricityTariffs));
            gasTable.setItems(FXCollections.observableArrayList(gasTariffs));
        } catch (DataPersistenceException e) {
            showError("Failed to load tariffs: " + e.getMessage());
        }
    }

    @FXML
    public void showAddTariffDialog() {
        Dialog<Tariff> dialog = new Dialog<>();
        dialog.setTitle("Add Tariff");
        dialog.setHeaderText("Create a new tariff");

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("Electricity", "Gas"));
        typeCombo.setValue("Electricity");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Standard Variable");
        TextField standingField = new TextField();
        standingField.setPromptText("e.g., 22.63");
        TextField unitRateField = new TextField();
        unitRateField.setPromptText("e.g., 19.349");
        CheckBox tieredCheck = new CheckBox("Tiered Pricing");
        TextField tier1Field = new TextField();
        tier1Field.setPromptText("Tier 1 rate");
        tier1Field.setDisable(true);
        TextField tier2Field = new TextField();
        tier2Field.setPromptText("Tier 2 rate");
        tier2Field.setDisable(true);
        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Tier 1 threshold (kWh)");
        thresholdField.setDisable(true);
        TextField calorificField = new TextField("39.4");
        calorificField.setPromptText("Calorific value");

        tieredCheck.setOnAction(e -> {
            tier1Field.setDisable(!tieredCheck.isSelected());
            tier2Field.setDisable(!tieredCheck.isSelected());
            thresholdField.setDisable(!tieredCheck.isSelected());
        });

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Standing Charge (p/day):"), 0, 2);
        grid.add(standingField, 1, 2);
        grid.add(new Label("Unit Rate (p/kWh):"), 0, 3);
        grid.add(unitRateField, 1, 3);
        grid.add(tieredCheck, 1, 4);
        grid.add(new Label("Tier 1 Rate (p):"), 0, 5);
        grid.add(tier1Field, 1, 5);
        grid.add(new Label("Tier 2 Rate (p):"), 0, 6);
        grid.add(tier2Field, 1, 6);
        grid.add(new Label("Tier 1 Threshold:"), 0, 7);
        grid.add(thresholdField, 1, 7);
        grid.add(new Label("Calorific Value:"), 0, 8);
        grid.add(calorificField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addType) {
                try {
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        showError("Tariff name is required");
                        return null;
                    }

                    BigDecimal standing = new BigDecimal(standingField.getText().trim());
                    BigDecimal unitRate = new BigDecimal(unitRateField.getText().trim());

                    Tariff tariff;
                    if ("Electricity".equals(typeCombo.getValue())) {
                        if (tieredCheck.isSelected()) {
                            double threshold = Double.parseDouble(thresholdField.getText().trim());
                            BigDecimal tier1 = new BigDecimal(tier1Field.getText().trim());
                            BigDecimal tier2 = new BigDecimal(tier2Field.getText().trim());
                            tariff = new ElectricityTariff(name, standing, threshold, tier1, tier2);
                        } else {
                            tariff = new ElectricityTariff(name, standing, unitRate);
                        }
                    } else {
                        BigDecimal calorific = new BigDecimal(calorificField.getText().trim());
                        tariff = new GasTariff(name, standing, unitRate, calorific);
                    }

                    tariffService.createTariff(tariff);
                    return tariff;
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                    return null;
                } catch (DataPersistenceException e) {
                    showError("Failed to create tariff: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Tariff> result = dialog.showAndWait();
        result.ifPresent(tariff -> {
            showSuccess("Tariff '" + tariff.getName() + "' created successfully!");
            refreshData();
        });
    }

    private void editTariff(Tariff tariff) {
        Dialog<Tariff> dialog = new Dialog<>();
        dialog.setTitle("Edit Tariff");
        dialog.setHeaderText("Edit tariff: " + tariff.getName());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(tariff.getName());
        TextField standingField = new TextField(tariff.getStandingCharge().toString());
        TextField unitRateField = new TextField(tariff.getUnitRate().toString());
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(tariff.isActive());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Standing Charge (p/day):"), 0, 1);
        grid.add(standingField, 1, 1);
        grid.add(new Label("Unit Rate (p/kWh):"), 0, 2);
        grid.add(unitRateField, 1, 2);
        grid.add(activeCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveType) {
                try {
                    tariff.setName(nameField.getText().trim());
                    tariff.setStandingCharge(new BigDecimal(standingField.getText().trim()));
                    BigDecimal newUnitRate = new BigDecimal(unitRateField.getText().trim());
                    if (tariff instanceof ElectricityTariff et) {
                        et.setUnitRatePence(newUnitRate);
                    } else if (tariff instanceof GasTariff gt) {
                        gt.setUnitRatePence(newUnitRate);
                    }
                    tariff.setActive(activeCheck.isSelected());
                    tariffService.updateTariff(tariff);
                    return tariff;
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                    return null;
                } catch (DataPersistenceException e) {
                    showError("Failed to update tariff: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Tariff> result = dialog.showAndWait();
        result.ifPresent(t -> {
            showSuccess("Tariff updated successfully!");
            refreshData();
        });
    }

    private void deleteTariff(Tariff tariff) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Tariff");
        confirm.setHeaderText("Delete tariff: " + tariff.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tariffService.deleteTariff(tariff.getTariffId());
                showSuccess("Tariff deleted successfully!");
                refreshData();
            } catch (DataPersistenceException e) {
                showError("Failed to delete tariff: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

