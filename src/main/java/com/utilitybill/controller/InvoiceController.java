package com.utilitybill.controller;

import com.utilitybill.exception.CustomerNotFoundException;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.exception.ValidationException;
import com.utilitybill.model.Customer;
import com.utilitybill.model.Invoice;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for invoice management view.
 * Handles invoice generation, display, and management.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class InvoiceController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<Invoice> invoicesTable;
    @FXML private TableColumn<Invoice, String> invoiceNumberCol;
    @FXML private TableColumn<Invoice, String> customerCol;
    @FXML private TableColumn<Invoice, String> accountCol;
    @FXML private TableColumn<Invoice, String> periodCol;
    @FXML private TableColumn<Invoice, String> amountCol;
    @FXML private TableColumn<Invoice, String> paidCol;
    @FXML private TableColumn<Invoice, String> balanceCol;
    @FXML private TableColumn<Invoice, String> statusCol;
    @FXML private TableColumn<Invoice, String> dueDateCol;
    @FXML private TableColumn<Invoice, Void> actionsCol;

    private final BillingService billingService;
    private final CustomerService customerService;
    private ObservableList<Invoice> invoicesList;

    public InvoiceController() {
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
        invoiceNumberCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        
        customerCol.setCellValueFactory(data -> {
            try {
                Customer customer = customerService.getCustomerById(data.getValue().getCustomerId());
                return new SimpleStringProperty(customer.getFullName());
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });
        
        accountCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAccountNumber()));
        
        periodCol.setCellValueFactory(data -> {
            Invoice inv = data.getValue();
            return new SimpleStringProperty(
                DateUtil.formatForDisplay(inv.getPeriodStart()) + " - " + 
                DateUtil.formatForDisplay(inv.getPeriodEnd()));
        });
        
        amountCol.setCellValueFactory(data -> {
            BigDecimal amount = data.getValue().getTotalAmount();
            return new SimpleStringProperty(amount != null ? String.format("£%.2f", amount) : "£0.00");
        });
        
        paidCol.setCellValueFactory(data -> {
            BigDecimal paid = data.getValue().getAmountPaid();
            return new SimpleStringProperty(paid != null ? String.format("£%.2f", paid) : "£0.00");
        });
        
        balanceCol.setCellValueFactory(data -> {
            BigDecimal balance = data.getValue().getBalanceDue();
            return new SimpleStringProperty(balance != null ? String.format("£%.2f", balance) : "£0.00");
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
                        case "Paid" -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        case "Overdue" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        case "Pending" -> setStyle("-fx-text-fill: #f59e0b;");
                        case "Cancelled" -> setStyle("-fx-text-fill: #94a3b8;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Actions column with View button
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 4;");
                viewBtn.setOnAction(e -> {
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    showInvoiceDetails(invoice);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
    }

    private void setupFilters() {
        statusCombo.setItems(FXCollections.observableArrayList(
            "All Status", "Pending", "Paid", "Overdue", "Cancelled"
        ));
        statusCombo.setValue("All Status");
    }

    @FXML
    public void refreshData() {
        try {
            List<Invoice> allInvoices = new ArrayList<>();
            for (Customer customer : customerService.getAllCustomers()) {
                allInvoices.addAll(billingService.getCustomerInvoices(customer.getCustomerId()));
            }
            invoicesList = FXCollections.observableArrayList(allInvoices);
            invoicesTable.setItems(invoicesList);
        } catch (DataPersistenceException e) {
            showError("Failed to load invoices: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = statusCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            List<Invoice> allInvoices = new ArrayList<>();
            for (Customer customer : customerService.getAllCustomers()) {
                allInvoices.addAll(billingService.getCustomerInvoices(customer.getCustomerId()));
            }

            List<Invoice> filtered = allInvoices.stream()
                .filter(inv -> {
                    if (!searchText.isEmpty()) {
                        boolean matches = inv.getInvoiceNumber().toLowerCase().contains(searchText) ||
                                         inv.getAccountNumber().toLowerCase().contains(searchText);
                        if (!matches) {
                            try {
                                Customer c = customerService.getCustomerById(inv.getCustomerId());
                                matches = c.getFullName().toLowerCase().contains(searchText);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        if (!matches) return false;
                    }
                    
                    if (statusFilter != null && !"All Status".equals(statusFilter)) {
                        if (!inv.getStatus().getDisplayName().equals(statusFilter)) {
                            return false;
                        }
                    }
                    
                    if (fromDate != null && inv.getIssueDate().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && inv.getIssueDate().isAfter(toDate)) {
                        return false;
                    }
                    
                    return true;
                })
                .toList();

            invoicesList = FXCollections.observableArrayList(filtered);
            invoicesTable.setItems(invoicesList);
        } catch (DataPersistenceException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void showGenerateInvoiceDialog() {
        Dialog<Invoice> dialog = new Dialog<>();
        dialog.setTitle("Generate Invoice");
        dialog.setHeaderText("Generate a new invoice for a customer");

        ButtonType generateType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Customer> customerCombo = new ComboBox<>();
        DatePicker periodStart = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker periodEnd = new DatePicker(LocalDate.now());

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

        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Period Start:"), 0, 1);
        grid.add(periodStart, 1, 1);
        grid.add(new Label("Period End:"), 0, 2);
        grid.add(periodEnd, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateType) {
                Customer customer = customerCombo.getValue();
                if (customer == null) {
                    showError("Please select a customer");
                    return null;
                }
                try {
                    return billingService.generateInvoice(
                        customer.getCustomerId(),
                        periodStart.getValue(),
                        periodEnd.getValue()
                    );
                } catch (CustomerNotFoundException | ValidationException | DataPersistenceException e) {
                    showError("Failed to generate invoice: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Invoice> result = dialog.showAndWait();
        result.ifPresent(invoice -> {
            showSuccess("Invoice " + invoice.getInvoiceNumber() + " generated successfully!");
            refreshData();
            showInvoiceDetails(invoice);
        });
    }

    private void showInvoiceDetails(Invoice invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Details");
        alert.setHeaderText("Invoice: " + invoice.getInvoiceNumber());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().addAll(
            new Label("Account: " + invoice.getAccountNumber()),
            new Label("Period: " + DateUtil.formatForDisplay(invoice.getPeriodStart()) + 
                     " - " + DateUtil.formatForDisplay(invoice.getPeriodEnd())),
            new Label("─".repeat(40)),
            new Label("Opening Reading: " + String.format("%.2f", invoice.getOpeningReading())),
            new Label("Closing Reading: " + String.format("%.2f", invoice.getClosingReading())),
            new Label("Units Consumed: " + String.format("%.2f kWh", invoice.getUnitsConsumed())),
            new Label("─".repeat(40)),
            new Label("Unit Cost: " + String.format("£%.2f", invoice.getUnitCost())),
            new Label("Standing Charge: " + String.format("£%.2f", invoice.getStandingChargeTotal())),
            new Label("Subtotal: " + String.format("£%.2f", invoice.getSubtotal())),
            new Label("VAT (" + invoice.getVatRate().multiply(BigDecimal.valueOf(100)) + "%): " + 
                     String.format("£%.2f", invoice.getVatAmount())),
            new Label("─".repeat(40)),
            new Label("TOTAL: " + String.format("£%.2f", invoice.getTotalAmount())),
            new Label("Amount Paid: " + String.format("£%.2f", invoice.getAmountPaid())),
            new Label("Balance Due: " + String.format("£%.2f", invoice.getBalanceDue())),
            new Label("─".repeat(40)),
            new Label("Status: " + invoice.getStatus().getDisplayName()),
            new Label("Due Date: " + DateUtil.formatForDisplay(invoice.getDueDate()))
        );
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
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

