package com.Webapp.BikeRentalSystem.Filehandling;

import com.Webapp.BikeRentalSystem.Model.RideRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RideRequestFileHandler {

    private final Path file;

    public RideRequestFileHandler(@Value("${rideRequest.file.path}") String path) throws IOException {

        file = Paths.get(path).toAbsolutePath().normalize();

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }

    public synchronized RideRequest[] findAll() throws IOException {

        List<RideRequest> requests = new ArrayList<>();

        try (BufferedReader r = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = r.readLine()) != null) {

                RideRequest request = parse(line);

                if (request != null) {
                    requests.add(request);
                }
            }
        }

        return requests.toArray(new RideRequest[0]);
    }

    public synchronized RideRequest findById(String id)
            throws IOException {

        for (RideRequest request : findAll()) {

            if (request.getRequestId().equals(id)) {
                return request;
            }
        }

        return null;
    }

    public synchronized RideRequest create(RideRequest save) throws IOException {

        int nextId = 1;

        for (RideRequest request : findAll()) {

            String requestId = request.getRequestId();

            try {
                if (requestId != null &&
                        requestId.startsWith("RRID")) {

                    int currentId = Integer.parseInt(
                            requestId.substring(4));

                    if (currentId >= nextId) {
                        nextId = currentId + 1;
                    }
                }

            } catch (NumberFormatException ignored) {
                System.out.println(ignored);
            }
        }

        String newId = String.format("RRID%03d", nextId);

        RideRequest saved = new RideRequest(
                newId,
                clean(save.getBookingId()),
                clean(save.getPassengerId()),
                clean(save.getBikeId()),
                save.getRequestDate(),
                clean(save.getStatus())
        );

        try (BufferedWriter w = Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {

            w.write(format(saved));
            w.newLine();
        }

        return saved;
    }

    public synchronized boolean update(RideRequest update)
            throws IOException {

        RideRequest[] requests = findAll();
        boolean found = false;

        for (RideRequest request : requests) {

            if (request.getRequestId()
                    .equals(update.getRequestId())) {

                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            for (RideRequest request : requests) {

                RideRequest result =
                        request.getRequestId()
                                .equals(update.getRequestId())
                                ? update : request;

                w.write(format(result));
                w.newLine();
            }
        }

        return true;
    }

    public synchronized boolean delete(String id)
            throws IOException {

        RideRequest[] requests = findAll();
        boolean found = false;

        for (RideRequest request : requests) {

            if (request.getRequestId().equals(id)) {
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            for (RideRequest request : requests) {

                if (!request.getRequestId().equals(id)) {

                    w.write(format(request));
                    w.newLine();
                }
            }
        }

        return true;
    }


    private String format(RideRequest request) {

        return clean(request.getRequestId()) + "|"
                + clean(request.getBookingId()) + "|"
                + clean(request.getPassengerId()) + "|"
                + clean(request.getBikeId()) + "|"
                + request.getRequestDate() + "|"
                + clean(request.getStatus());
    }



    private RideRequest parse(String line) {

        String[] request = line.split("\\|", -1);

        if (request.length != 6) {
            return null;
        }

        try {
            return new RideRequest(
                    request[0].trim(),
                    request[1].trim(),
                    request[2].trim(),
                    request[3].trim(),
                    LocalDate.parse(request[4].trim()),
                    request[5].trim()
            );

        } catch (RuntimeException ex) {
            return null;
        }
    }


    private String clean(String s) {

        return s == null ? ""
                : s.replace('|', '/')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }
}
