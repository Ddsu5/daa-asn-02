import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

public final class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int TRIALS = 5;
    private static final int RANDOM_GETS = 10_000;
    private static final int SEARCHES = 1_000;
    private static final int EDITS = 1_000;
    private static volatile long blackhole;
    private final StringBuilder csv = new StringBuilder(
        "workload,structure,n,m,average_ms,metric,average_metric,theory\n");

    private static int[] values(int n) {
        Random random = new Random(42);
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = random.nextInt(n * 2);
        return result;
    }

    private static DynamicArray array(int[] values) {
        DynamicArray result = new DynamicArray(values.length + EDITS + 1);
        for (int value : values) result.add(value);
        result.resetMetrics();
        return result;
    }

    private static LinkedList list(int[] values) {
        LinkedList result = new LinkedList();
        for (int value : values) result.add(value);
        result.resetMetrics();
        return result;
    }

    private void row(String workload, String structure, int n, int m,
                     long nanoseconds, long metric, String metricName, String theory) {
        csv.append(String.format(Locale.ROOT, "%s,%s,%d,%d,%.6f,%s,%.1f,%s%n",
            workload, structure, n, m, nanoseconds / (TRIALS * 1_000_000.0),
            metricName, metric / (double) TRIALS, theory));
    }

    private void randomAccess(int n, int[] values) {
        Random random = new Random(42);
        int[] indices = new int[RANDOM_GETS];
        for (int i = 0; i < indices.length; i++) indices[i] = random.nextInt(n);
        long aTime = 0, lTime = 0, aMetric = 0, lMetric = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            DynamicArray a = array(values);
            long sum = 0, start = System.nanoTime();
            for (int index : indices) sum += a.get(index);
            aTime += System.nanoTime() - start;
            aMetric += a.accesses();
            blackhole = sum;

            LinkedList l = list(values);
            sum = 0;
            start = System.nanoTime();
            for (int index : indices) sum += l.get(index);
            lTime += System.nanoTime() - start;
            lMetric += l.accesses();
            blackhole = sum;
        }
        row("random_access", "DynamicArray", n, RANDOM_GETS, aTime, aMetric, "accesses", "Theta(m)");
        row("random_access", "LinkedList", n, RANDOM_GETS, lTime, lMetric, "accesses", "Theta(m*n)");
    }

    private void search(int n, int[] values) {
        Random random = new Random(42);
        int[] queries = new int[SEARCHES];
        for (int i = 0; i < queries.length; i++)
            queries[i] = (i % 2 == 0) ? values[random.nextInt(n)] : -1 - i;
        for (int i = queries.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = queries[i]; queries[i] = queries[j]; queries[j] = tmp;
        }
        long aTime = 0, lTime = 0, aMetric = 0, lMetric = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            DynamicArray a = array(values);
            int hits = 0;
            long start = System.nanoTime();
            for (int query : queries) if (a.contains(query)) hits++;
            aTime += System.nanoTime() - start;
            aMetric += a.comparisons();
            blackhole = hits;

            LinkedList l = list(values);
            hits = 0;
            start = System.nanoTime();
            for (int query : queries) if (l.contains(query)) hits++;
            lTime += System.nanoTime() - start;
            lMetric += l.comparisons();
            blackhole = hits;
        }
        row("search", "DynamicArray", n, SEARCHES, aTime, aMetric, "comparisons", "Theta(m*n)");
        row("search", "LinkedList", n, SEARCHES, lTime, lMetric, "comparisons", "Theta(m*n)");
    }

    private void edit(int n, int[] values, boolean middle, boolean insertion) {
        int index = middle ? n / 2 : 0;
        String operation = insertion ? "insert" : "remove";
        String position = middle ? "middle" : "front";
        long aTime = 0, lTime = 0, aMetric = 0, lMetric = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            DynamicArray a = array(values);
            if (!insertion) {
                for (int i = 0; i < EDITS; i++) a.add(index, -i - 1);
                a.resetMetrics();
            }
            long sum = 0, start = System.nanoTime();
            for (int i = 0; i < EDITS; i++) {
                if (insertion) a.add(index, -i - 1);
                else sum += a.remove(index);
            }
            aTime += System.nanoTime() - start;
            aMetric += a.movements();
            blackhole = sum;

            LinkedList l = list(values);
            if (!insertion) {
                for (int i = 0; i < EDITS; i++) l.add(index, -i - 1);
                l.resetMetrics();
            }
            sum = 0;
            start = System.nanoTime();
            for (int i = 0; i < EDITS; i++) {
                if (insertion) l.add(index, -i - 1);
                else sum += l.remove(index);
            }
            lTime += System.nanoTime() - start;
            lMetric += l.accesses();
            blackhole = sum;
        }
        String workload = operation + "_" + position;
        row(workload, "DynamicArray", n, EDITS, aTime, aMetric, "movements", "Theta(m*n+m^2)");
        row(workload, "LinkedList", n, EDITS, lTime, lMetric, "node_accesses",
            middle ? "Theta(m*n)" : "Theta(m)");
    }

    private void heap(int n, int[] values) {
        long insertTime = 0, extractTime = 0, insertComparisons = 0, extractComparisons = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            MinHeap h = new MinHeap(n);
            long start = System.nanoTime();
            for (int value : values) h.insert(value);
            insertTime += System.nanoTime() - start;
            insertComparisons += h.comparisons();
            if (!h.validHeap()) throw new AssertionError("Heap invalid after insertions");
            h.resetMetrics();
            int previous = Integer.MIN_VALUE;
            start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                int current = h.extractMin();
                if (current < previous) throw new AssertionError("Extraction not sorted");
                previous = current;
            }
            extractTime += System.nanoTime() - start;
            extractComparisons += h.comparisons();
            if (!h.validHeap()) throw new AssertionError("Heap invalid after extraction");
            blackhole = previous;
        }
        row("priority_insert", "MinHeap", n, n, insertTime, insertComparisons,
            "comparisons", "O(n*log(n))");
        row("priority_extract", "MinHeap", n, n, extractTime, extractComparisons,
            "comparisons", "Theta(n*log(n))");
    }

    private static void warmup() {
        int[] values = values(500);
        for (int k = 0; k < 15; k++) {
            DynamicArray a = array(values);
            LinkedList l = list(values);
            MinHeap h = new MinHeap(values.length);
            for (int i = 0; i < values.length; i++) {
                blackhole = a.get(i) + l.get(i);
                blackhole = a.contains(i) ? 1 : 0;
                blackhole = l.contains(i) ? 1 : 0;
                h.insert(values[i]);
            }
            for (int i = 0; i < values.length; i++) blackhole = h.extractMin();
            for (int i = 0; i < 200; i++) {
                a.add(0, i); l.add(0, i);
                a.remove(0); l.remove(0);
                a.add(100, i); l.add(100, i);
                a.remove(100); l.remove(100);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        warmup();
        Benchmark benchmark = new Benchmark();
        for (int n : SIZES) {
            int[] values = values(n);
            benchmark.randomAccess(n, values);
            benchmark.search(n, values);
            benchmark.edit(n, values, false, true);
            benchmark.edit(n, values, false, false);
            benchmark.edit(n, values, true, true);
            benchmark.edit(n, values, true, false);
            benchmark.heap(n, values);
            System.out.println("Finished n=" + n);
        }
        Path file = Path.of("results/tables/benchmark.csv");
        Files.createDirectories(file.getParent());
        Files.writeString(file, benchmark.csv);
        System.out.println("Saved " + file.toAbsolutePath());
    }
}
