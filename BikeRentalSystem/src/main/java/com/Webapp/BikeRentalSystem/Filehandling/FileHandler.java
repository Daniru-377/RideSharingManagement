package com.Webapp.BikeRentalSystem.Filehandling;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileHandler {

    public synchronized List<String> readLines(String path) {
        try {
            Path p = Paths.get(path);
            if (!Files.exists(p)) return new ArrayList<>();
            return Files.readAllLines(p).stream()
                    .filter(l -> !l.isBlank())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void writeLines(String path, List<String> lines) {
        try {
            Path p = Paths.get(path);
            Files.createDirectories(p.getParent());
            Files.write(p, lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String nextId(String prefix, List<String> ids) {
        int max = ids.stream()
                .mapToInt(id -> Integer.parseInt(id.substring(prefix.length())))
                .max().orElse(0);
        return String.format("%s%03d", prefix, max + 1);
    }
}
