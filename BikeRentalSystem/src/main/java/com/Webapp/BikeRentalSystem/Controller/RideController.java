package com.Webapp.BikeRentalSystem.Controller;

import com.Webapp.BikeRentalSystem.DTO.RideDTO;
import com.Webapp.BikeRentalSystem.Model.Ride;
import com.Webapp.BikeRentalSystem.Model.User;
import com.Webapp.BikeRentalSystem.Service.RideService;
import com.Webapp.BikeRentalSystem.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/rides")
public class RideController {

    private final RideService rideService;
    private final UserService userService;

    public RideController(RideService rideService, UserService userService) {
        this.rideService = rideService;
        this.userService = userService;
    }

    // ---------- LIST (Admin = all, Driver = own, Customer = own) ----------

    @GetMapping
    public String listRides(HttpSession session, Model model) {

        User user = currentUser(session);
        if (user == null) {
            return "redirect:/users/login";
        }

        String role = user.getRole();

        if ("Admin".equals(role)) {
            model.addAttribute("rides", rideService.readRides());
        } else if ("Driver".equals(role)) {
            model.addAttribute("rides", rideService.getRidesForDriver(user.getUserId()));
        } else {
            List<Ride> rides = rideService.getRidesForCustomer(user.getUserId());
            model.addAttribute("rides", rides);
            model.addAttribute("bookingIds", rideService.getBookingIdsForRides(user.getUserId(), rides));
        }

        return "admin/rides";
    }

    // ---------- CREATE (Customer only) ----------

    @GetMapping("/create")
    public String createRide(HttpSession session,
                             Model model,
                             @RequestParam(required = false) String bikeId) {

        if (!isCustomer(session)) {
            return "redirect:/rides";
        }

        RideDTO rideDTO = new RideDTO();

        if (bikeId != null) {
            rideDTO.setBikeId(bikeId);
        }

        model.addAttribute("rideDTO", rideDTO);
        model.addAttribute("drivers", userService.getAllDrivers());

        return "rides/create";
    }

    @PostMapping("/save")
    public String addRide(HttpSession session,
                          @ModelAttribute("rideDTO") RideDTO rideDTO,
                          BindingResult result,
                          Model model) {

        if (!isCustomer(session)) {
            return "redirect:/rides";
        }

        if (result.hasErrors()) {
            model.addAttribute("drivers", userService.getAllDrivers());
            return "rides/create";
        }

        try {
            rideService.createRide(rideDTO);
        } catch (IllegalArgumentException e) {
            result.reject("ride.invalid", e.getMessage());
            model.addAttribute("drivers", userService.getAllDrivers());
            return "rides/create";
        }

        return "redirect:/rides";
    }

    // ---------- EDIT (Customer only) ----------

    @GetMapping("/edit/{id}")
    public String editRideForm(@PathVariable String id,
                               HttpSession session,
                               Model model) {

        if (!isCustomer(session)) {
            return "redirect:/rides";
        }

        Ride ride = rideService.getRideById(id);
        if (ride == null) {
            return "redirect:/rides";
        }

        RideDTO dto = new RideDTO();
        dto.setBikeId(ride.getBikeId());
        dto.setDriverId(ride.getDiverId());
        dto.setStartLocation(ride.getStartLocation());
        dto.setDestination(ride.getDestination());
        dto.setRideDate(ride.getRideDate());
        dto.setRideTime(ride.getRideTime());
        dto.setAvailableSeats(ride.getAvailableSeats());
        dto.setStatus(ride.getStatus());

        model.addAttribute("rideDTO", dto);
        model.addAttribute("rideId", id);

        return "rides/editRide";
    }

    @PostMapping("/edit/{id}")
    public String updateRide(@PathVariable String id,
                             HttpSession session,
                             @ModelAttribute("rideDTO") RideDTO rideDTO,
                             BindingResult result,
                             Model model) {

        if (!isCustomer(session)) {
            return "redirect:/rides";
        }

        if (result.hasErrors()) {
            model.addAttribute("rideId", id);
            return "rides/editRide";
        }

        rideService.updateRide(id, rideDTO);
        return "redirect:/rides";
    }

    // ---------- DELETE (Admin only) ----------

    @PostMapping("/delete/{id}")
    public String deleteRide(@PathVariable String id, HttpSession session) {

        User user = currentUser(session);
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/rides";
        }

        rideService.deleteRide(id);
        return "redirect:/rides";
    }

    // ---------- DRIVERS ----------

    @GetMapping("/drivers")
    public String getAllDrivers(Model model) {

        List<User> drivers = userService.getAllDrivers();
        model.addAttribute("drivers", drivers);

        return "users/drivers";
    }

    // ---------- helpers ----------

    private User currentUser(HttpSession session) {
        Object user = session.getAttribute("loggedInUser");
        return user instanceof User ? (User) user : null;
    }

    private boolean isCustomer(HttpSession session) {
        User user = currentUser(session);
        return user != null && "Customer".equals(user.getRole());
    }
}