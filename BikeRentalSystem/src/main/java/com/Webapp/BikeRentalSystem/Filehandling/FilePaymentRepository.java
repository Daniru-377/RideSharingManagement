package com.Webapp.BikeRentalSystem.Filehandling;

import com.Webapp.BikeRentalSystem.Model.Payment;
import com.Webapp.BikeRentalSystem.Model.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;


@Repository
public class FilePaymentRepository implements PaymentRepository {
    private static final String FILE_NAME = "src/main/resources/paymentrecords.txt";

    @Override
    public void save(Payment payment) throws IOException {            // CREATE
        FileWriter writer = new FileWriter(FILE_NAME, true);          // true = add to the end
        writer.write(toLine(payment) + "\n");
        writer.close();
    }

    @Override
    public ArrayList<Payment> findAll() throws IOException {          // READ
        ArrayList<Payment> payments = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return payments;                                          // no file yet = no payments yet
        }

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] p = line.split(",");
            payments.add(new Payment(p[0], p[1], p[2], Double.parseDouble(p[3]),
                    LocalDate.parse(p[4]), p[5], p[6]));
        }
        scanner.close();
        return payments;
    }

    @Override
    public boolean update(String paymentId, String newStatus) throws IOException {   // UPDATE
        ArrayList<Payment> payments = findAll();
        boolean found = false;
        for (Payment p : payments) {
            if (p.getPaymentId().equals(paymentId)) {
                p.setStatus(newStatus);
                found = true;
            }
        }
        if (found) {
            writeAll(payments);
        }
        return found;
    }

    @Override
    public boolean delete(String paymentId) throws IOException {                     // DELETE
        ArrayList<Payment> payments = findAll();
        boolean found = false;
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentId().equals(paymentId)) {
                payments.remove(i);
                found = true;
                break;
            }
        }
        if (found) {
            writeAll(payments);
        }
        return found;
    }

    // ---- private helpers ----

    private void writeAll(ArrayList<Payment> payments) throws IOException {
        FileWriter writer = new FileWriter(FILE_NAME, false);         // false = start the file again
        for (Payment p : payments) {
            writer.write(toLine(p) + "\n");
        }
        writer.close();
    }

    private String toLine(Payment p) {
        return p.getPaymentId() + "," + p.getBookingId() + "," + p.getUserId() + "," + p.getAmount()
                + "," + p.getPaymentDate() + "," + p.getStatus() + "," + p.getMethod();
    }
}
