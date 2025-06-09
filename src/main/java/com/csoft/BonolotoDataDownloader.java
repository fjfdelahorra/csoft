package com.csoft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to download the historical Bonoloto results CSV and remove
 * malformed lines.
 */
public class BonolotoDataDownloader {
    private static final String DATA_URL =
        "https://docs.google.com/spreadsheets/d/e/2PACX-1vQALTRaLDFfhXOAQmeONPqmFKm9yOiQ4W97rhWgR41BZ7czFsjK5YktD6fnETKHGB9YUnyQ4XBSbhZx/pub?gid=0&single=true&output=csv";

    /**
     * Downloads the CSV, removes lines that don't have exactly nine columns and
     * writes the cleaned content to the provided path.
     */
    public static void downloadAndClean(Path outputPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATA_URL)).build();
        HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        List<String> validLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length == 9) {
                    validLines.add(line);
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            for (String l : validLines) {
                writer.write(l);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "bonoloto_clean.csv");
        downloadAndClean(out);
        System.out.println("Datos guardados en " + out.toAbsolutePath());
    }
}
