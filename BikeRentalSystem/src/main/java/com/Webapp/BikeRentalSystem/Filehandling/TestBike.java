package com.Webapp.BikeRentalSystem.Filehandling;


import com.Webapp.BikeRentalSystem.Model.Bike;

import java.util.List;

public class TestBike {

    public static void main(String[] args) throws Exception {
        BikeFileHandler handler = new BikeFileHandler("src/main/resources/data/bikes.txt");

        List<Bike> bikes = handler.loadBikes();

        for (int i = 0; i < bikes.size(); i++) {
            Bike b = bikes.get(i);
            System.out.println(b.getBikeId() + " - " + b.getModel() + " - " + b.getLocation());
        }
    }
}