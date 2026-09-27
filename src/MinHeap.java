import java.util.Arrays;
import java.util.NoSuchElementException;

public final class MinHeap {
    private int[] heap;
    private int size;
    private long comparisons;

    public MinHeap() { this(16); }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Negative capacity");
        heap = new int[Math.max(1, initialCapacity)];
    }

    public int size() { return size; }
    public long comparisons() { return comparisons; }
    public void resetMetrics() { comparisons = 0; }

    public void insert(int value) {
        if (size == heap.length) heap = Arrays.copyOf(heap, heap.length * 2);
        int child = size++;
        while (child > 0) {
            int parent = (child - 1) / 2;
            comparisons++;
            if (heap[parent] <= value) break;
            heap[child] = heap[parent];
            child = parent;
        }
        heap[child] = value;
    }

    public int peekMin() {
        if (size == 0) throw new NoSuchElementException("Empty heap");
        return heap[0];
    }

    public int extractMin() {
        if (size == 0) throw new NoSuchElementException("Empty heap");
        int answer = heap[0];
        int last = heap[--size];
        if (size == 0) return answer;
        int parent = 0;
        while (parent * 2 + 1 < size) {
            int child = parent * 2 + 1;
            if (child + 1 < size) {
                comparisons++;
                if (heap[child + 1] < heap[child]) child++;
            }
            comparisons++;
            if (last <= heap[child]) break;
            heap[parent] = heap[child];
            parent = child;
        }
        heap[parent] = last;
        return answer;
    }

    public boolean validHeap() {
        for (int child = 1; child < size; child++)
            if (heap[(child - 1) / 2] > heap[child]) return false;
        return true;
    }
}
