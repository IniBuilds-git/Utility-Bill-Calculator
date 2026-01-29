package com.utilitybill.controller;

import com.utilitybill.Main;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.User;
import com.utilitybill.service.*;
import com.utilitybill.util.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    private static final String CLASS_NAME = DashboardController.class.getName();

    // Navigation buttons
    @FXML
    private Button dashboardBtn;
    @FXML
    private Button customersBtn;
    @FXML
    private Button meterReadingsBtn;
    @FXML
    private Button invoicesBtn;
    @FXML
    private Button paymentsBtn;
    @FXML
    private Button tariffsBtn;
    @FXML
    private Button billGeneratorBtn;

    // Dashboard stats
    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label unpaidInvoicesLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label overdueLabel;

    // User info
    @FXML
    private Label userLabel;

    // Content area
    @FXML
    private StackPane contentArea;

    // Charts and Table removed for simplicity

    private final AuthenticationService authService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final PaymentService paymentService;

    private Button currentActiveButton;
    private Node dashboardHomeContent;

    public DashboardController() {
        this.authService = AuthenticationService.getInstance();
        this.customerService = CustomerService.getInstance();
        this.billingService = BillingService.getInstance();
        this.paymentService = PaymentService.getInstance();
    }

    @FXML
    public void initialize() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Welcome, " + currentUser.getFullName());
        }

        currentActiveButton = dashboardBtn;

        if (!contentArea.getChildren().isEmpty()) {
            dashboardHomeContent = contentArea.getChildren().get(0);
        }

        refreshDashboardStats();
    }

    private void refreshDashboardStats() {
        try {
            long customerCount = customerService.getCustomerCount();
            totalCustomersLabel.setText(String.valueOf(customerCount));

            List<Invoice> unpaidInvoices = billingService.getUnpaidInvoices();
            unpaidInvoicesLabel.setText(String.valueOf(unpaidInvoices.size()));

            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate today = LocalDate.now();
            BigDecimal revenue = paymentService.getTotalPaymentsByDateRange(startOfMonth, today);
            totalRevenueLabel.setText(String.format("£%.2f", revenue));

            List<Invoice> overdueInvoices = billingService.getOverdueInvoices();
            overdueLabel.setText(String.valueOf(overdueInvoices.size()));

        } catch (DataPersistenceException e) {
            AppLogger.error(CLASS_NAME, "Dashboard refresh error: " + e.getMessage(), e);
        }
    }

    private static final String ACTIVE_BUTTON_STYLE = "-fx-background-color: #e0f2f1; -fx-text-fill: #0d9488; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String INACTIVE_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: #475569; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;";

    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(INACTIVE_BUTTON_STYLE);
        }
        button.setStyle(ACTIVE_BUTTON_STYLE);
        currentActiveButton = button;
    }

    @FXML
    private void showDashboard() {
        setActiveButton(dashboardBtn);
        if (dashboardHomeContent != null) {
            contentArea.getChildren().setAll(dashboardHomeContent);
        }
        refreshDashboardStats();
    }

    @FXML
    private void showCustomers() {
        setActiveButton(customersBtn);
        loadContent("/com/utilitybill/view/customer-management.fxml");
    }

    @FXML
    private void showMeterReadings() {
        setActiveButton(meterReadingsBtn);
        loadContent("/com/utilitybill/view/meter-readings.fxml");
    }

    @FXML
    private void showInvoices() {
        setActiveButton(invoicesBtn);
        loadContent("/com/utilitybill/view/invoices.fxml");
    }

    @FXML
    private void showPayments() {
        setActiveButton(paymentsBtn);
        loadContent("/com/utilitybill/view/payments.fxml");
    }

    @FXML
    private void showTariffs() {
        setActiveButton(tariffsBtn);
        loadContent("/com/utilitybill/view/tariffs.fxml");
    }

    @FXML
    private void showBillGenerator() {
        setActiveButton(billGeneratorBtn);
        loadContent("/com/utilitybill/view/bill-generator.fxml");
    }

    @FXML
    private void showAddCustomer() {
        showCustomers();
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        Main.showLogin();
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            AppLogger.error(CLASS_NAME, "Error loading content: " + e.getMessage(), e);
            showPlaceholder("Error", "Failed to load content: " + e.getMessage());
        }
    }

    private void showPlaceholder(String title, String description) {
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");
        placeholder.setPadding(new javafx.geometry.Insets(50));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 48px;");

        placeholder.getChildren().addAll(icon, titleLabel, descLabel);
        contentArea.getChildren().setAll(placeholder);
    }
}
