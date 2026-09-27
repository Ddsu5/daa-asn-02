import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public final class Tests {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    private static void throwsType(Class<? extends Throwable> type, Runnable task) {
        checks++;
        try { task.run(); }
        catch (Throwable error) {
            if (type.isInstance(error)) return;
            throw new AssertionError("Wrong exception", error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    private static void verify(ArrayList<Integer> expected, DynamicArray a, LinkedList l) {
        equal(expected.size(), a.size());
        equal(expected.size(), l.size());
        for (int i = 0; i < expected.size(); i++) {
            equal(expected.get(i), a.get(i));
            equal(expected.get(i), l.get(i));
        }
    }

    private static void lists() {
        DynamicArray a = new DynamicArray(0);
        LinkedList l = new LinkedList();
        ArrayList<Integer> oracle = new ArrayList<>();
        verify(oracle, a, l);
        equal(false, a.contains(7)); equal(false, l.contains(7));
        throwsType(IndexOutOfBoundsException.class, () -> a.get(0));
        throwsType(IndexOutOfBoundsException.class, () -> l.get(0));
        throwsType(IndexOutOfBoundsException.class, () -> a.remove(0));
        throwsType(IndexOutOfBoundsException.class, () -> l.remove(0));
        throwsType(IndexOutOfBoundsException.class, () -> a.add(-1, 1));
        throwsType(IndexOutOfBoundsException.class, () -> l.add(-1, 1));
        a.add(7); l.add(7); oracle.add(7);
        verify(oracle, a, l);
        a.add(0, 7); l.add(0, 7); oracle.add(0, 7);
        a.add(a.size(), -2); l.add(l.size(), -2); oracle.add(-2);
        verify(oracle, a, l);
        equal(oracle.remove(1), a.remove(1));
        equal(7, l.remove(1));
        verify(oracle, a, l);

        Random random = new Random(99);
        for (int step = 0; step < 5_000; step++) {
            int choice = random.nextInt(5);
            int value = random.nextInt(100) - 50;
            if (choice == 0) {
                oracle.add(value); a.add(value); l.add(value);
            } else if (choice == 1) {
                int index = random.nextInt(oracle.size() + 1);
                oracle.add(index, value); a.add(index, value); l.add(index, value);
            } else if (choice == 2 && !oracle.isEmpty()) {
                int index = random.nextInt(oracle.size());
                int removed = oracle.remove(index);
                equal(removed, a.remove(index)); equal(removed, l.remove(index));
            } else if (choice == 3) {
                equal(oracle.contains(value), a.contains(value));
                equal(oracle.contains(value), l.contains(value));
            } else if (!oracle.isEmpty()) {
                int index = random.nextInt(oracle.size());
                equal(oracle.get(index), a.get(index));
                equal(oracle.get(index), l.get(index));
            }
            if (step % 200 == 0) verify(oracle, a, l);
        }
        verify(oracle, a, l);
        throwsType(IndexOutOfBoundsException.class, () -> a.get(a.size()));
        throwsType(IndexOutOfBoundsException.class, () -> l.get(l.size()));
        throwsType(IndexOutOfBoundsException.class, () -> a.remove(-1));
        throwsType(IndexOutOfBoundsException.class, () -> l.remove(-1));
        throwsType(IndexOutOfBoundsException.class, () -> a.add(a.size() + 1, 1));
        throwsType(IndexOutOfBoundsException.class, () -> l.add(l.size() + 1, 1));

        DynamicArray largeArray = new DynamicArray();
        LinkedList largeList = new LinkedList();
        for (int i = 0; i < 100_000; i++) { largeArray.add(i); largeList.add(i); }
        equal(99_999, largeArray.get(99_999)); equal(99_999, largeList.get(99_999));
        equal(99_999, largeArray.remove(99_999)); equal(99_999, largeList.remove(99_999));
        largeArray.add(99_999); largeList.add(99_999);
        equal(99_999, largeArray.get(99_999)); equal(99_999, largeList.get(99_999));
    }

    private static void heap() {
        MinHeap heap = new MinHeap(0);
        PriorityQueue<Integer> oracle = new PriorityQueue<>();
        throwsType(NoSuchElementException.class, heap::peekMin);
        throwsType(NoSuchElementException.class, heap::extractMin);
        Random random = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            int value = random.nextInt(500);
            heap.insert(value);
            oracle.add(value);
            if (i < 200 || i % 1000 == 0) {
                equal(oracle.peek(), heap.peekMin());
                equal(true, heap.validHeap());
            }
        }
        int previous = Integer.MIN_VALUE;
        while (!oracle.isEmpty()) {
            int expected = oracle.remove();
            int actual = heap.extractMin();
            equal(expected, actual);
            equal(true, previous <= actual);
            previous = actual;
            if (heap.size() < 200 || heap.size() % 1000 == 0)
                equal(true, heap.validHeap());
        }
        equal(0, heap.size());
        throwsType(NoSuchElementException.class, heap::extractMin);
    }

    public static void main(String[] args) {
        lists(); heap();
        System.out.println("PASS: " + checks + " assertions");
    }
}
