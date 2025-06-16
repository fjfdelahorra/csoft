package com.csoft;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Simple evolutionary algorithm to generate Bonoloto combinations.
 */
public class BonolotoEvolver {
    private static final Logger LOGGER = Logger.getLogger(BonolotoEvolver.class.getName());
    private static final int POPULATION_SIZE = 100;
    private static final int ELITE_SIZE = 20;
    public static final int DEFAULT_GENERATIONS = 50;

    public static class EvolutionResult {
        public final List<String> steps;
        public final int[] best;
        public final double score;

        EvolutionResult(List<String> steps, int[] best, double score) {
            this.steps = steps;
            this.best = best;
            this.score = score;
        }
    }

    private static final class Stats {
        double avgEven;
        double[] avgTens = new double[5];
        double avgHigh;
        double avgSum;
        double avgPrime;
        double[] avgEndDigits = new double[10];
        double avgConsecutive;
    }

    private static final class Features {
        int even;
        int[] tens = new int[5];
        int high;
        int sum;
        int prime;
        int[] endDigits = new int[10];
        int consecutive;
    }

    private static Stats loadStats(Path csv) throws IOException {
        List<BonolotoStats.DrawStat> list = BonolotoStats.compute(csv);
        Stats s = new Stats();
        int total = list.size();
        for (BonolotoStats.DrawStat d : list) {
            s.avgEven += d.even;
            for (int i = 0; i < 5; i++) {
                s.avgTens[i] += d.tens[i];
            }
            int high = 0;
            int sum = 0;
            int prime = 0;
            for (int n : d.numbers) {
                if (n > 24) high++;
                sum += n;
                if (isPrime(n)) prime++;
                s.avgEndDigits[n % 10] += 1;
            }
            s.avgHigh += high;
            s.avgSum += sum;
            s.avgPrime += prime;
            s.avgConsecutive += d.consecutive;
        }
        if (total > 0) {
            s.avgEven /= total;
            s.avgHigh /= total;
            s.avgSum /= total;
            s.avgPrime /= total;
            s.avgConsecutive /= total;
            for (int i = 0; i < 5; i++) {
                s.avgTens[i] /= total;
            }
            for (int i = 0; i < 10; i++) {
                s.avgEndDigits[i] /= total;
            }
        }
        return s;
    }

    private static Features computeFeatures(int[] combo) {
        Features f = new Features();
        Arrays.sort(combo);
        int run = 1;
        int maxRun = 1;
        for (int i = 0; i < combo.length; i++) {
            int n = combo[i];
            if (n % 2 == 0) f.even++; 
            int tensIdx = n <= 9 ? 0 : n <= 19 ? 1 : n <= 29 ? 2 : n <= 39 ? 3 : 4;
            f.tens[tensIdx]++;
            if (n > 24) f.high++;
            f.sum += n;
            if (isPrime(n)) f.prime++;
            f.endDigits[n % 10]++;
            if (i > 0) {
                if (combo[i] == combo[i - 1] + 1) {
                    run++;
                    if (run > maxRun) maxRun = run;
                } else {
                    run = 1;
                }
            }
        }
        f.consecutive = maxRun;
        return f;
    }

    private static double fitness(int[] combo, Stats stats) {
        Features f = computeFeatures(combo);
        double error = 0.0;
        error += sq(f.even - stats.avgEven);
        for (int i = 0; i < 5; i++) {
            error += sq(f.tens[i] - stats.avgTens[i]);
        }
        error += sq(f.high - stats.avgHigh);
        error += sq(f.sum - stats.avgSum) / 100.0; // scale sum difference
        error += sq(f.prime - stats.avgPrime);
        for (int i = 0; i < 10; i++) {
            error += sq(f.endDigits[i] - stats.avgEndDigits[i]);
        }
        error += sq(f.consecutive - stats.avgConsecutive);
        return -error; // higher is better
    }

