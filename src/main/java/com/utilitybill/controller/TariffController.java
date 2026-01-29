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
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class TariffController {

    // ---------- Electricity table ----------
    @FXML private TableView<Tariff> electricityTable;
    @FXML private TableColumn<Tariff, String> elecNameCol;
    @FXML private TableColumn<Tariff, String> elecStandingCol;
    @FXML private TableColumn<Tariff, String> elecDayRateCol;
    @FXML private TableColumn<Tariff, String> elecNightRateCol;
    @FXML private TableColumn<Tariff, String> elecStatusCol;
    @FXML private TableColumn<Tariff, Void> elecActionsCol;

    // ---------- Gas table ----------
    @FXML private TableView<Tariff> gasTable;
    @FXML private TableColumn<Tariff, String> gasNameCol;
    @FXML private TableColumn<Tariff, String> gasStandingCol;
    @FXML private TableColumn<Tariff, String> gasUnitCol;
    @FXML private TableColumn<Tariff, String> gasCvCol;
    @FXML private TableColumn<Tariff, String> gasCorrectCol;
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

    // --------------------------
    // Table setups
    // --------------------------

    private void setupElectricityTable() {
        elecNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        elecStandingCol.setCellValueFactory(data ->
                new SimpleStringProperty(formatPencePerDay(data.getValue().getStandingCharge()))
        );

        // Matches bill: Unit rate per kWh -> Day and Night
        elecDayRateCol.setCellValueFactory(data -> {
            Tariff t = data.getValue();
            if (t instanceof ElectricityTariff et && et.getDayRate() != null) {
                return new SimpleStringProperty(formatPencePerKwh(et.getDayRate()));
            }
            if (t.getUnitRate() != null) {
                return new SimpleStringProperty(formatPencePerKwh(t.getUnitRate()));
            }
            return new SimpleStringProperty("-");
        });

        elecNightRateCol.setCellValueFactory(data -> {
            Tariff t = data.getValue();
            if (t instanceof ElectricityTariff et && et.getNightRate() != null) {
                return new SimpleStringProperty(formatPencePerKwh(et.getNightRate()));
            }
            return new SimpleStringProperty("-");
        });

        elecStatusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive")
        );
        styleStatusColumn(elecStatusCol);

        elecActionsCol.setCellFactory(col -> actionButtonsCell(
                this::editTariff,
                this::deleteTariff
        ));
    }

    private void setupGasTable() {
        gasNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        gasStandingCol.setCellValueFactory(data ->
                new SimpleStringProperty(formatPencePerDay(data.getValue().getStandingCharge()))
        );

        // Matches bill: Unit rate per kWh (single)
        gasUnitCol.setCellValueFactory(data -> {
            BigDecimal ur = data.getValue().getUnitRate();
            return new SimpleStringProperty(ur != null ? formatPencePerKwh(ur) : "N/A");
        });

        gasCvCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof GasTariff gt) {
                return new SimpleStringProperty(String.format("%.1f", gt.getCalorificValue()));
            }
            return new SimpleStringProperty("-");
        });

        gasCorrectCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof GasTariff gt) {
                return new SimpleStringProperty(String.format("%.5f", gt.getCorrectionFactor()));
            }
            return new SimpleStringProperty("-");
        });

        gasStatusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive")
        );
        styleStatusColumn(gasStatusCol);

        gasActionsCol.setCellFactory(col -> actionButtonsCell(
                this::editTariff,
                this::deleteTariff
        ));
    }

    private void styleStatusColumn(TableColumn<Tariff, String> statusCol) {
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Active".equals(item)
                            ? "-fx-text-fill: #10b981; -fx-font-weight: bold;"
                            : "-fx-text-fill: #94a3b8;");
                }
            }
        });
    }

    private TableCell<Tariff, Void> actionButtonsCell(java.util.function.Consumer<Tariff> onEdit,
                                                      java.util.function.Consumer<Tariff> onDelete) {
        return new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");

                editBtn.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    // --------------------------
    // Data
    // --------------------------

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

    // --------------------------
    // Add
    // --------------------------

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
        grid.setPadding(new Insets(20));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("Electricity", "Gas"));
        typeCombo.setValue("Electricity");

        TextField nameField = new TextField();
        nameField.setPromptText("Tariff name");

        TextField standingField = new TextField();
        standingField.setPromptText("Standing charge (p/day)");

        // Electricity fields (match bill: day + night)
        TextField dayRateField = new TextField();
        dayRateField.setPromptText("Day rate (p/kWh)");

        TextField nightRateField = new TextField();
        nightRateField.setPromptText("Night rate (p/kWh)");

        // Gas fields (match bill: unit + cv + correction)
        TextField unitRateField = new TextField();
        unitRateField.setPromptText("Unit rate (p/kWh)");

        TextField cvField = new TextField("39.4");
        cvField.setPromptText("Calorific value");

        TextField cfField = new TextField("1.02264");
        cfField.setPromptText("Correction factor");

        // Switch visible inputs based on type
        Runnable applyTypeVisibility = () -> {
            boolean isElectric = "Electricity".equals(typeCombo.getValue());

            dayRateField.setDisable(!isElectric);
            nightRateField.setDisable(!isElectric);

            unitRateField.setDisable(isElectric);
            cvField.setDisable(isElectric);
            cfField.setDisable(isElectric);
        };

        typeCombo.setOnAction(e -> applyTypeVisibility.run());
        applyTypeVisibility.run();

        int row = 0;
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeCombo, 1, row++);

        grid.add(new Label("Tariff name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Standing charge (p/day):"), 0, row);
        grid.add(standingField, 1, row++);

        // Electricity
        grid.add(new Label("Day rate (p/kWh):"), 0, row);
        grid.add(dayRateField, 1, row++);

        grid.add(new Label("Night rate (p/kWh):"), 0, row);
        grid.add(nightRateField, 1, row++);

        // Gas
        grid.add(new Label("Unit rate (p/kWh):"), 0, row);
        grid.add(unitRateField, 1, row++);

        grid.add(new Label("Calorific value:"), 0, row);
        grid.add(cvField, 1, row++);

        grid.add(new Label("Correction factor:"), 0, row);
        grid.add(cfField, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != addType) return null;

            try {
                String type = typeCombo.getValue();
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    showError("Tariff name is required.");
                    return null;
                }
                if (standingField.getText().trim().isEmpty()) {
                    showError("Standing charge is required.");
                    return null;
                }

                BigDecimal standing = new BigDecimal(standingField.getText().trim());

                Tariff tariff;

                if ("Electricity".equals(type)) {
                    if (dayRateField.getText().trim().isEmpty() || nightRateField.getText().trim().isEmpty()) {
                        showError("Day rate and Night rate are required for electricity tariffs.");
                        return null;
                    }

                    BigDecimal day = new BigDecimal(dayRateField.getText().trim());
                    BigDecimal night = new BigDecimal(nightRateField.getText().trim());

                    tariff = new ElectricityTariff(name, standing, day, night);

                } else {
                    if (unitRateField.getText().trim().isEmpty()) {
                        showError("Unit rate is required for gas tariffs.");
                        return null;
                    }

                    BigDecimal unit = new BigDecimal(unitRateField.getText().trim());
                    double cv = Double.parseDouble(cvField.getText().trim());
                    double cf = Double.parseDouble(cfField.getText().trim());

                    GasTariff gt = new GasTariff(name, standing, unit);
                    gt.setCalorificValue(cv);
                    gt.setCorrectionFactor(cf);
                    tariff = gt;
                }

                tariffService.createTariff(tariff);
                return tariff;

            } catch (NumberFormatException ex) {
                showError("Invalid number format: " + ex.getMessage());
                return null;
            } catch (Exception ex) {
                showError("Error creating tariff: " + ex.getMessage());
                return null;
            }
        });

        dialog.showAndWait().ifPresent(tariff -> {
            showSuccess("Tariff '" + tariff.getName() + "' created successfully!");
            refreshData();
        });
    }

    // --------------------------
    // Edit
    // --------------------------

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
        TextField standingField = new TextField(tariff.getStandingCharge() == null ? "" : tariff.getStandingCharge().toString());

        // Electricity fields
        TextField dayRateField = new TextField();
        TextField nightRateField = new TextField();

        // Gas fields
        TextField unitRateField = new TextField();
        TextField cvField = new TextField();
        TextField cfField = new TextField();

        boolean isElectric = tariff instanceof ElectricityTariff;
        boolean isGas = tariff instanceof GasTariff;

        if (isElectric) {
            ElectricityTariff et = (ElectricityTariff) tariff;
            dayRateField.setText(et.getDayRate() == null ? "" : et.getDayRate().toString());
            nightRateField.setText(et.getNightRate() == null ? "" : et.getNightRate().toString());
        }

        if (isGas) {
            GasTariff gt = (GasTariff) tariff;
            unitRateField.setText(tariff.getUnitRate() == null ? "" : tariff.getUnitRate().toString());
            cvField.setText(String.valueOf(gt.getCalorificValue()));
            cfField.setText(String.valueOf(gt.getCorrectionFactor()));
        }

        // Disable irrelevant fields
        dayRateField.setDisable(!isElectric);
        nightRateField.setDisable(!isElectric);

        unitRateField.setDisable(!isGas);
        cvField.setDisable(!isGas);
        cfField.setDisable(!isGas);

        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(tariff.isActive());

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Standing charge (p/day):"), 0, row);
        grid.add(standingField, 1, row++);

        grid.add(new Label("Day rate (p/kWh):"), 0, row);
        grid.add(dayRateField, 1, row++);

        grid.add(new Label("Night rate (p/kWh):"), 0, row);
        grid.add(nightRateField, 1, row++);

        grid.add(new Label("Unit rate (p/kWh):"), 0, row);
        grid.add(unitRateField, 1, row++);

        grid.add(new Label("Calorific value:"), 0, row);
        grid.add(cvField, 1, row++);

        grid.add(new Label("Correction factor:"), 0, row);
        grid.add(cfField, 1, row++);

        grid.add(activeCheck, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveType) return null;

            try {
                tariff.setName(nameField.getText().trim());
                tariff.setStandingCharge(new BigDecimal(standingField.getText().trim()));

                if (tariff instanceof ElectricityTariff et) {
                    et.setDayRate(new BigDecimal(dayRateField.getText().trim()));
                    et.setNightRate(new BigDecimal(nightRateField.getText().trim()));
                }

                if (tariff instanceof GasTariff gt) {
                    gt.setUnitRatePence(new BigDecimal(unitRateField.getText().trim()));
                    gt.setCalorificValue(Double.parseDouble(cvField.getText().trim()));
                    gt.setCorrectionFactor(Double.parseDouble(cfField.getText().trim()));
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
        });

        Optional<Tariff> result = dialog.showAndWait();
        result.ifPresent(t -> {
            showSuccess("Tariff updated successfully!");
            refreshData();
        });
    }

    // --------------------------
    // Delete
    // --------------------------

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

    // --------------------------
    // Helpers
    // --------------------------

    private String formatPencePerDay(BigDecimal pence) {
        if (pence == null) return "N/A";
        return formatPlain(pence) + "p/day";
    }

    private String formatPencePerKwh(BigDecimal pence) {
        if (pence == null) return "N/A";
        return formatPlain(pence) + "p/kWh";
    }

    private String formatPlain(BigDecimal val) {
        // keep up to 5 dp like bills often do, but avoid trailing zeros if not needed
        BigDecimal scaled = val.setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.toPlainString();
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
