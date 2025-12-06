package com.utilitybill.controller;

import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.InsufficientPaymentException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.Payment;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.PaymentService;
import com.utilitybill.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for payment processing view.
 * Handles recording and tracking customer payments.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class PaymentController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> methodCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> referenceCol;
    @FXML private TableColumn<Payment, String> customerCol;
    @FXML private TableColumn<Payment, String> invoiceCol;
    @FXML private TableColumn<Payment, String> amountCol;
    @FXML private TableColumn<Payment, String> methodCol;
    @FXML private TableColumn<Payment, String> dateCol;
    @FXML private TableColumn<Payment, String> statusCol;
    @FXML private TableColumn<Payment, Void> actionsCol;

    // Summary labels
    @FXML private Label todayPaymentsLabel;
    @FXML private Label weekPaymentsLabel;
    @FXML private Label monthPaymentsLabel;

    private final PaymentService paymentService;
    private final CustomerService customerService;
    private final BillingService billingService;
    private ObservableList<Payment> paymentsList;

    public PaymentController() {
        this.paymentService = PaymentService.getInstance();
        this.customerService = CustomerService.getInstance();
        this.billingService = BillingService.getInstance();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        refreshData();
        updateSummary();
    }

    private void setupTableColumns() {
        referenceCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getReferenceNumber()));
        
        customerCol.setCellValueFactory(data -> {
            try {
                Customer customer = customerService.getCustomerById(data.getValue().getCustomerId());
                return new SimpleStringProperty(customer.getFullName());
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });
        
        invoiceCol.setCellValueFactory(data -> {
            String invoiceId = data.getValue().getInvoiceId();
            if (invoiceId == null || invoiceId.isEmpty()) {
                return new SimpleStringProperty("Account Payment");
            }
            try {
                Invoice invoice = billingService.getInvoiceById(invoiceId);
                return new SimpleStringProperty(invoice != null ? invoice.getInvoiceNumber() : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });
        
        amountCol.setCellValueFactory(data -> {
            BigDecimal amount = data.getValue().getAmount();
            return new SimpleStringProperty(amount != null ? String.format("£%.2f", amount) : "£0.00");
        });
        
        methodCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPaymentMethod().getDisplayName()));
        
        dateCol.setCellValueFactory(data -> 
            new SimpleStringProperty(DateUtil.formatForDisplay(data.getValue().getPaymentDate())));
        
        statusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));

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
                        case "Completed" -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        case "Pending" -> setStyle("-fx-text-fill: #f59e0b;");
                        case "Failed" -> setStyle("-fx-text-fill: #ef4444;");
                        case "Refunded" -> setStyle("-fx-text-fill: #8b5cf6;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        methodCombo.setItems(FXCollections.observableArrayList(
            "All Methods", "Cash", "Card", "Bank Transfer", "Direct Debit", "Cheque"
        ));
        methodCombo.setValue("All Methods");

        statusCombo.setItems(FXCollections.observableArrayList(
            "All Status", "Completed", "Pending", "Failed", "Refunded"
        ));
        statusCombo.setValue("All Status");
    }

    @FXML
    public void refreshData() {
        try {
            List<Payment> allPayments = paymentService.getAllPayments();
            paymentsList = FXCollections.observableArrayList(allPayments);
            paymentsTable.setItems(paymentsList);
        } catch (DataPersistenceException e) {
            showError("Failed to load payments: " + e.getMessage());
        }
    }

    private void updateSummary() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(7);
            LocalDate monthStart = today.withDayOfMonth(1);

            BigDecimal todayTotal = paymentService.getTotalPaymentsByDateRange(today, today);
            BigDecimal weekTotal = paymentService.getTotalPaymentsByDateRange(weekStart, today);
            BigDecimal monthTotal = paymentService.getTotalPaymentsByDateRange(monthStart, today);

            if (todayPaymentsLabel != null) {
                todayPaymentsLabel.setText(String.format("£%.2f", todayTotal));
            }
            if (weekPaymentsLabel != null) {
                weekPaymentsLabel.setText(String.format("£%.2f", weekTotal));
            }
            if (monthPaymentsLabel != null) {
                monthPaymentsLabel.setText(String.format("£%.2f", monthTotal));
            }
        } catch (DataPersistenceException e) {
            System.err.println("Error updating summary: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String methodFilter = methodCombo.getValue();
        String statusFilter = statusCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            List<Payment> allPayments = paymentService.getAllPayments();

            List<Payment> filtered = allPayments.stream()
                .filter(payment -> {
                    if (!searchText.isEmpty()) {
                        boolean matches = payment.getReferenceNumber().toLowerCase().contains(searchText);
                        if (!matches) {
                            try {
                                Customer c = customerService.getCustomerById(payment.getCustomerId());
                                matches = c.getFullName().toLowerCase().contains(searchText);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        if (!matches) return false;
                    }
                    
                    if (methodFilter != null && !"All Methods".equals(methodFilter)) {
                        if (!payment.getPaymentMethod().getDisplayName().equals(methodFilter)) {
                            return false;
                        }
                    }
                    
                    if (statusFilter != null && !"All Status".equals(statusFilter)) {
                        if (!payment.getStatus().getDisplayName().equals(statusFilter)) {
                            return false;
                        }
                    }
                    
                    if (fromDate != null && payment.getPaymentDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && payment.getPaymentDate().isAfter(toDate)) {
                        return false;
                    }
                    
                    return true;
                })
                .toList();

            paymentsList = FXCollections.observableArrayList(filtered);
            paymentsTable.setItems(paymentsList);
        } catch (DataPersistenceException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void showRecordPaymentDialog() {
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Record a new payment");

        ButtonType recordType = new ButtonType("Record Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(recordType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Customer> customerCombo = new ComboBox<>();
        ComboBox<Invoice> invoiceCombo = new ComboBox<>();
        TextField amountField = new TextField();
        ComboBox<Payment.PaymentMethod> methodCombo = new ComboBox<>();
        TextField referenceField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(2);

        try {
            customerCombo.setItems(FXCollections.observableArrayList(customerService.getActiveCustomers()));
            customerCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Customer c) {
                    return c == null ? "" : c.getAccountNumber() + " - " + c.getFullName();
                }
                @Override
                public Customer fromString(String s) { return null; }
            });
        } catch (DataPersistenceException e) {
            showError("Failed to load customers");
            return;
        }

        // When customer changes, load their unpaid invoices
        customerCombo.setOnAction(e -> {
            Customer customer = customerCombo.getValue();
            if (customer != null) {
                try {
                    List<Invoice> unpaid = billingService.getCustomerInvoices(customer.getCustomerId())
                        .stream()
                        .filter(inv -> inv.getBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                        .toList();
                    invoiceCombo.setItems(FXCollections.observableArrayList(unpaid));
                    invoiceCombo.setConverter(new javafx.util.StringConverter<>() {
                        @Override
                        public String toString(Invoice inv) {
                            return inv == null ? "" : inv.getInvoiceNumber() + " - £" + inv.getBalanceDue();
                        }
                        @Override
                        public Invoice fromString(String s) { return null; }
                    });
                } catch (DataPersistenceException ex) {
                    showError("Failed to load invoices");
                }
            }
        });

        // When invoice is selected, populate amount
        invoiceCombo.setOnAction(e -> {
            Invoice invoice = invoiceCombo.getValue();
            if (invoice != null) {
                amountField.setText(invoice.getBalanceDue().toString());
            }
        });

        methodCombo.setItems(FXCollections.observableArrayList(Payment.PaymentMethod.values()));
        methodCombo.setValue(Payment.PaymentMethod.DEBIT_CARD);
        methodCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Payment.PaymentMethod m) {
                return m == null ? "" : m.getDisplayName();
            }
            @Override
            public Payment.PaymentMethod fromString(String s) { return null; }
        });

        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Invoice (optional):"), 0, 1);
        grid.add(invoiceCombo, 1, 1);
        grid.add(new Label("Amount (£):"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Payment Method:"), 0, 3);
        grid.add(methodCombo, 1, 3);
        grid.add(new Label("Reference:"), 0, 4);
        grid.add(referenceField, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == recordType) {
                Customer customer = customerCombo.getValue();
                if (customer == null) {
                    showError("Please select a customer");
                    return null;
                }
                
                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountField.getText().trim());
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        showError("Amount must be positive");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid amount");
                    return null;
                }

                try {
                    Invoice invoice = invoiceCombo.getValue();
                    Payment payment;
                    
                    if (invoice != null) {
                        payment = paymentService.recordPayment(
                            customer.getCustomerId(),
                            invoice.getInvoiceId(),
                            amount,
                            methodCombo.getValue()
                        );
                    } else {
                        payment = paymentService.recordAccountPayment(
                            customer.getCustomerId(),
                            amount,
                            methodCombo.getValue()
                        );
                    }
                    
                    if (!referenceField.getText().trim().isEmpty()) {
                        payment.setReferenceNumber(referenceField.getText().trim());
                    }
                    if (!notesArea.getText().trim().isEmpty()) {
                        payment.setNotes(notesArea.getText().trim());
                    }
                    
                    return payment;
                } catch (CustomerNotFoundException | ValidationException | 
                         InsufficientPaymentException | DataPersistenceException e) {
                    showError("Failed to record payment: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Payment> result = dialog.showAndWait();
        result.ifPresent(payment -> {
            showSuccess("Payment recorded: " + payment.getReferenceNumber());
            refreshData();
            updateSummary();
        });
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

