package com.Webapp.BikeRentalSystem.Service;

import com.Webapp.BikeRentalSystem.Filehandling.BikeFileHandler;
import com.Webapp.BikeRentalSystem.Model.Bike;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BikeService {
    private final BikeFileHandler handler;

    public BikeService(@Value("${bike.file.path}") String path) {
        this.handler = new BikeFileHandler(path);
    }

    public List<Bike> getAllBikes() throws IOException {
        return handler.loadBikes();
    }

    public Bike getBikeById(String bikeId) throws IOException {
        List<Bike> bikes = handler.loadBikes();

        for (int i = 0; i < bikes.size(); i++) {
            if(bikes.get(i).getBikeId().equals(bikeId)){
                return bikes.get(i);
            }
        }
        return null;
    }
    public String getNextBikeId() throws IOException{
        List<Bike> bikes=handler.loadBikes();
        int highest =0;

        for(int i=0;i<bikes.size();i++){
            String id=bikes.get(i).getBikeId();
            int number=Integer.parseInt(id.substring(1));
            if(number>highest){
                highest=number;
            }
        }
        int next=highest+1;
        return String.format("B%03d",next);
    }
    public String addBike(Bike bike) throws IOException {
        String error = validate(bike);
        if (!error.equals("")) {
            return error;
        }


        if (getBikeById(bike.getBikeId()) != null) {
            return "A bike with this ID already exists.";
        }

        List<Bike> bikes = handler.loadBikes();
        bikes.add(bike);
        handler.saveBikes(bikes);
        return "OK";
    }


    public String updateBike(Bike updatedBike) throws IOException {
        String error = validate(updatedBike);
        if (!error.equals("")) {
            return error;
        }

        List<Bike> bikes = handler.loadBikes();

        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i).getBikeId().equals(updatedBike.getBikeId())) {
                bikes.set(i, updatedBike);
                handler.saveBikes(bikes);
                return "OK";
            }
        }
        return "Bike not found.";
    }


    public String deleteBike(String bikeId) throws IOException {
        List<Bike> bikes = handler.loadBikes();

        for (int i = 0; i < bikes.size(); i++) {
            if (bikes.get(i).getBikeId().equals(bikeId)) {

                bikes.remove(i);
                handler.saveBikes(bikes);
                return "OK";
            }
        }
        return "Bike not found.";
    }


    private String validate(Bike bike) {
        if (bike.getBikeId() == null || bike.getBikeId().isBlank()) {
            return "Bike ID cannot be empty.";
        }
        if (bike.getModel() == null || bike.getModel().isBlank()) {
            return "Model cannot be empty.";
        }
        if (!bike.getType().equals("Motorbike")
                && !bike.getType().equals("Scooter")
                && !bike.getType().equals("Bicycle")) {
            return "Type must be Motorbike, Scooter or Bicycle.";
        }
        if (bike.getLocation() == null || bike.getLocation().isBlank()) {
            return "Location cannot be empty.";
        }
        if (bike.getPricePerDay() <= 0) {
            return "Price per day must be greater than 0.";
        }
        if (!bike.getStatus().equals("Active")
                && !bike.getStatus().equals("Maintenance")) {
            return "Status must be Active or Maintenance.";
        }
        if (bike.getStatus().equals("Maintenance") && bike.isAvailable()) {
            return "A bike in Maintenance cannot be available.";
        }
        return "";
    }
}



