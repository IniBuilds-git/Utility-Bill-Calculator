package com.utilitybill.controller;

import com.utilitybill.util.BillCalculator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BillGeneratorController {

    // Electricity inputs
    @FXML private TextField elecStandingChargeField;
    @FXML private TextField elecUnitPriceField;
    @FXML private TextField elecDayOpeningField;
    @FXML private TextField elecDayClosingField;
    @FXML private TextField elecNightOpeningField;
    @FXML private TextField elecNightClosingField;

    // Gas inputs
    @FXML private TextField gasStandingChargeField;
    @FXML private TextField gasUnitPriceField;
    @FXML private TextField gasOpeningField;
    @FXML private TextField gasClosingField;
    @FXML private CheckBox imperialMeterCheckbox;

    // Common inputs
    @FXML private TextField billingDaysField;
    @FXML private TextField vatRateField;
    @FXML private CheckBox vatInclusiveCheckbox;

    // Output labels
    @FXML private Label elecBillLabel;
    @FXML private Label gasBillLabel;
    @FXML private Label totalBillLabel;
    @FXML private Label billingPeriodLabel;
    @FXML private Label errorLabel;

    // Detail labels
    @FXML private Label elecDayUnitsLabel;
    @FXML private Label elecNightUnitsLabel;
    @FXML private Label elecTotalUnitsLabel;
    @FXML private Label elecUnitCostLabel;
    @FXML private Label elecStandingCostLabel;
    @FXML private Label elecSubtotalLabel;
    @FXML private Label elecVatLabel;

    @FXML private Label gasUnitsLabel;
    @FXML private Label gasKwhLabel;
    @FXML private Label gasUnitCostLabel;
    @FXML private Label gasStandingCostLabel;
    @FXML private Label gasSubtotalLabel;
    @FXML private Label gasVatLabel;

    // Results panel
    @FXML private javafx.scene.layout.VBox resultsPanel;

    private Stage dialogStage;

    private static final double GAS_CALORIFIC_VALUE = 39.4;
    private static final double GAS_CORRECTION_FACTOR = 1.02264;
    private static final double IMPERIAL_TO_METRIC = 2.83;

    @FXML
    public void initialize() {
        elecStandingChargeField.setText("22.63");
        elecUnitPriceField.setText("19.349");
        elecDayOpeningField.setText("40470.637");
        elecDayClosingField.setText("40516.687");
        elecNightOpeningField.setText("37386.998");
        elecNightClosingField.setText("37623.210");

        gasStandingChargeField.setText("24.87");
        gasUnitPriceField.setText("3.797");
        gasOpeningField.setText("10091.5");
        gasClosingField.setText("10127.6");

        billingDaysField.setText("33");
        vatRateField.setText("5");

        if (vatInclusiveCheckbox != null) {
            vatInclusiveCheckbox.setSelected(false);
        }

        if (imperialMeterCheckbox != null) {
            imperialMeterCheckbox.setSelected(true);
        }

        hideError();
        if (resultsPanel != null) {
            resultsPanel.setVisible(false);
            resultsPanel.setManaged(false);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleCalculate() {
        hideError();

        try {
            int billingDays = parseInteger(billingDaysField.getText(), "Billing Days");
            double vatRate = parseDouble(vatRateField.getText(), "VAT Rate") / 100.0;
            boolean vatInclusive = vatInclusiveCheckbox != null && vatInclusiveCheckbox.isSelected();

            BigDecimal electricityTotal = calculateElectricityBill(billingDays, vatRate, vatInclusive);
            BigDecimal gasTotal = calculateGasBill(billingDays, vatRate, vatInclusive);
            BigDecimal grandTotal = electricityTotal.add(gasTotal);

            elecBillLabel.setText(String.format("£%.2f", electricityTotal));
            gasBillLabel.setText(String.format("£%.2f", gasTotal));
            totalBillLabel.setText(String.format("£%.2f", grandTotal));
            billingPeriodLabel.setText(String.format("FOR %d DAYS", billingDays));

            if (resultsPanel != null) {
                resultsPanel.setVisible(true);
                resultsPanel.setManaged(true);
            }

        } catch (NumberFormatException e) {
            showError("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private BigDecimal calculateElectricityBill(int billingDays, double vatRate, boolean vatInclusive) {
        double standingChargePence = parseDouble(elecStandingChargeField.getText(), "Electricity Standing Charge");
        double unitPricePence = parseDouble(elecUnitPriceField.getText(), "Electricity Unit Price");
        double dayOpening = parseDouble(elecDayOpeningField.getText(), "Day Opening Reading");
        double dayClosing = parseDouble(elecDayClosingField.getText(), "Day Closing Reading");
        double nightOpening = parseDouble(elecNightOpeningField.getText(), "Night Opening Reading");
        double nightClosing = parseDouble(elecNightClosingField.getText(), "Night Closing Reading");

        double dayUnits = dayClosing - dayOpening;
        double nightUnits = nightClosing - nightOpening;
        double totalUnits = dayUnits + nightUnits;

        if (dayUnits < 0 || nightUnits < 0) {
            throw new IllegalArgumentException("Closing readings must be greater than opening readings");
        }

        BigDecimal unitCost = BillCalculator.calculateUnitCost(totalUnits, BigDecimal.valueOf(unitPricePence));
        BigDecimal standingCost = BigDecimal.valueOf(standingChargePence)
                .multiply(BigDecimal.valueOf(billingDays))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal subtotal = unitCost.add(standingCost);
        BigDecimal vat;
        BigDecimal total;

        if (vatInclusive) {
            BigDecimal vatMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(vatRate));
            BigDecimal netAmount = subtotal.divide(vatMultiplier, 2, RoundingMode.HALF_UP);
            vat = subtotal.subtract(netAmount);
            total = subtotal; // Total is the same as subtotal (VAT already included)
        } else {
            // Add VAT on top
            vat = subtotal.multiply(BigDecimal.valueOf(vatRate)).setScale(2, RoundingMode.HALF_UP);
            total = subtotal.add(vat);
        }

        if (elecDayUnitsLabel != null) {
            elecDayUnitsLabel.setText(String.format("%.3f kWh", dayUnits));
            elecNightUnitsLabel.setText(String.format("%.3f kWh", nightUnits));
            elecTotalUnitsLabel.setText(String.format("%.3f kWh", totalUnits));
            elecUnitCostLabel.setText(String.format("£%.2f", unitCost));
            elecStandingCostLabel.setText(String.format("£%.2f", standingCost));
            elecSubtotalLabel.setText(String.format("£%.2f", subtotal));
            elecVatLabel.setText(String.format("£%.2f %s", vat, vatInclusive ? "(included)" : "(5%)"));
        }

        return total;
    }

    private BigDecimal calculateGasBill(int billingDays, double vatRate, boolean vatInclusive) {
        double standingChargePence = parseDouble(gasStandingChargeField.getText(), "Gas Standing Charge");
        double unitPricePence = parseDouble(gasUnitPriceField.getText(), "Gas Unit Price");
        double gasOpening = parseDouble(gasOpeningField.getText(), "Gas Opening Reading");
        double gasClosing = parseDouble(gasClosingField.getText(), "Gas Closing Reading");
        boolean isImperial = imperialMeterCheckbox != null && imperialMeterCheckbox.isSelected();

        double meterUnits = gasClosing - gasOpening;

        if (meterUnits < 0) {
            throw new IllegalArgumentException("Gas closing reading must be greater than opening reading");
        }

        double cubicMeters = isImperial ? meterUnits * IMPERIAL_TO_METRIC : meterUnits;
        double kWh = cubicMeters * GAS_CORRECTION_FACTOR * GAS_CALORIFIC_VALUE / 3.6;

        BigDecimal unitCost = BillCalculator.calculateUnitCost(kWh, BigDecimal.valueOf(unitPricePence));
        BigDecimal standingCost = BigDecimal.valueOf(standingChargePence)
                .multiply(BigDecimal.valueOf(billingDays))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal subtotal = unitCost.add(standingCost);
        BigDecimal vat;
        BigDecimal total;

        if (vatInclusive) {
            BigDecimal vatMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(vatRate));
            BigDecimal netAmount = subtotal.divide(vatMultiplier, 2, RoundingMode.HALF_UP);
            vat = subtotal.subtract(netAmount);
            total = subtotal;
        } else {
            vat = subtotal.multiply(BigDecimal.valueOf(vatRate)).setScale(2, RoundingMode.HALF_UP);
            total = subtotal.add(vat);
        }

        if (gasUnitsLabel != null) {
            String unitsText = isImperial 
                ? String.format("%.1f units → %.1f m³", meterUnits, cubicMeters)
                : String.format("%.1f m³", cubicMeters);
            gasUnitsLabel.setText(unitsText);
            gasKwhLabel.setText(String.format("%.3f kWh", kWh));
            gasUnitCostLabel.setText(String.format("£%.2f", unitCost));
            gasStandingCostLabel.setText(String.format("£%.2f", standingCost));
            gasSubtotalLabel.setText(String.format("£%.2f", subtotal));
            gasVatLabel.setText(String.format("£%.2f %s", vat, vatInclusive ? "(included)" : "(5%)"));
        }

        return total;
    }

    private double parseDouble(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid " + fieldName);
        }
    }

    private int parseInteger(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid " + fieldName);
        }
    }

    @FXML
    private void handleClear() {
        elecStandingChargeField.clear();
        elecUnitPriceField.clear();
        elecDayOpeningField.clear();
        elecDayClosingField.clear();
        elecNightOpeningField.clear();
        elecNightClosingField.clear();

        gasStandingChargeField.clear();
        gasUnitPriceField.clear();
        gasOpeningField.clear();
        gasClosingField.clear();

        billingDaysField.clear();
        vatRateField.setText("5");

        if (resultsPanel != null) {
            resultsPanel.setVisible(false);
            resultsPanel.setManaged(false);
        }

        hideError();
    }

    @FXML
    private void handleClose() {
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
}

