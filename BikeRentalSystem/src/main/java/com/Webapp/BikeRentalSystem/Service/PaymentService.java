package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.Model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class PaymentService {
    private PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public Payment pay(Billing billing, LocalDate start, LocalDate end, String method)
            throws PaymentException, IOException {
        double amount = billing.calculateTotal(start, end);
        String id = Payment.createId(repository.findAll().size());

        // Use billing.getUserId() instead of booking.getUserId()
        LocalDate today = LocalDate.now();

        Payment payment;
        if (method.equals("cash")) {
            payment = new Cash(id, billing.getBookingId(), billing.getUserId(), amount, today);
        } else if (method.equals("online")) {
            payment = new Online(id, billing.getBookingId(), billing.getUserId(), amount, today);
        } else if (method.equals("card")) {
            throw new PaymentException("Card details are required.");
        } else {
            throw new PaymentException("Please choose a payment method.");
        }
        return process(payment);
    }

    public Payment pay(Billing billing, LocalDate start, LocalDate end, String method,
                       String cardName, String cardNumber, String expiry, String cvv)
            throws PaymentException, IOException {
        double amount = billing.calculateTotal(start, end);
        String id = Payment.createId(repository.findAll().size());

        Card card = new Card(id, billing.getBookingId(), billing.getUserId(), amount, LocalDate.now());
        card.setCardDetails(cardName, cardNumber, expiry, cvv);
        return process(card);
    }

    private Payment process(Payment payment) throws PaymentException, IOException {
        payment.processPayment();
        repository.save(payment);
        payment.displayPaymentDetails("New payment");
        return payment;
    }

    public java.util.ArrayList<Payment> getAllPayments() throws IOException {
        return repository.findAll();
    }

    public void updateStatus(String paymentId, String newStatus) throws PaymentException, IOException {
        if (!newStatus.equals("Paid") && !newStatus.equals("Pending")) {
            throw new PaymentException("Status must be Paid or Pending.");
        }
        if (!repository.update(paymentId, newStatus)) {
            throw new PaymentException("Payment not found: " + paymentId);
        }
    }

    public void deletePayment(String paymentId) throws PaymentException, IOException {
        if (!repository.delete(paymentId)) {
            throw new PaymentException("Payment not found: " + paymentId);
        }
    }
}