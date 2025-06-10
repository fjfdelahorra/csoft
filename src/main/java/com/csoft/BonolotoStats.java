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
        public DrawStat(String date, int even, int odd) {
            this.date = date;
            this.even = even;
            this.odd = odd;
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
                for (int i = 1; i <= 6; i++) {
                    int n = Integer.parseInt(parts[i]);
                    if (n % 2 == 0) {
                        even++;
                    } else {
                        odd++;
                    }
                }
                list.add(new DrawStat(parts[0], even, odd));
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] : "data/history.csv");
        List<DrawStat> stats = compute(path);
        System.out.println("FECHA,EVEN,ODD");
        for (DrawStat s : stats) {
            System.out.println(s.date + "," + s.even + "," + s.odd);
        }
    }
}
