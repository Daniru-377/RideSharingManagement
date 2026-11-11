
package com.Webapp.BikeRentalSystem.Controller;

import com.Webapp.BikeRentalSystem.DTO.RideRequestDTO;
import com.Webapp.BikeRentalSystem.DTO.RideRequestResponse;
import com.Webapp.BikeRentalSystem.Model.Bike;
import com.Webapp.BikeRentalSystem.Model.User;
import com.Webapp.BikeRentalSystem.Service.BikeService;
import com.Webapp.BikeRentalSystem.Service.RideRequestService;
import com.Webapp.BikeRentalSystem.Service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/rideRequests")
public class RideRequestController {

    private final RideRequestService rideRequestService;
    private final UserService userService;
    private final BikeService bikeService;

    public RideRequestController(
            RideRequestService rideRequestService,
            UserService userService,
            BikeService bikeService) {

        this.rideRequestService = rideRequestService;
        this.userService = userService;
        this.bikeService = bikeService;
    }

    @GetMapping
    public String getAllRideRequests(Model model) {

        RideRequestResponse response =
                rideRequestService.getAllRideRequests();

        model.addAttribute("rideRequests",
                response.getRideRequests());

        return "rideRequests/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam String bookingId, @RequestParam String passengerId, @RequestParam String bikeId, Model model) throws IOException {

        RideRequestDTO dto = new RideRequestDTO();

        dto.setBookingId(bookingId);
        dto.setPassengerId(passengerId);
        dto.setBikeId(bikeId);

        model.addAttribute("rideRequest", dto);

        return "rideRequests/create";
    }

    @PostMapping("/save")
    public String saveRideRequest(@ModelAttribute("rideRequest") RideRequestDTO rideRequestDTO) {

        rideRequestService.saveRideRequest(rideRequestDTO);

        return "redirect:/rideRequests";
    }

    @PostMapping("/approve/{requestId}")
    public String approveRideRequest(@PathVariable String requestId) {
        rideRequestService.approveRideRequest(requestId);
        return "redirect:/rideRequests";
    }

    @PostMapping("/reject/{requestId}")
    public String rejectRideRequest(@PathVariable String requestId) {
        rideRequestService.rejectRideRequest(requestId);
        return "redirect:/rideRequests";
    }

    @PostMapping("/delete/{requestId}")
    public String deleteRideRequest(
            @PathVariable String requestId) {

        rideRequestService.deleteRideRequest(requestId);

        return "redirect:/rideRequests";
    }
}
