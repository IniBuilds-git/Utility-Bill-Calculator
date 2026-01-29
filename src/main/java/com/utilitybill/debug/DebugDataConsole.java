package com.utilitybill.debug;

import com.utilitybill.model.Customer;
import com.utilitybill.model.Invoice;
import com.utilitybill.model.Payment;
import com.utilitybill.service.BillingService;
import com.utilitybill.service.CustomerService;
import com.utilitybill.service.PaymentService;

import java.util.List;

public class DebugDataConsole {
    public static void main(String[] args) {
        try {
            System.out.println("DEBUG: Starting Data Inspection...");
            
            CustomerService cs = CustomerService.getInstance();
            BillingService bs = BillingService.getInstance();
            PaymentService ps = PaymentService.getInstance();

            Customer customer = cs.getCustomerByAccountNumber("ACC-100000");
            System.out.println("Customer Found: " + customer.getFullName());
            System.out.println("Current Balance: " + customer.getAccountBalance());
            System.out.println("Has Debt: " + customer.hasDebt());

            System.out.println("\n--- INVOICES ---");
            List<Invoice> invoices = bs.getCustomerInvoices(customer.getCustomerId());
            for (Invoice inv : invoices) {
                System.out.printf("Inv: %s | Status: %s | Total: %s | Paid: %s | Due: %s%n",
                        inv.getInvoiceNumber(), inv.getStatus(), inv.getTotalAmount(), inv.getAmountPaid(), inv.getBalanceDue());
            }

            System.out.println("\n--- PAYMENTS ---");
            List<Payment> payments = ps.getCustomerPayments(customer.getCustomerId());
            for (Payment pay : payments) {
                System.out.printf("Pay: %s | Date: %s | Amount: %s | Method: %s%n",
                        pay.getReferenceNumber(), pay.getPaymentDate(), pay.getAmount(), pay.getPaymentMethod());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