    private static double sq(double v) {
        return v * v;
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private static int[] randomCombination(Random rnd) {
        Set<Integer> set = new HashSet<>();
        while (set.size() < 6) {
            set.add(rnd.nextInt(49) + 1);
        }
        int[] arr = set.stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(arr);
        return arr;
    }

    private static List<int[]> generatePopulation(Random rnd) {
        List<int[]> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        while (list.size() < POPULATION_SIZE) {
            int[] combo = randomCombination(rnd);
            String key = Arrays.toString(combo);
            if (seen.add(key)) {
                list.add(combo);
            }
        }
        return list;
    }

    private static int[] crossover(int[] a, int[] b, Random rnd) {
        Set<Integer> union = new HashSet<>();
        for (int n : a) union.add(n);
        for (int n : b) union.add(n);
        List<Integer> all = new ArrayList<>(union);
        int[] child = new int[6];
        for (int i = 0; i < 6; i++) {
            child[i] = all.remove(rnd.nextInt(all.size()));
        }
        Arrays.sort(child);
        return child;
    }

    private static void mutate(int[] combo, Random rnd) {
        if (rnd.nextDouble() < 0.1) {
            int idx = rnd.nextInt(6);
            int newVal;
            Set<Integer> existing = new HashSet<>();
            for (int n : combo) existing.add(n);
            do {
                newVal = rnd.nextInt(49) + 1;
            } while (existing.contains(newVal));
            combo[idx] = newVal;
            Arrays.sort(combo);
        }
    }

    /**
     * Runs the evolutionary algorithm using the provided CSV file with the
     * historical draws. The returned result includes the log of each generation
     * along with the best combination found and its score.
     */
    public static EvolutionResult evolve(Path csv) throws IOException {
        return evolve(csv, DEFAULT_GENERATIONS);
    }

    public static EvolutionResult evolve(Path csv, int generations) throws IOException {
        Stats tmpStats;
        try {
            tmpStats = loadStats(csv);
        } catch (IOException e) {
            tmpStats = new Stats();
        }
        final Stats stats = tmpStats;
        Random rnd = new Random();
        List<int[]> population = generatePopulation(rnd);
        int[] best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> steps = new ArrayList<>();

        for (int gen = 1; gen <= generations; gen++) {
            population.sort((x, y) -> Double.compare(fitness(y, stats), fitness(x, stats)));
            double score = fitness(population.get(0), stats);
            if (score > bestScore) {
                bestScore = score;
                best = Arrays.copyOf(population.get(0), 6);
            }
            steps.add("Generacion " + gen + " mejor puntuacion " + score + " combinacion "
                    + Arrays.toString(population.get(0)));

            List<int[]> next = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < ELITE_SIZE; i++) {
                int[] elite = Arrays.copyOf(population.get(i), 6);
                next.add(elite);
                seen.add(Arrays.toString(elite));
            }
            while (next.size() < POPULATION_SIZE) {
                int[] p1 = population.get(rnd.nextInt(ELITE_SIZE));
                int[] p2 = population.get(rnd.nextInt(ELITE_SIZE));
                int[] child = crossover(p1, p2, rnd);
                mutate(child, rnd);
                String key = Arrays.toString(child);
                if (seen.add(key)) {
                    next.add(child);
                }
            }
            population = next;
        }

        steps.add("Mejor combinacion obtenida: " + Arrays.toString(best) + " con puntuacion " + bestScore);
        return new EvolutionResult(steps, best, bestScore);
    }

    public static void main(String[] args) throws Exception {
        Path csv = Path.of(args.length > 0 ? args[0] : "data/history.csv");
        int generations = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_GENERATIONS;
        Stats tmpStats;
        try {
            tmpStats = loadStats(csv);
            LOGGER.info("Cargadas estadisticas de " + csv);
        } catch (IOException e) {
            LOGGER.warning("No se pudo leer el historico, usando estadisticas nulas");
            tmpStats = new Stats();
        }
        final Stats stats = tmpStats;
        Random rnd = new Random();
        List<int[]> population = generatePopulation(rnd);
        int[] best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int gen = 1; gen <= generations; gen++) {
            population.sort((x, y) -> Double.compare(fitness(y, stats), fitness(x, stats)));
            double score = fitness(population.get(0), stats);
            if (score > bestScore) {
                bestScore = score;
                best = Arrays.copyOf(population.get(0), 6);
            }
            LOGGER.info("Generacion " + gen + " mejor puntuacion " + score + " combinacion " + Arrays.toString(population.get(0)));

            List<int[]> next = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < ELITE_SIZE; i++) {
                int[] elite = Arrays.copyOf(population.get(i), 6);
                next.add(elite);
                seen.add(Arrays.toString(elite));
            }
            while (next.size() < POPULATION_SIZE) {
                int[] p1 = population.get(rnd.nextInt(ELITE_SIZE));
                int[] p2 = population.get(rnd.nextInt(ELITE_SIZE));
                int[] child = crossover(p1, p2, rnd);
                mutate(child, rnd);
                String key = Arrays.toString(child);
                if (seen.add(key)) {
                    next.add(child);
                }
            }
            population = next;
        }

        LOGGER.info("Mejor combinacion obtenida: " + Arrays.toString(best) + " con puntuacion " + bestScore);
    }
}
