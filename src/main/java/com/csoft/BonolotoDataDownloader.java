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
import java.util.logging.Logger;

/**
 * Utility class to download the historical Bonoloto results CSV and remove
 * malformed lines.
 */
public class BonolotoDataDownloader {
    private static final String DATA_URL =
        "https://docs.google.com/spreadsheets/d/e/2PACX-1vQALTRaLDFfhXOAQmeONPqmFKm9yOiQ4W97rhWgR41BZ7czFsjK5YktD6fnETKHGB9YUnyQ4XBSbhZx/pub?gid=0&single=true&output=csv";
    private static final Logger LOGGER = Logger.getLogger(BonolotoDataDownloader.class.getName());

    /**
     * Downloads the CSV, removes lines that don't have exactly nine columns and
     * writes the cleaned content to the provided path.
     */
    public static void downloadAndClean(Path outputPath) throws IOException, InterruptedException {
        LOGGER.info("Descargando datos desde " + DATA_URL);
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATA_URL)).build();
        HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        LOGGER.info("Codigo de respuesta: " + response.statusCode());

        byte[] allBytes = response.body().readAllBytes();
        LOGGER.info("Descargados " + allBytes.length + " bytes");

        List<String> validLines = new ArrayList<>();
        int totalLines = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(allBytes), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                totalLines++;
                String[] parts = line.split(",", -1);
                if (parts.length == 9) {
                    validLines.add(line);
                }
            }
        }
        LOGGER.info("Lineas totales: " + totalLines + ", lineas validas: " + validLines.size());

        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            for (String l : validLines) {
                writer.write(l);
                writer.newLine();
            }
        }
        LOGGER.info("Datos guardados en " + outputPath.toAbsolutePath());
    }

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "bonoloto_clean.csv");
        downloadAndClean(out);
        System.out.println("Datos guardados en " + out.toAbsolutePath());
    }
}
