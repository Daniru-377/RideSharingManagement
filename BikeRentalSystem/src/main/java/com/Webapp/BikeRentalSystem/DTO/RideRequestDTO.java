package com.Webapp.BikeRentalSystem.DTO;

import java.time.LocalDate;

public class RideRequestDTO {

    private String requestId;
    private String bookingId;
    private String passengerId;
    private String bikeId;
    private LocalDate requestDate;
    private String status;
;

    public RideRequestDTO() {
    }

    public String getRequestId() {
        return requestId;
    }

    public RideRequestDTO(String requestId, String bookingId, String passengerId, String bikeId, LocalDate requestDate, String status) {
        this.requestId = requestId;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.bikeId = bikeId;
        this.requestDate = requestDate;
        this.status = status;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }


}
