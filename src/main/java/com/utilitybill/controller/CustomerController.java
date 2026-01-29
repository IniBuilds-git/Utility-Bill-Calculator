package com.utilitybill.controller;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.Customer;
import com.utilitybill.service.CustomerService;
import com.utilitybill.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.utilitybill.util.AppLogger;
import com.utilitybill.util.FormatUtil;
import com.utilitybill.util.ViewUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CustomerController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> accountNumberCol;
    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> emailCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    @FXML
    private TableColumn<Customer, String> addressCol;
    @FXML
    private TableColumn<Customer, String> balanceCol;
    @FXML
    private TableColumn<Customer, String> statusCol;
    @FXML
    private TableColumn<Customer, Void> actionsCol;
    @FXML
    private Label summaryLabel;
    @FXML
    private Pagination pagination;

    private final CustomerService customerService;
    private ObservableList<Customer> customerList;

    public CustomerController() {
        this.customerService = CustomerService.getInstance();
    }

    @FXML
    public void initialize() {
        setupTableColumns();

        // Filter combo may not exist in all views - handle gracefully
        if (filterCombo != null) {
            filterCombo.setItems(FXCollections.observableArrayList(
                    "All Customers", "Active", "Inactive", "With Debt"));
            filterCombo.setValue("All Customers");
            filterCombo.setOnAction(e -> handleSearch());
        }

        refreshData();
        searchField.setOnAction(e -> handleSearch());
    }

    private void setupTableColumns() {
        accountNumberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNumber()));

        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        addressCol.setCellValueFactory(data -> {
            if (data.getValue().getServiceAddress() != null) {
                return new SimpleStringProperty(data.getValue().getServiceAddress().getInlineAddress());
            }
            return new SimpleStringProperty("");
        });

        balanceCol.setCellValueFactory(data -> {
            BigDecimal balance = data.getValue().getAccountBalance();
            String formatted = FormatUtil.formatCurrency(balance);
            return new SimpleStringProperty(formatted);
        });

        balanceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("-") || item.startsWith("£-")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981;");
                    }
                }
            }
        });

        statusCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Active".equals(item)) {
                        setStyle("-fx-text-fill: #10b981;");
                    } else {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                }
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final HBox buttons = new HBox(5, viewBtn, editBtn);

            {
                viewBtn.setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #0d9488; " +
                        "-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                        "-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> viewCustomer(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> editCustomer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    @FXML
    public void refreshData() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerList = FXCollections.observableArrayList(customers);
            customerTable.setItems(customerList);
            updateSummary(customers.size());
        } catch (DataPersistenceException e) {
            ViewUtil.showError("Error", "Failed to load customers: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String filter = filterCombo != null ? filterCombo.getValue() : "All Customers";

        try {
            List<Customer> customers;

            if ("Active".equals(filter)) {
                customers = customerService.getActiveCustomers();
            } else if ("Inactive".equals(filter)) {
                customers = customerService.getAllCustomers().stream()
                        .filter(c -> !c.isActive())
                        .toList();
            } else if ("With Debt".equals(filter)) {
                customers = customerService.getCustomersWithDebt();
            } else {
                customers = customerService.getAllCustomers();
            }

            if (!searchText.isEmpty()) {
                customers = customers.stream()
                        .filter(c -> c.getFullName().toLowerCase().contains(searchText) ||
                                c.getAccountNumber().toLowerCase().contains(searchText) ||
                                c.getEmail().toLowerCase().contains(searchText))
                        .toList();
            }

            customerList = FXCollections.observableArrayList(customers);
            customerTable.setItems(customerList);
            updateSummary(customers.size());

        } catch (DataPersistenceException e) {
            ViewUtil.showError("Error", "Search failed: " + e.getMessage());
        }
    }

    @FXML
    public void showAddCustomerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitybill/view/customer-dialog.fxml"));
            Parent root = loader.load();
            CustomerDialogController controller = loader.getController();
            controller.setMode(CustomerDialogController.Mode.ADD);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Customer");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (IOException e) {
            ViewUtil.showError("Error", "Failed to open dialog: " + e.getMessage());
        }
    }

    private void viewCustomer(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitybill/view/customer-details.fxml"));
            Parent root = loader.load();
            
            CustomerDetailsController controller = loader.getController();
            controller.setCustomer(customer);

            Stage stage = new Stage();
            stage.setTitle("Customer Dashboard - " + customer.getFullName());
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            AppLogger.error(getClass().getName(), "Failed to open customer details", e);
            ViewUtil.showError("Error", "Failed to open details view: " + e.getMessage());
        }
    }

    private void editCustomer(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitybill/view/customer-dialog.fxml"));
            Parent root = loader.load();
            CustomerDialogController controller = loader.getController();
            controller.setMode(CustomerDialogController.Mode.EDIT);
            controller.setCustomer(customer);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Customer");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
            }
        } catch (IOException e) {
            ViewUtil.showError("Error", "Failed to open dialog: " + e.getMessage());
        }
    }

    private void updateSummary(int count) {
        summaryLabel.setText(String.format("Showing %d customer%s", count, count == 1 ? "" : "s"));
    }


}
