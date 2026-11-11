package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.DTO.BookingDTO;
import com.Webapp.BikeRentalSystem.DTO.BookingResponse;
import com.Webapp.BikeRentalSystem.Filehandling.BookingFileHandler;
import com.Webapp.BikeRentalSystem.Model.Bike;
import com.Webapp.BikeRentalSystem.Model.Booking;
import com.Webapp.BikeRentalSystem.Model.User;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BookingService {

    private final BookingFileHandler bookingFileHandler;
    private final UserService userService;
    private final BikeService bikeService;

    public BookingService(BookingFileHandler bookingFileHandler, UserService userService, BikeService bikeService) {
        this.bookingFileHandler = bookingFileHandler;
        this.userService = userService;
        this.bikeService = bikeService;
    }

    public BookingResponse getAllBookings() {

            try {
                Booking[] bookings = bookingFileHandler.findAll();
                BookingDTO[] bookingDTOS = new BookingDTO[bookings.length];

                for (int i = 0; i < bookings.length; i++) {
                    Booking booking = bookings[i];

                    User user = userService.getUserById(booking.getUserId());
                    String userName = user != null ? user.getName() : "Unknown User";

                    Bike bike = bikeService.getBikeById(booking.getBikeId());
                    String bikeName = bike != null ? bike.getModel() : "Unknown Bike";
                    bookingDTOS[i] = new BookingDTO(
                            booking.getBookingId(),
                            userName,
                            bikeName,
                            booking.getBookingDate(),
                            booking.getStatus());
                }

                return new BookingResponse(200, "success", null, bookingDTOS);
            } catch (IOException e) {
                return BookingResponse.message(500, e.getMessage());
            }
        }

        public BookingResponse getBookingById(String bookingId) {

           try {
               Booking booking = bookingFileHandler.findById(bookingId);
               if(booking == null) {
                   return BookingResponse.message(404, "Booking not found");
               }else {
                   User user = userService.getUserById(booking.getUserId());
                   String userName = user != null ? user.getName() : "Unknown User";

                   Bike bike = bikeService.getBikeById(booking.getBikeId());
                   String bikeName = bike != null ? bike.getModel() : "Unknown Bike";
                   BookingDTO bookingDTO = new BookingDTO(
                           booking.getBookingId(),
                           userName,
                           bikeName,
                           booking.getBookingDate(),
                           booking.getStatus());


                   return new BookingResponse(200, "success", bookingDTO, null);
               }
           } catch (IOException e) {
               return BookingResponse.message(500, e.getMessage());
           }
        }

        public BookingResponse saveBooking(String userId,BookingDTO bookingDTO) {

           try {

               Booking bookingSave = new Booking(null,
                                               userId,
                                               bookingDTO.getBikeId(),
                                               bookingDTO.getBookingDate(),
                                               bookingDTO.getStatus());

               bookingFileHandler.create(bookingSave);

               return new BookingResponse(200, "success", null, null);



           }catch (IOException e) {
               throw new RuntimeException(e);
           }
        }

        public BookingResponse updateBooking (String bookingId,BookingDTO bookingDTO){

            try {
                Booking booking = bookingFileHandler.findById(bookingId);
                if(booking == null) {
                    return BookingResponse.message(404, "Booking Id not found");
                }else {
                    Booking bookingUpdate = new Booking(bookingId,
                                                        bookingDTO.getUserId(),
                                                        bookingDTO.getBikeId(),
                                                        bookingDTO.getBookingDate(),
                                                        bookingDTO.getStatus());


                    bookingFileHandler.update(bookingUpdate);
                }

                return new BookingResponse(200, "success", null, null);

            }catch (Exception e){
                return BookingResponse.message(500, e.getMessage());
            }

        }

        public BookingResponse deleteBooking(String bookingId){

            try {

                if(!bookingFileHandler.delete(bookingId)) {
                    return BookingResponse.message(404, "Booking  not found");
                }else{
                    return BookingResponse.message(200, "Booking deleted successfully");

                }

            }catch (Exception e){
                return BookingResponse.message(500, e.getMessage());
            }

        }

        public BookingResponse getBookingByUserId(String userId) {


            try {
                Booking[] bookings = bookingFileHandler.getBookingsByUserId(userId);
                BookingDTO[] bookingDTOS = new BookingDTO[bookings.length];

                for (int i = 0; i < bookings.length; i++) {
                    Booking booking = bookings[i];
                    User user = userService.getUserById(booking.getUserId());
                    String userName = user != null ? user.getName() : "Unknown User";

                    Bike bike = bikeService.getBikeById(booking.getBikeId());
                    String bikeName = bike != null ? bike.getModel() : "Unknown Bike";
                    bookingDTOS[i] = new BookingDTO(
                            booking.getBookingId(),
                            userName,
                            bikeName,
                            booking.getBookingDate(),
                            booking.getStatus());
                }

                return new BookingResponse(200, "success", null, bookingDTOS);
            } catch (IOException e) {
                return BookingResponse.message(500, e.getMessage());
            }
        }
    public BookingResponse updateBookingStatus(String bookingId, String status) {
        try {
            Booking booking = bookingFileHandler.findById(bookingId);

            if (booking == null) {
                return BookingResponse.message(404, "Booking Id not found");
            }

            BookingDTO bookingDTO = new BookingDTO(
                    booking.getBookingId(),
                    booking.getUserId(),
                    booking.getBikeId(),
                    booking.getBookingDate(),
                    status
            );

            return updateBooking(bookingId, bookingDTO);

        } catch (Exception e) {
            return BookingResponse.message(500, e.getMessage());
        }
    }



}
