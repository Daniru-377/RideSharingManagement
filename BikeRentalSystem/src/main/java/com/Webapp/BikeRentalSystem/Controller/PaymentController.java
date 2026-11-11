package com.Webapp.BikeRentalSystem.Controller;

import com.Webapp.BikeRentalSystem.Model.Billing;
import com.Webapp.BikeRentalSystem.Model.Payment;
import com.Webapp.BikeRentalSystem.Model.User;
import com.Webapp.BikeRentalSystem.Service.PaymentService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 1. Display All Payments (Admin sees all, or handle accordingly)
    @GetMapping
    public String getAllPayments(HttpSession session, Model model, RedirectAttributes ra) {
        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            List<Payment> payments = paymentService.getAllPayments();
            model.addAttribute("payments", payments);
            model.addAttribute("role", loggedInUser.getRole());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error loading payments: " + e.getMessage());
        }

        return "payments/list";
    }

    // 2. Display Booking Details for Payment initiation
    @GetMapping("/booking")
    public String getBooking(@RequestParam String id, HttpSession session, Model model, RedirectAttributes ra) {
        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            Billing billing = new Billing(id);
            model.addAttribute("booking", billing);
            return "payments/booking";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings";
        }
    }

    // 3. Calculate Rental Cost
    @GetMapping("/calculate")
    public String calculate(
            @RequestParam String id,
            @RequestParam String start,
            @RequestParam String end,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            Billing billing = new Billing(id);
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            int days = billing.calculateRentalDays(startDate, endDate);
            double total = billing.calculateTotal(days);

            model.addAttribute("booking", billing);
            model.addAttribute("days", days);
            model.addAttribute("pricePerDay", billing.getPricePerDay());
            model.addAttribute("total", total);
            model.addAttribute("start", start);
            model.addAttribute("end", end);

            return "payments/calculate";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/payments/booking?id=" + id;
        }
    }

    // 4. Display Payment Form
    @GetMapping("/pay")
    public String paymentForm(
            @RequestParam String bookingId,
            @RequestParam String start,
            @RequestParam String end,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        return "payments/pay";
    }

    // 5. Process Payment (Cash, Online, Card)
    @PostMapping("/pay")
    public String pay(
            @RequestParam String bookingId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String method,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expiry,
            @RequestParam(required = false) String cvv,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            Billing billing = new Billing(bookingId);
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            Payment payment;

            if ("card".equals(method)) {
                payment = paymentService.pay(
                        billing, startDate, endDate, method,
                        cardName, cardNumber, expiry, cvv
                );
            } else {
                payment = paymentService.pay(
                        billing, startDate, endDate, method
                );
            }

            model.addAttribute("payment", payment);
            return "payments/success";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/payments/pay?bookingId=" + bookingId + "&start=" + start + "&end=" + end;
        }
    }

    // 6. Update Payment Status (Admin / System)
    @PostMapping("/update")
    public String update(
            @RequestParam String paymentId,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes ra) {

        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            paymentService.updateStatus(paymentId, status);
            ra.addFlashAttribute("message", "Payment status updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payments";
    }

    // 7. Delete Payment
    @PostMapping("/delete/{paymentId}")
    public String deletePayment(
            @PathVariable String paymentId,
            HttpSession session,
            RedirectAttributes ra) {

        User loggedInUser = currentUser(session);
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        try {
            paymentService.deletePayment(paymentId);
            ra.addFlashAttribute("message", "Payment deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payments";
    }

    // Helper method to get logged-in user from session (matches BookingController style)
    private User currentUser(HttpSession session) {
        Object user = session.getAttribute("loggedInUser");
        if (user instanceof User) {
            return (User) user;
        }
        return null;
    }
}