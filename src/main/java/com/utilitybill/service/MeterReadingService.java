package com.utilitybill.service;

import com.utilitybill.exception.UtilityBillException;
import com.utilitybill.model.MeterReadingRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MeterReadingService {

    private static MeterReadingService instance;
    private static final String STORE_FILE = "meter_readings.dat";

    private final List<MeterReadingRecord> readings = new ArrayList<>();

    private MeterReadingService() {
        try { load(); }
        catch (UtilityBillException e) { System.err.println(e.getMessage()); }
    }

    public static synchronized MeterReadingService getInstance() {
        if (instance == null) instance = new MeterReadingService();
        return instance;
    }

    public MeterReadingRecord addReading(MeterReadingRecord r) throws UtilityBillException {
        validate(r);
        r.recalc();
        readings.add(r);
        save();
        return r;
    }

    public List<MeterReadingRecord> getReadingsForCustomer(String accountNumber) {
        if (accountNumber == null) return List.of();
        String acc = accountNumber.trim();
        return readings.stream()
                .filter(r -> acc.equals(r.getAccountNumber()))
                .sorted(Comparator.comparing(MeterReadingRecord::getClosingDate))
                .collect(Collectors.toList());
    }

    public void deleteReading(String readingId) throws UtilityBillException {
        boolean removed = readings.removeIf(r -> r.getReadingId().equals(readingId));
        if (!removed) throw new UtilityBillException("Reading not found.");
        save();
    }

    private void validate(MeterReadingRecord r) throws UtilityBillException {
        if (r == null) throw new UtilityBillException("Reading is required.");
        if (r.getAccountNumber() == null || r.getAccountNumber().trim().isEmpty())
            throw new UtilityBillException("Account number is required.");
        if (r.getMeterType() == null) throw new UtilityBillException("Meter type is required.");
        if (r.getOpeningDate() == null || r.getClosingDate() == null)
            throw new UtilityBillException("Opening and closing dates are required.");
        if (r.getClosingRead() < r.getOpeningRead())
            throw new UtilityBillException("Closing read must be >= opening read.");
    }

    @SuppressWarnings("unchecked")
    private void load() throws UtilityBillException {
        Path p = Path.of(STORE_FILE);
        if (!Files.exists(p)) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STORE_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                readings.clear();
                readings.addAll((List<MeterReadingRecord>) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new UtilityBillException("Failed to load meter readings.", e);
        }
    }

    private void save() throws UtilityBillException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORE_FILE))) {
            oos.writeObject(readings);
        } catch (IOException e) {
            throw new UtilityBillException("Failed to save meter readings.", e);
        }
    }
}
