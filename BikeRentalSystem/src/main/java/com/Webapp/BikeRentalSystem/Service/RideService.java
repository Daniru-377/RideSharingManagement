package com.Webapp.BikeRentalSystem.Service;


import com.Webapp.BikeRentalSystem.DTO.BookingDTO;
import com.Webapp.BikeRentalSystem.DTO.RideDTO;
import com.Webapp.BikeRentalSystem.Filehandling.RideFileHandler;
import com.Webapp.BikeRentalSystem.Model.Ride;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RideService {

    private final RideFileHandler fileHandler;
    private final BookingService bookingService;

    public RideService(RideFileHandler fileHandler, BookingService bookingService) {
        this.fileHandler = fileHandler;
        this.bookingService = bookingService;
    }

    public List<Ride> readRides() {
        return fileHandler.loadRides();
    }

    public Ride getRideById(String rideId) {
        return fileHandler.loadRides().stream()
                .filter(r -> r.getRideId().equals(rideId))
                .findFirst()
                .orElse(null);
    }

    public synchronized Ride createRide(RideDTO dto) {
        if (dto.getStartLocation().equalsIgnoreCase(dto.getDestination())) {
            throw new IllegalArgumentException("Start location and destination must be different.");
        }

        List<Ride> rides = fileHandler.loadRides();
        String status = dto.getAvailableSeats() == 0 ? "Full" : "Open";

        Ride ride = new Ride(
                fileHandler.nextId(rides),
                dto.getBikeId(),
                dto.getDriverId(),
                dto.getStartLocation(),
                dto.getDestination(),
                dto.getRideDate(),
                dto.getRideTime(),
                dto.getAvailableSeats(),
                status
        );

        rides.add(ride);
        fileHandler.saveRides(rides);
        return ride;
    }

    public synchronized Ride updateRide(String rideId, RideDTO dto) {
        List<Ride> rides = fileHandler.loadRides();
        Optional<Ride> optionalRide = rides.stream()
                .filter(r -> r.getRideId().equals(rideId))
                .findFirst();

        if (optionalRide.isPresent()) {
            Ride ride = optionalRide.get();
            ride.setRideTime(dto.getRideTime());
            ride.setAvailableSeats(dto.getAvailableSeats());

            String newStatus = dto.getAvailableSeats() == 0 ? "Full" : dto.getStatus();
            ride.setStatus(newStatus);

            fileHandler.saveRides(rides);
            return ride;
        }
        return null;
    }

    public synchronized void deleteRide(String rideId) {
        List<Ride> rides = fileHandler.loadRides();
        boolean removed = rides.removeIf(r -> r.getRideId().equals(rideId));
        if (removed) {
            fileHandler.saveRides(rides);
        }

    }

    // DRIVER: rides assigned to this driver
    public List<Ride> getRidesForDriver(String userId) {
        List<Ride> result = new ArrayList<>();
        for (Ride ride : fileHandler.loadRides()) {
            if (userId.equals(ride.getDiverId())) {
                result.add(ride);
            }
        }
        return result;
    }

    // CUSTOMER: rides on the bikes this customer has booked
    public List<Ride> getRidesForCustomer(String userId) {
        List<Ride> result = new ArrayList<>();
        BookingDTO[] bookings = bookingService.getAllBookings().getBookings();

        for (Ride ride : fileHandler.loadRides()) {
            if (bookings == null) break;
            for (BookingDTO b : bookings) {
                if (userId.equals(b.getUserId()) && ride.getBikeId().equals(b.getBikeId())) {
                    result.add(ride);
                    break;
                }
            }
        }
        return result;
    }

    // rideId -> bookingId (customer's own booking for the ride's bike)
    public Map<String, String> getBookingIdsForRides(String userId, List<Ride> rides) {

        Map<String, String> map = new HashMap<>();
        BookingDTO[] bookings = bookingService.getAllBookings().getBookings();

        if (bookings == null) {
            return map;
        }

        for (Ride ride : rides) {
            for (BookingDTO b : bookings) {
                if (userId.equals(b.getUserId())
                        && ride.getBikeId().equals(b.getBikeId())
                        && !"Cancelled".equalsIgnoreCase(b.getStatus())) {
                    map.put(ride.getRideId(), b.getBookingId());
                    break;
                }
            }
        }
        return map;
    }

}