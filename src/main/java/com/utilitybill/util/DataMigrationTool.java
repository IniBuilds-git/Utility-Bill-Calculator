package com.utilitybill.util;

import com.google.gson.*;
import com.utilitybill.dao.*;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.*;

import java.io.*;
import java.time.LocalDate;

/**
 * Utility to migrate data from JSON files to binary .dat files
 */
public class DataMigrationTool {

    private static final String CLASS_NAME = DataMigrationTool.class.getName();

    private static final String DATA_DIR = "data";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public static void main(String[] args) {
        AppLogger.info(CLASS_NAME, "=== Data Migration Tool: JSON to Binary .dat ===");

        try {
            migrateTariffs();
            migrateCustomers();
            migrateUsers();
            migrateInvoices();
            migratePayments();
            migrateMeterReadings();

            AppLogger.info(CLASS_NAME, "=== Migration completed successfully! ===");
            AppLogger.info(CLASS_NAME, "Data has been migrated to binary .dat format.");
            AppLogger.info(CLASS_NAME, "Old JSON files have been backed up with .json.bak extension.");

        } catch (Exception e) {
            AppLogger.error(CLASS_NAME, "Migration failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static void migrateTariffs() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating tariffs...");
        File jsonFile = new File(DATA_DIR + File.separator + "tariffs.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No tariffs.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            TariffDAO tariffDAO = TariffDAO.getInstance();

            int count = 0;
            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                String type = obj.get("_type").getAsString();

                Tariff tariff;
                if ("ElectricityTariff".equals(type)) {
                    tariff = GSON.fromJson(element, ElectricityTariff.class);
                } else if ("GasTariff".equals(type)) {
                    tariff = GSON.fromJson(element, GasTariff.class);
                } else {
                    continue;
                }

                tariffDAO.save(tariff);
                count++;
            }

            backupJsonFile(jsonFile);
            AppLogger.info(CLASS_NAME, "  Migrated " + count + " tariffs");
        }
    }

    private static void migrateCustomers() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating customers...");
        File jsonFile = new File(DATA_DIR + File.separator + "customers.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No customers.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            Customer[] customers = GSON.fromJson(reader, Customer[].class);
            CustomerDAO customerDAO = CustomerDAO.getInstance();

            if (customers != null) {
                for (Customer customer : customers) {
                    customerDAO.save(customer);
                }
                backupJsonFile(jsonFile);
                AppLogger.info(CLASS_NAME, "  Migrated " + customers.length + " customers");
            }
        }
    }

    private static void migrateUsers() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating users...");
        File jsonFile = new File(DATA_DIR + File.separator + "users.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No users.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            User[] users = GSON.fromJson(reader, User[].class);
            UserDAO userDAO = UserDAO.getInstance();

            if (users != null) {
                for (User user : users) {
                    userDAO.save(user);
                }
                backupJsonFile(jsonFile);
                AppLogger.info(CLASS_NAME, "  Migrated " + users.length + " users");
            }
        }
    }

    private static void migrateInvoices() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating invoices...");
        File jsonFile = new File(DATA_DIR + File.separator + "invoices.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No invoices.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            Invoice[] invoices = GSON.fromJson(reader, Invoice[].class);
            InvoiceDAO invoiceDAO = InvoiceDAO.getInstance();

            if (invoices != null) {
                for (Invoice invoice : invoices) {
                    invoiceDAO.save(invoice);
                }
                backupJsonFile(jsonFile);
                AppLogger.info(CLASS_NAME, "  Migrated " + invoices.length + " invoices");
            }
        }
    }

    private static void migratePayments() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating payments...");
        File jsonFile = new File(DATA_DIR + File.separator + "payments.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No payments.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            Payment[] payments = GSON.fromJson(reader, Payment[].class);
            PaymentDAO paymentDAO = PaymentDAO.getInstance();

            if (payments != null) {
                for (Payment payment : payments) {
                    paymentDAO.save(payment);
                }
                backupJsonFile(jsonFile);
                AppLogger.info(CLASS_NAME, "  Migrated " + payments.length + " payments");
            }
        }
    }

    private static void migrateMeterReadings() throws DataPersistenceException, IOException {
        AppLogger.info(CLASS_NAME, "Migrating meter readings...");
        File jsonFile = new File(DATA_DIR + File.separator + "meter_readings.json");

        if (!jsonFile.exists()) {
            AppLogger.info(CLASS_NAME, "  No meter_readings.json found, skipping");
            return;
        }

        try (Reader reader = new FileReader(jsonFile)) {
            MeterReading[] readings = GSON.fromJson(reader, MeterReading[].class);
            MeterReadingDAO readingDAO = MeterReadingDAO.getInstance();

            if (readings != null) {
                for (MeterReading reading : readings) {
                    readingDAO.save(reading);
                }
                backupJsonFile(jsonFile);
                AppLogger.info(CLASS_NAME, "  Migrated " + readings.length + " meter readings");
            }
        }
    }

    private static void backupJsonFile(File jsonFile) throws IOException {
        File backupFile = new File(jsonFile.getAbsolutePath() + ".bak");
        if (jsonFile.renameTo(backupFile)) {
            AppLogger.info(CLASS_NAME, "  Backed up to: " + backupFile.getName());
        }
    }
}
