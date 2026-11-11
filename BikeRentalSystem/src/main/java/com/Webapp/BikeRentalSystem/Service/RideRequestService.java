package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.DTO.BookingResponse;
import com.Webapp.BikeRentalSystem.DTO.RideRequestDTO;
import com.Webapp.BikeRentalSystem.DTO.RideRequestResponse;
import com.Webapp.BikeRentalSystem.Filehandling.RideRequestFileHandler;
import com.Webapp.BikeRentalSystem.Model.Bike;
import com.Webapp.BikeRentalSystem.Model.RideRequest;
import com.Webapp.BikeRentalSystem.Service.DSA.RequestQueue;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class RideRequestService {

    private final RideRequestFileHandler rideRequestFileHandler;
    private final RequestQueue queue = new RequestQueue();
    private final BookingService bookingService;


    public RideRequestService(RideRequestFileHandler rideRequestFileHandler, BookingService bookingService) {
        this.rideRequestFileHandler = rideRequestFileHandler;

        this.bookingService = bookingService;
    }


    public RideRequestResponse getAllRideRequests() {

         try{
             RideRequest[] rideRequests = rideRequestFileHandler.findAll();
             RideRequestDTO[] rideRequestDTOS = new RideRequestDTO[rideRequests.length];

             for(int i = 0; i < rideRequests.length; i++){
                 RideRequest rideRequest = rideRequests[i];
                 rideRequestDTOS[i] = new RideRequestDTO(
                         rideRequest.getRequestId(),
                         rideRequest.getBookingId(),
                         rideRequest.getPassengerId(),
                         rideRequest.getBikeId(),
                         rideRequest.getRequestDate(),
                         rideRequest.getStatus());
             }
             return new RideRequestResponse(200, "success", null, rideRequestDTOS);

         } catch (IOException e) {
             return RideRequestResponse.message(500, e.getMessage());
         }
    }

    public RideRequestResponse getRideRequestById(String requestId) {
        try {
            RideRequest request =
                    rideRequestFileHandler.findById(requestId);

            if (request == null) {
                return RideRequestResponse.message(
                        404, "Ride Request not found"
                );
            }

            RideRequestDTO dto = new RideRequestDTO(
                    request.getRequestId(),
                    request.getBookingId(),
                    request.getPassengerId(),
                    request.getBikeId(),
                    request.getRequestDate(),
                    request.getStatus()
            );

            return new RideRequestResponse(
                    200, "success", dto, null
            );

        } catch (IOException e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }

    @PostConstruct
    public void loadQueue() throws IOException {
        for (RideRequest request : rideRequestFileHandler.findAll()) {
            if ("Pending".equals(request.getStatus())) {
                queue.enqueue(request);
            }
        }
    }

    public synchronized RideRequestResponse saveRideRequest(RideRequestDTO requestDTO) {

        try {
            RideRequest requestSave = new RideRequest(
                    null,
                    requestDTO.getBookingId(),
                    requestDTO.getPassengerId(),
                    requestDTO.getBikeId(),
                    LocalDate.now(),
                    "Pending"
            );

            RideRequest savedRequest = rideRequestFileHandler.create(requestSave);

            queue.enqueue(savedRequest);

            return RideRequestResponse.message(200, "Ride Request saved successfully");

        } catch (IOException e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }

    public synchronized RideRequestResponse updateRideRequest(String requestId, boolean approve) {

        try {
            RideRequest request =
                    rideRequestFileHandler.findById(requestId);

            if (request == null) {
                return RideRequestResponse.message(
                        404, "Ride Request not found"
                );
            }

            if (!"Pending".equals(request.getStatus())) {
                return RideRequestResponse.message(
                        400, "Request is already processed"
                );
            }

            String newStatus = approve ? "Approved" : "Rejected";

            RideRequest requestUpdate = new RideRequest(
                    request.getRequestId(),
                    request.getBookingId(),
                    request.getPassengerId(),
                    request.getBikeId(),
                    request.getRequestDate(),
                    newStatus
            );

            boolean updated =
                    rideRequestFileHandler.update(requestUpdate);

            if (!updated) {
                return RideRequestResponse.message(
                        404, "Ride Request not found"
                );
            }

            return RideRequestResponse.message(
                    200, "Ride Request " + newStatus + " successfully"
            );

        } catch (Exception e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }

    public RideRequestResponse deleteRideRequest(String requestId) {
        try {
            if (!rideRequestFileHandler.delete(requestId)) {
                return RideRequestResponse.message(
                        404, "Ride Request not found"
                );
            }

            return RideRequestResponse.message(
                    200, "Ride Request deleted successfully"
            );

        } catch (Exception e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }
    public synchronized RideRequestResponse approveRideRequest(String requestId) {
        try {
            RideRequest request = rideRequestFileHandler.findById(requestId);

            if (request == null) {
                return RideRequestResponse.message(404, "Ride Request not found");
            }

            bookingService.updateBookingStatus(
                    request.getBookingId(),
                    "Confirmed"
            );

            request.setStatus("Approved");
            rideRequestFileHandler.update(request);

            return RideRequestResponse.message(200, "Ride Request Approved");

        } catch (Exception e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }

    public synchronized RideRequestResponse rejectRideRequest(String requestId) {
        try {
            RideRequest request = rideRequestFileHandler.findById(requestId);

            if (request == null) {
                return RideRequestResponse.message(404, "Ride Request not found");
            }

            bookingService.updateBookingStatus(
                    request.getBookingId(),
                    "Cancelled"
            );

            request.setStatus("Rejected");
            rideRequestFileHandler.update(request);

            return RideRequestResponse.message(200, "Ride Request Rejected");

        } catch (Exception e) {
            return RideRequestResponse.message(500, e.getMessage());
        }
    }


}