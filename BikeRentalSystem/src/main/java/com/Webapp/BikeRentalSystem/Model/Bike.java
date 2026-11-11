package com.Webapp.BikeRentalSystem.Model;

public class Bike {
        private String bikeId;
        private String model;
        private String type;
        private String location;
        private double pricePerDay;
        private boolean available;
        private String status;

    public Bike(String bikeId, String model, String type, String location, double pricePerDay, boolean available, String status) {
        this.bikeId = bikeId;
        this.model = model;
        this.type = type;
        this.location = location;
        this.pricePerDay = pricePerDay;
        this.available = available;
        this.status = status;
    }

    public Bike() {

    }

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String toFileString() {
        return bikeId + "|" + model + "|" + type + "|" + location + "|"
                + pricePerDay + "|" + available + "|" + status;
    }


    public static Bike fromFileString(String line) {
        String[] p = line.split("\\|");
        return new Bike(p[0], p[1], p[2], p[3],
                Double.parseDouble(p[4]), Boolean.parseBoolean(p[5]), p[6]);
    }
}



