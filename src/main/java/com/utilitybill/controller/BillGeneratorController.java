package com.utilitybill.controller;

import com.utilitybill.util.BillCalculator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controller for the bill generation view.
 * Handles calculation of electricity and gas bills based on meter readings.
 *
 * <p>Supports Day/Night (Economy 7) electricity meters and gas meters.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
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

    /** Gas calorific value (MJ/m³) - UK average */
    private static final double GAS_CALORIFIC_VALUE = 39.4;
    
    /** Gas volume correction factor - UK standard */
    private static final double GAS_CORRECTION_FACTOR = 1.02264;
    
    /** Imperial to metric conversion (100s of cubic feet to cubic meters) */
    private static final double IMPERIAL_TO_METRIC = 2.83;

    @FXML
    public void initialize() {
        // Set default values from real UK energy bill statement (ex-VAT rates)
        // Electricity: 22.63p/day standing, 19.349p/kWh unit rate
        elecStandingChargeField.setText("22.63");
        elecUnitPriceField.setText("19.349");
        elecDayOpeningField.setText("40470.637");
        elecDayClosingField.setText("40516.687");
        elecNightOpeningField.setText("37386.998");
        elecNightClosingField.setText("37623.210");

        // Gas: 24.87p/day standing, 3.797p/kWh unit rate (ex-VAT)
        gasStandingChargeField.setText("24.87");
        gasUnitPriceField.setText("3.797");
        gasOpeningField.setText("10091.5");
        gasClosingField.setText("10127.6");

        billingDaysField.setText("33");
        vatRateField.setText("5");
        
        // Default: VAT is added on top (as shown on actual bill statements)
        if (vatInclusiveCheckbox != null) {
            vatInclusiveCheckbox.setSelected(false);
        }
        
        // Default: Imperial gas meter (100s of cubic feet)
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
            // Parse billing days and VAT
            int billingDays = parseInteger(billingDaysField.getText(), "Billing Days");
            double vatRate = parseDouble(vatRateField.getText(), "VAT Rate") / 100.0;
            boolean vatInclusive = vatInclusiveCheckbox != null && vatInclusiveCheckbox.isSelected();

            // Calculate Electricity Bill
            BigDecimal electricityTotal = calculateElectricityBill(billingDays, vatRate, vatInclusive);

            // Calculate Gas Bill
            BigDecimal gasTotal = calculateGasBill(billingDays, vatRate, vatInclusive);

            // Calculate combined total
            BigDecimal grandTotal = electricityTotal.add(gasTotal);

            // Update output labels
            elecBillLabel.setText(String.format("£%.2f", electricityTotal));
            gasBillLabel.setText(String.format("£%.2f", gasTotal));
            totalBillLabel.setText(String.format("£%.2f", grandTotal));
            billingPeriodLabel.setText(String.format("FOR %d DAYS", billingDays));

            // Show results
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

    /**
     * Calculates the electricity bill.
     *
     * @param billingDays number of billing days
     * @param vatRate VAT rate as decimal
     * @param vatInclusive true if prices already include VAT
     * @return total electricity bill including VAT
     */
    private BigDecimal calculateElectricityBill(int billingDays, double vatRate, boolean vatInclusive) {
        // Parse electricity inputs
        double standingChargePence = parseDouble(elecStandingChargeField.getText(), "Electricity Standing Charge");
        double unitPricePence = parseDouble(elecUnitPriceField.getText(), "Electricity Unit Price");
        double dayOpening = parseDouble(elecDayOpeningField.getText(), "Day Opening Reading");
        double dayClosing = parseDouble(elecDayClosingField.getText(), "Day Closing Reading");
        double nightOpening = parseDouble(elecNightOpeningField.getText(), "Night Opening Reading");
        double nightClosing = parseDouble(elecNightClosingField.getText(), "Night Closing Reading");

        // Calculate consumption
        double dayUnits = dayClosing - dayOpening;
        double nightUnits = nightClosing - nightOpening;
        double totalUnits = dayUnits + nightUnits;

        // Validate readings
        if (dayUnits < 0 || nightUnits < 0) {
            throw new IllegalArgumentException("Closing readings must be greater than opening readings");
        }

        // Calculate costs
        BigDecimal unitCost = BillCalculator.calculateUnitCost(totalUnits, BigDecimal.valueOf(unitPricePence));
        BigDecimal standingCost = BigDecimal.valueOf(standingChargePence)
                .multiply(BigDecimal.valueOf(billingDays))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal subtotal = unitCost.add(standingCost);
        BigDecimal vat;
        BigDecimal total;
        
        if (vatInclusive) {
            // Prices already include VAT - extract VAT from total
            // If price includes 5% VAT, the net = price / 1.05, VAT = price - net
            BigDecimal vatMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(vatRate));
            BigDecimal netAmount = subtotal.divide(vatMultiplier, 2, RoundingMode.HALF_UP);
            vat = subtotal.subtract(netAmount);
            total = subtotal; // Total is the same as subtotal (VAT already included)
        } else {
            // Add VAT on top
            vat = subtotal.multiply(BigDecimal.valueOf(vatRate)).setScale(2, RoundingMode.HALF_UP);
            total = subtotal.add(vat);
        }

        // Update detail labels if they exist
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

    /**
     * Calculates the gas bill.
     *
     * @param billingDays number of billing days
     * @param vatRate VAT rate as decimal
     * @param vatInclusive true if prices already include VAT
     * @return total gas bill including VAT
     */
    private BigDecimal calculateGasBill(int billingDays, double vatRate, boolean vatInclusive) {
        // Parse gas inputs
        double standingChargePence = parseDouble(gasStandingChargeField.getText(), "Gas Standing Charge");
        double unitPricePence = parseDouble(gasUnitPriceField.getText(), "Gas Unit Price");
        double gasOpening = parseDouble(gasOpeningField.getText(), "Gas Opening Reading");
        double gasClosing = parseDouble(gasClosingField.getText(), "Gas Closing Reading");
        boolean isImperial = imperialMeterCheckbox != null && imperialMeterCheckbox.isSelected();

        // Calculate consumption - raw meter units
        double meterUnits = gasClosing - gasOpening;
        
        if (meterUnits < 0) {
            throw new IllegalArgumentException("Gas closing reading must be greater than opening reading");
        }

        // Convert to cubic meters
        // Imperial meters measure in 100s of cubic feet - multiply by 2.83 to get m³
        // Metric meters already measure in m³
        double cubicMeters = isImperial ? meterUnits * IMPERIAL_TO_METRIC : meterUnits;

        // Convert cubic meters to kWh using UK standard formula:
        // kWh = Volume (m³) × Correction Factor × Calorific Value ÷ 3.6
        double kWh = cubicMeters * GAS_CORRECTION_FACTOR * GAS_CALORIFIC_VALUE / 3.6;

        // Calculate costs
        BigDecimal unitCost = BillCalculator.calculateUnitCost(kWh, BigDecimal.valueOf(unitPricePence));
        BigDecimal standingCost = BigDecimal.valueOf(standingChargePence)
                .multiply(BigDecimal.valueOf(billingDays))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal subtotal = unitCost.add(standingCost);
        BigDecimal vat;
        BigDecimal total;
        
        if (vatInclusive) {
            // Prices already include VAT - extract VAT from total
            BigDecimal vatMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(vatRate));
            BigDecimal netAmount = subtotal.divide(vatMultiplier, 2, RoundingMode.HALF_UP);
            vat = subtotal.subtract(netAmount);
            total = subtotal; // Total is the same as subtotal (VAT already included)
        } else {
            // Add VAT on top
            vat = subtotal.multiply(BigDecimal.valueOf(vatRate)).setScale(2, RoundingMode.HALF_UP);
            total = subtotal.add(vat);
        }

        // Update detail labels if they exist
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

    /**
     * Parses a double from text with validation.
     */
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

    /**
     * Parses an integer from text with validation.
     */
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
        // Clear electricity fields
        elecStandingChargeField.clear();
        elecUnitPriceField.clear();
        elecDayOpeningField.clear();
        elecDayClosingField.clear();
        elecNightOpeningField.clear();
        elecNightClosingField.clear();

        // Clear gas fields
        gasStandingChargeField.clear();
        gasUnitPriceField.clear();
        gasOpeningField.clear();
        gasClosingField.clear();

        // Clear common fields
        billingDaysField.clear();
        vatRateField.setText("5");

        // Hide results
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

