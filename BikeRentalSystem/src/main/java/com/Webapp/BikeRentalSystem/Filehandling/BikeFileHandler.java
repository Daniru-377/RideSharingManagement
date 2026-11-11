package com.Webapp.BikeRentalSystem.Filehandling;



import com.Webapp.BikeRentalSystem.Model.Bike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

    public class BikeFileHandler {

        private String path;

        // The constructor receives the file location
        public BikeFileHandler(String path) {
            this.path = path;
        }

        // READ: file -> list of bikes
        public List<Bike> loadBikes() throws IOException {
            List<Bike> bikes = new ArrayList<>();
            File file = new File(path);

            // If the file does not exist yet, return an empty list
            if (!file.exists()) {
                return bikes;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null) {
                if (!line.isBlank()) {
                    Bike bike = Bike.fromFileString(line);
                    bikes.add(bike);
                }
                line = reader.readLine();
            }

            reader.close();
            return bikes;
        }

        // WRITE: list of bikes -> file
        public void saveBikes(List<Bike> bikes) throws IOException {
            PrintWriter writer = new PrintWriter(new FileWriter(path));

            for (int i = 0; i < bikes.size(); i++) {
                writer.println(bikes.get(i).toFileString());
            }

            writer.close();
        }
    }


