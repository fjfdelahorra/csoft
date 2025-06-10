package com.csoft;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BonolotoStats {
    public static class DrawStat {
        public final String date;
        public final int even;
        public final int odd;
        public final int[] tens; // counts of numbers in each tens range
        public final int consecutive; // longest streak of consecutive numbers

        public DrawStat(String date, int even, int odd, int[] tens, int consecutive) {
            this.date = date;
            this.even = even;
            this.odd = odd;
            this.tens = tens;
            this.consecutive = consecutive;
        }
    }

    public static List<DrawStat> compute(Path csv) throws IOException {
        List<DrawStat> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length != 9 || parts[0].equalsIgnoreCase("FECHA")) {
                    continue;
                }
                int even = 0;
                int odd = 0;
                int[] tens = new int[5];
                int[] numbers = new int[6];
                for (int i = 1; i <= 6; i++) {
                    int n = Integer.parseInt(parts[i]);
                    numbers[i - 1] = n;
                    if (n % 2 == 0) {
                        even++;
                    } else {
                        odd++;
                    }
                    if (n <= 9) {
                        tens[0]++;
                    } else if (n <= 19) {
                        tens[1]++;
                    } else if (n <= 29) {
                        tens[2]++;
                    } else if (n <= 39) {
                        tens[3]++;
                    } else {
                        tens[4]++;
                    }
                }

                int maxRun = 1;
                int currentRun = 1;
                for (int i = 1; i < numbers.length; i++) {
                    if (numbers[i] == numbers[i - 1] + 1) {
                        currentRun++;
                        if (currentRun > maxRun) {
                            maxRun = currentRun;
                        }
                    } else {
                        currentRun = 1;
                    }
                }

                list.add(new DrawStat(parts[0], even, odd, tens, maxRun));
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] : "data/history.csv");
        List<DrawStat> stats = compute(path);
        System.out.println("FECHA,EVEN,ODD,D1,D2,D3,D4,D5,CONSEC");
        for (DrawStat s : stats) {
            System.out.println(s.date + "," + s.even + "," + s.odd + "," +
                    s.tens[0] + "," + s.tens[1] + "," + s.tens[2] + "," +
                    s.tens[3] + "," + s.tens[4] + "," + s.consecutive);
        }
    }
}
