package com.utilitybill.controller;

import com.utilitybill.Main;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.User;
import com.utilitybill.service.*;
import com.utilitybill.util.DateUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the main dashboard view.
 * Manages navigation and displays summary statistics.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class DashboardController {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button customersBtn;
    @FXML private Button meterReadingsBtn;
    @FXML private Button invoicesBtn;
    @FXML private Button paymentsBtn;
    @FXML private Button tariffsBtn;
    @FXML private Button billGeneratorBtn;

    // Dashboard stats
    @FXML private Label totalCustomersLabel;
    @FXML private Label unpaidInvoicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label overdueLabel;

    // User info
    @FXML private Label userLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateTimeLabel;

    // Content area
    @FXML private StackPane contentArea;

    // Recent invoices table
    @FXML private TableView<Invoice> recentInvoicesTable;
    @FXML private TableColumn<Invoice, String> invoiceNumberCol;
    @FXML private TableColumn<Invoice, String> customerNameCol;
    @FXML private TableColumn<Invoice, String> amountCol;
    @FXML private TableColumn<Invoice, String> statusCol;
    @FXML private TableColumn<Invoice, String> dueDateCol;

    /** Services */
    private final AuthenticationService authService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final PaymentService paymentService;

    /** DateTime formatter */
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy  HH:mm:ss");

    /** Current active button */
    private Button currentActiveButton;

    /** Original dashboard content - stored to restore when navigating back */
    private Node dashboardHomeContent;

    /**
     * Constructs a new DashboardController.
     */
    public DashboardController() {
        this.authService = AuthenticationService.getInstance();
        this.customerService = CustomerService.getInstance();
        this.billingService = BillingService.getInstance();
        this.paymentService = PaymentService.getInstance();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Set user info
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Welcome, " + currentUser.getFullName());
        }

        // Set current active button
        currentActiveButton = dashboardBtn;

        // Store the original dashboard content for later restoration
        if (!contentArea.getChildren().isEmpty()) {
            dashboardHomeContent = contentArea.getChildren().get(0);
        }

        // Setup table columns
        setupTableColumns();

        // Load dashboard data
        refreshDashboardStats();

        // Start clock
        startClock();

        // Set initial status
        statusLabel.setText("Dashboard loaded successfully");
    }

    /**
     * Sets up the table columns for the recent invoices table.
     */
    private void setupTableColumns() {
        invoiceNumberCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInvoiceNumber()));

        customerNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAccountNumber()));

        amountCol.setCellValueFactory(data -> {
            BigDecimal amount = data.getValue().getTotalAmount();
            return new SimpleStringProperty(amount != null ? String.format("£%.2f", amount) : "£0.00");
        });

        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

        dueDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getDueDate())));

        // Style status column
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Paid" -> setStyle("-fx-text-fill: #10b981;");
                        case "Overdue" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        case "Pending" -> setStyle("-fx-text-fill: #f59e0b;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Refreshes the dashboard statistics.
     */
    private void refreshDashboardStats() {
        try {
            // Total customers
            long customerCount = customerService.getCustomerCount();
            totalCustomersLabel.setText(String.valueOf(customerCount));

            // Unpaid invoices
            List<Invoice> unpaidInvoices = billingService.getUnpaidInvoices();
            unpaidInvoicesLabel.setText(String.valueOf(unpaidInvoices.size()));

            // Total revenue this month
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate today = LocalDate.now();
            BigDecimal revenue = paymentService.getTotalPaymentsByDateRange(startOfMonth, today);
            totalRevenueLabel.setText(String.format("£%.2f", revenue));

            // Overdue invoices
            List<Invoice> overdueInvoices = billingService.getOverdueInvoices();
            overdueLabel.setText(String.valueOf(overdueInvoices.size()));

            // Load recent invoices to table
            List<Invoice> allInvoices = billingService.getUnpaidInvoices();
            recentInvoicesTable.setItems(FXCollections.observableArrayList(
                    allInvoices.stream().limit(10).toList()
            ));

        } catch (DataPersistenceException e) {
            statusLabel.setText("Error loading dashboard data");
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }

    /**
     * Starts the clock display.
     */
    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            dateTimeLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /** Style for active sidebar button */
    private static final String ACTIVE_BUTTON_STYLE = "-fx-background-color: #e0f2f1; -fx-text-fill: #0d9488; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;";

    /** Style for inactive sidebar button */
    private static final String INACTIVE_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: #475569; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand;";

    /**
     * Updates the active navigation button.
     */
    private void setActiveButton(Button button) {
        // Reset all buttons to inactive style
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(INACTIVE_BUTTON_STYLE);
        }
        // Set new active button style
        button.setStyle(ACTIVE_BUTTON_STYLE);
        currentActiveButton = button;
    }

    // ==================== Navigation Handlers ====================

    @FXML
    private void showDashboard() {
        setActiveButton(dashboardBtn);
        // Restore the original dashboard content
        if (dashboardHomeContent != null) {
            contentArea.getChildren().setAll(dashboardHomeContent);
        }
        // Refresh the dashboard data
        refreshDashboardStats();
        statusLabel.setText("Dashboard");
    }

    @FXML
    private void showCustomers() {
        setActiveButton(customersBtn);
        loadContent("/com/utilitybill/view/customer-management.fxml");
        statusLabel.setText("Customer Management");
    }

    @FXML
    private void showMeterReadings() {
        setActiveButton(meterReadingsBtn);
        loadContent("/com/utilitybill/view/meter-readings.fxml");
        statusLabel.setText("Meter Readings");
    }

    @FXML
    private void showInvoices() {
        setActiveButton(invoicesBtn);
        loadContent("/com/utilitybill/view/invoices.fxml");
        statusLabel.setText("Invoice Management");
    }

    @FXML
    private void showPayments() {
        setActiveButton(paymentsBtn);
        loadContent("/com/utilitybill/view/payments.fxml");
        statusLabel.setText("Payment Processing");
    }

    @FXML
    private void showTariffs() {
        setActiveButton(tariffsBtn);
        loadContent("/com/utilitybill/view/tariffs.fxml");
        statusLabel.setText("Tariff Management");
    }

    @FXML
    private void showBillGenerator() {
        setActiveButton(billGeneratorBtn);
        loadContent("/com/utilitybill/view/bill-generator.fxml");
        statusLabel.setText("Bill Calculator");
    }

    @FXML
    private void showReports() {
        statusLabel.setText("Reports & Analytics - Coming Soon");
        showPlaceholder("Reports & Analytics", "View detailed reports and analytics");
    }

    @FXML
    private void showAddCustomer() {
        showCustomers();
        // TODO: Open add customer dialog
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        Main.showLogin();
    }

    /**
     * Loads content into the main content area.
     */
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Error loading content: " + e.getMessage());
            showPlaceholder("Error", "Failed to load content: " + e.getMessage());
        }
    }

    /**
     * Shows a placeholder for unimplemented features.
     */
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

