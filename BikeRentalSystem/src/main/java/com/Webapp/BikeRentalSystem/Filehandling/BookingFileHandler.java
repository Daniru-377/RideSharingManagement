package com.Webapp.BikeRentalSystem.Filehandling;

import com.Webapp.BikeRentalSystem.Model.Booking;
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
public class BookingFileHandler {

    private final Path file;

    public BookingFileHandler(@Value("${booking.file.path}") String path) throws IOException {

        file = Paths.get(path).toAbsolutePath().normalize();

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }

    public synchronized Booking[] findAll() throws IOException {
        List<Booking> bookings = new ArrayList<>();

        try (BufferedReader r = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = r.readLine()) != null) {
                Booking booking = parse(line);

                if (booking != null) {
                    bookings.add(booking);
                }
            }
        }

        return bookings.toArray(new Booking[0]);
    }

    public synchronized Booking findById(String id) throws IOException {

        for (Booking booking : findAll()) {
            if (booking.getBookingId().equals(id)) {
                return booking;
            }
        }

        return null;
    }


    public synchronized Booking create(Booking save) throws IOException {

        int nextId = 1;

        for (Booking booking : findAll()) {
            String bookingId = booking.getBookingId();

            try {
                if (bookingId != null && bookingId.startsWith("BID")) {
                    int currentId = Integer.parseInt(
                            bookingId.substring(3));

                    if (currentId >= nextId) {
                        nextId = currentId + 1;
                    }
                }
            } catch (NumberFormatException ignored) {
                System.out.println(ignored);
            }
        }

        String newId = String.format("BID%03d", nextId);

        Booking saved = new Booking(
                newId,
                clean(save.getUserId()),
                clean(save.getBikeId()),
                save.getBookingDate(),
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


    public synchronized boolean update(Booking update) throws IOException {

        Booking[] bookings = findAll();
        boolean found = false;

        for (Booking booking : bookings) {
            if (booking.getBookingId()
                    .equals(update.getBookingId())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            for (Booking booking : bookings) {
                Booking result =
                        booking.getBookingId()
                                .equals(update.getBookingId())
                                ? update : booking;

                w.write(format(result));
                w.newLine();
            }
        }

        return true;
    }

    public synchronized boolean delete(String id) throws IOException {

        Booking[] bookings = findAll();
        boolean found = false;

        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(id)) {
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            for (Booking booking : bookings) {
                if (!booking.getBookingId().equals(id)) {
                    w.write(format(booking));
                    w.newLine();
                }
            }
        }

        return true;
    }

    public synchronized Booking[] getBookingsByUserId(String userId) throws IOException {

        List<Booking> userBookings = new ArrayList<>();

        for (Booking booking : findAll()) {

            if (booking.getUserId().equals(userId)) {
                userBookings.add(booking);
            }
        }

        return userBookings.toArray(new Booking[0]);
    }


    private String format(Booking booking) {
        return clean(booking.getBookingId()) + "|"
                + clean(booking.getUserId()) + "|"
                + clean(booking.getBikeId()) + "|"
                + booking.getBookingDate() + "|"
                + clean(booking.getStatus());
    }

    private Booking parse(String line) {
        String[] booking = line.split("\\|", -1);

        if (booking.length != 5) {
            return null;
        }

        try {
            return new Booking(
                    booking[0],
                    booking[1],
                    booking[2],
                    LocalDate.parse(booking[3]),
                    booking[4]
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
