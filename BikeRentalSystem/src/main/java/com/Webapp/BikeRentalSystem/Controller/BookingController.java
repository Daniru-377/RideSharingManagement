package com.Webapp.BikeRentalSystem.Controller;

import com.Webapp.BikeRentalSystem.DTO.BookingDTO;
import com.Webapp.BikeRentalSystem.DTO.BookingResponse;
import com.Webapp.BikeRentalSystem.Model.User;
import com.Webapp.BikeRentalSystem.Service.BookingService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String getAllBookings(HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        if ("Admin".equalsIgnoreCase(loggedInUser.getRole())) {

            BookingResponse response = bookingService.getAllBookings();

            model.addAttribute("bookings", response.getBookings());

            return "admin/bookings";
        }

        BookingResponse response = bookingService.getBookingByUserId(loggedInUser.getUserId());

        model.addAttribute("bookings", response.getBookings());

        return "bookings/list";
    }

    @GetMapping("/{id}")
    public String getBookingByUserId(@PathVariable String id, HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        if (!"Admin".equals(loggedInUser.getRole()) && !loggedInUser.getUserId().equals(id)) {
            return "redirect:/bookings/" + loggedInUser.getUserId();
        }

        BookingResponse response = bookingService.getBookingByUserId(id);

        model.addAttribute("bookings", response.getBookings());

        return "bookings/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("booking", new BookingDTO());

        return "bookings/create";
    }

    @PostMapping("/save")
    public String saveBooking(HttpSession session,@ModelAttribute BookingDTO bookingDTO) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        bookingService.saveBooking(loggedInUser.getUserId(),bookingDTO);

        return "redirect:/bookings";
    }

    @GetMapping("/edit/{bookingId}")
    public String editForm(@PathVariable String bookingId, Model model) {

        BookingResponse response = bookingService.getBookingById(bookingId);

        model.addAttribute("booking", response.getBooking());

        return "bookings/edit";
    }

    @PostMapping("/update/{bookingId}")
    public String updateBooking(@PathVariable String bookingId, @ModelAttribute BookingDTO bookingDTO) {

        bookingService.updateBooking(bookingId, bookingDTO);

        return "redirect:/bookings";
    }

    @PostMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable String bookingId) {

        bookingService.deleteBooking(bookingId);

        return "redirect:/bookings";
    }

    @GetMapping("/new")
    public String showNewBookingForm(@RequestParam("bikeId") String bikeId, Model model) {

        BookingDTO booking = new BookingDTO();
        booking.setBikeId(bikeId);

        model.addAttribute("booking", booking);

        return "bookings/create";
    }
}
