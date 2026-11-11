package com.Webapp.BikeRentalSystem.Model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Ride {
    private String rideId, diverId,bikeId, startLocation, destination, status;
    private LocalDate rideDate;
    private LocalTime rideTime;
    private int availableSeats;

    public Ride() {}

    public Ride(String rideId, String diverId, String bikeId, String startLocation,
                String destination, LocalDate rideDate, LocalTime rideTime,
                int availableSeats, String status) {
        this.rideId = rideId;
        this.diverId = diverId;
        this.bikeId = bikeId;
        this.startLocation = startLocation;
        this.destination = destination;
        this.rideDate = rideDate;
        this.rideTime = rideTime;
        this.availableSeats = availableSeats;
        this.status = status;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }


    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRideDate() {
        return rideDate;
    }

    public void setRideDate(LocalDate rideDate) {
        this.rideDate = rideDate;
    }

    public LocalTime getRideTime() {
        return rideTime;
    }

    public void setRideTime(LocalTime rideTime) {
        this.rideTime = rideTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDiverId() {
        return diverId;
    }

    public void setDiverId(String diverId) {
        this.diverId = diverId;
    }

    public String toFileString() {
        return rideId + "|" +  diverId + "|" + bikeId + "|" + startLocation + "|" +
                destination + "|" + rideDate + "|" + rideTime + "|" + availableSeats + "|" + status;
    }

    public static Ride fromFileString(String line) {
        // Escaping the pipe character is required for split()
        String[] p = line.split("\\|");
        return new Ride(
                p[0] ,p[1],p[2], p[3], p[4],
                LocalDate.parse(p[5]), LocalTime.parse(p[6]),
                Integer.parseInt(p[7]), p[8]
        );
    }
}