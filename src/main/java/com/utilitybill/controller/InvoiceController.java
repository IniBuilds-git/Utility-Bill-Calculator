package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.*;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.PaymentService;
import com.utilitybill.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceController {

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private Label totalBilledLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label balanceDueLabel;

    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, String> invoiceIdCol;
    @FXML private TableColumn<Invoice, String> periodCol;
    @FXML private TableColumn<Invoice, String> meterTypeCol;
    @FXML private TableColumn<Invoice, String> tariffCol;
    @FXML private TableColumn<Invoice, String> kwhCol;
    @FXML private TableColumn<Invoice, String> totalCol;
    @FXML private TableColumn<Invoice, String> paidCol;
    @FXML private TableColumn<Invoice, String> balanceCol;
    @FXML private TableColumn<Invoice, String> statusCol;

    private final BillingService billingService = BillingService.getInstance();
    private final CustomerService customerService = CustomerService.getInstance();
    private final PaymentService paymentService = PaymentService.getInstance();

    @FXML
    public void initialize() {
        invoiceIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInvoiceNumber()));
        periodCol.setCellValueFactory(d -> new SimpleStringProperty(
                DateUtil.formatForDisplay(d.getValue().getPeriodStart()) + " - " + DateUtil.formatForDisplay(d.getValue().getPeriodEnd())));
        meterTypeCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMeterType() == null ? "-" : d.getValue().getMeterType().getDisplayName()));
        tariffCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTariffName() == null ? "-" : d.getValue().getTariffName()));
        kwhCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getKWh())));
        totalCol.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getTotalAmount())));
        paidCol.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getAmountPaid())));
        balanceCol.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getBalanceDue())));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().getDisplayName()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    setOnMouseClicked(null);
                    setCursor(javafx.scene.Cursor.DEFAULT);
                } else {
                    setText(item);
                    Invoice invoice = getTableView().getItems().get(getIndex());
                    if (invoice.getStatus() == Invoice.InvoiceStatus.PENDING || invoice.getStatus() == Invoice.InvoiceStatus.OVERDUE) {
                        setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold; -fx-underline: true;");
                        setCursor(javafx.scene.Cursor.HAND);
                        setOnMouseClicked(event -> {
                            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                                handleQuickPayment(invoice);
                            }
                        });
                    } else {
                        setStyle("");
                        setCursor(javafx.scene.Cursor.DEFAULT);
                        setOnMouseClicked(null);
                    }
                }
            }
        });

        invoiceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Double-click to show summary
        invoiceTable.setRowFactory(tv -> {
            TableRow<Invoice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Invoice rowData = row.getItem();
                    showSummaryDialog(rowData.getRunSummary());
                }
            });
            return row;
        });

        // Populate customer dropdown
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
            customerComboBox.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Customer c) {
                    if (c == null) return "";
                    return c.getFullName() + " (" + c.getAccountNumber() + ")";
                }
                @Override public Customer fromString(String s) { return null; }
            });
        } catch (DataPersistenceException e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    @FXML
    public void handleLoadInvoices() {
        Customer customer = customerComboBox.getValue();
        if (customer == null) {
            showError("Please select a customer.");
            return;
        }
        try {
            List<Invoice> list = billingService.getCustomerInvoices(customer.getCustomerId());
            invoiceTable.setItems(FXCollections.observableArrayList(list));
            updateSummary(list);
        } catch (DataPersistenceException e) {
            showError("Failed to load invoices: " + e.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        customerComboBox.setValue(null);
        invoiceTable.setItems(FXCollections.observableArrayList());
        clearSummary();
    }

    @FXML
    public void handleGenerateInvoice() {
        Customer customer = customerComboBox.getValue();
        if (customer == null) {
            showError("Please select a customer first.");
            return;
        }

        try {
            List<MeterReading> readings = billingService.getCustomerReadings(customer.getCustomerId());
            if (readings.isEmpty()) {
                showError("No meter readings found for this account. Add meter readings first.");
                return;
            }

            Dialog<MeterReading> pick = new Dialog<>();
            pick.setTitle("Select Meter Reading");
            pick.setHeaderText("Choose the meter reading record to invoice for " + customer.getFullName());

            ButtonType selectBtn = new ButtonType("Generate Invoice", ButtonBar.ButtonData.OK_DONE);
            pick.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);

            ComboBox<MeterReading> cb = new ComboBox<>(FXCollections.observableArrayList(readings));
            cb.getSelectionModel().selectLast();

            cb.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(MeterReading r) {
                    if (r == null) return "";
                    return DateUtil.formatForDisplay(r.getReadingDate()) + " (Value: " + r.getReadingValue() + ")";
                }
                @Override public MeterReading fromString(String s) { return null; }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.add(new Label("Select Reading:"), 0, 0);
            grid.add(cb, 1, 0);

            pick.getDialogPane().setContent(grid);
            pick.setResultConverter(btn -> btn == selectBtn ? cb.getSelectionModel().getSelectedItem() : null);

            Optional<MeterReading> chosenOpt = pick.showAndWait();
            if (chosenOpt.isPresent()) {
                MeterReading chosen = chosenOpt.get();
                Invoice inv = billingService.generateInvoice(
                    customer.getCustomerId(),
                    chosen.getPeriodStartDate() != null ? chosen.getPeriodStartDate() : chosen.getReadingDate().minusMonths(1),
                    chosen.getReadingDate()
                );
                handleLoadInvoices();
                showSummaryDialog(inv.getRunSummary());
            }
        } catch (Exception e) {
            showError("Failed to generate invoice: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddPayment() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an invoice to pay.");
            return;
        }

        BigDecimal balance = selected.getBalanceDue();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            showError("This invoice is already fully paid.");
            return;
        }

        Dialog<BigDecimal> dialog = new Dialog<>();
        dialog.setTitle("Pay Invoice (Full Balance)");

        ButtonType payBtn = new ButtonType("Pay Full", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField amountField = new TextField(formatMoney(balance));
        amountField.setDisable(true);
        DatePicker datePicker = new DatePicker(LocalDate.now());

        grid.addRow(0, new Label("Amount (£):"), amountField);
        grid.addRow(1, new Label("Payment Date:"), datePicker);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == payBtn ? balance : null);

        dialog.showAndWait().ifPresent(amount -> {
            try {
                paymentService.recordPayment(selected.getInvoiceId(), amount, Payment.PaymentMethod.BANK_TRANSFER);
                handleLoadInvoices();
                showSummaryDialog(selected.getRunSummary());
            } catch (Exception e) {
                showError("Payment failed: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleDeleteInvoice() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an invoice to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Invoice");
        confirm.setHeaderText("Cancel invoice " + selected.getInvoiceNumber() + "?");
        confirm.setContentText("This will mark the invoice as CANCELLED and credit the customer's account balance. This action cannot be undone.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                billingService.cancelInvoice(selected.getInvoiceId());
                handleLoadInvoices();
            } catch (Exception e) {
                showError("Cancellation failed: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleViewSummary() {
        List<Invoice> selectedItems = invoiceTable.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            showError("Select at least one invoice to view summary.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Invoice inv : selectedItems) {
            sb.append(inv.getRunSummary()).append("\n\n");
        }
        showSummaryDialog(sb.toString().trim());
    }

    private void updateSummary(List<Invoice> list) {
        BigDecimal totalBilled = list.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = list.stream().map(Invoice::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = totalBilled.subtract(totalPaid);

        totalBilledLabel.setText(formatMoney(totalBilled));
        totalPaidLabel.setText(formatMoney(totalPaid));
        balanceDueLabel.setText(formatMoney(balance));
    }

    private void clearSummary() {
        totalBilledLabel.setText("£0.00");
        totalPaidLabel.setText("£0.00");
        balanceDueLabel.setText("£0.00");
    }

    private void handleQuickPayment(Invoice selected) {
        BigDecimal balance = selected.getBalanceDue();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quick Payment");
        confirm.setHeaderText("Pay full balance?");
        confirm.setContentText(String.format("Record full payment of %s for Invoice %s?",
                formatMoney(balance), selected.getInvoiceNumber()));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    paymentService.recordPayment(selected.getInvoiceId(), balance, Payment.PaymentMethod.BANK_TRANSFER);
                    handleLoadInvoices();
                    showSummaryDialog(selected.getRunSummary());
                } catch (Exception e) {
                    showError("Payment failed: " + e.getMessage());
                }
            }
        });
    }

    private void showSummaryDialog(String summary) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detailed Bill Summary");
        dialog.setHeaderText(null);

        TextArea textArea = new TextArea(summary);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px;");

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String formatMoney(BigDecimal gbp) {
        if (gbp == null) return "£0.00";
        return "£" + gbp.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
