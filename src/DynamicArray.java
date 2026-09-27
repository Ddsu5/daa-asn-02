import java.util.Arrays;

public final class DynamicArray {
    private int[] data;
    private int size;
    private long accesses, comparisons, movements;

    public DynamicArray() { this(16); }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Negative capacity");
        data = new int[Math.max(1, initialCapacity)];
    }

    public int size() { return size; }
    public long accesses() { return accesses; }
    public long comparisons() { return comparisons; }
    public long movements() { return movements; }
    public void resetMetrics() { accesses = comparisons = movements = 0; }

    private void ensureCapacity(int needed) {
        if (needed <= data.length) return;
        int capacity = data.length;
        while (capacity < needed) capacity = Math.max(capacity + 1, capacity * 2);
        data = Arrays.copyOf(data, capacity);
        movements += size;
    }

    public void add(int value) {
        ensureCapacity(size + 1);
        data[size++] = value;
    }

    public void add(int index, int value) {
        checkPosition(index);
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movements++;
        }
        data[index] = value;
        size++;
    }

    public int remove(int index) {
        checkElement(index);
        int removed = data[index];
        accesses++;
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movements++;
        }
        data[--size] = 0;
        return removed;
    }

    public int get(int index) {
        checkElement(index);
        accesses++;
        return data[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            accesses++;
            comparisons++;
            if (data[i] == value) return true;
        }
        return false;
    }

    private void checkElement(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
    }

    private void checkPosition(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException(index);
    }
}
