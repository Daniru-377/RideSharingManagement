package com.Webapp.BikeRentalSystem.Model;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Billing {
    private String bookingId;
    private String userId;
    private double pricePerDay;
    private int defaultDays;
    private String customerName;
    private String email;
    private String phone;

    public Billing(String bookingId) throws PaymentException, IOException {
        this.bookingId = bookingId;
        File file = new File("src/main/resources/data/payments.txt");

        if (!file.exists()) {
            throw new PaymentException("Could not find payments.txt in the project");

        }

        try (Scanner scanner = new Scanner(file)) { //try with resources
            if (scanner.hasNextLine())
                scanner.nextLine();
            boolean found = false;

            while (scanner.hasNextLine()) {
                String[] c = scanner.nextLine().trim().split(",");
                if (c.length >= 7 && c[0].trim().equalsIgnoreCase(bookingId)) {
                    this.userId = c[1].trim();
                    this.pricePerDay = Double.parseDouble(c[2].trim());
                    this.defaultDays = Integer.parseInt(c[3].trim());
                    this.customerName = c[4].trim();
                    this.email = c[5].trim();
                    this.phone = c[6].trim();
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new PaymentException("Booking not found: " + bookingId);
            }
        }
        catch (IOException e) {
            throw new PaymentException("Could not read payments.txt");
        }
    }

    public int calculateRentalDays(LocalDate start, LocalDate end) throws PaymentException {
        if (start.isBefore(LocalDate.now())) {
            throw new PaymentException("Start date cannot be in the past.");
        }
        if (end.isBefore(start)) {
            throw new PaymentException("End date must be on or after the start date.");
        }
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    public double calculateTotal(int days) {
        return days * pricePerDay;
    }

    public double calculateTotal(LocalDate start, LocalDate end) throws PaymentException {
        return calculateTotal(calculateRentalDays(start, end));
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public double getPricePerDay() { return pricePerDay; }
    public int getDefaultDays() { return defaultDays; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setDefaultDays(int defaultDays) {
        this.defaultDays = defaultDays;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}