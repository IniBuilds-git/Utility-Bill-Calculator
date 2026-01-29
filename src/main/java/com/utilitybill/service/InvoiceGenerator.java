package com.utilitybill.service;

import com.utilitybill.model.Invoice;
import java.io.File;

import com.utilitybill.exception.DocumentGenerationException;

public interface InvoiceGenerator {
    void generateInvoice(Invoice invoice, File destination) throws DocumentGenerationException;
}
