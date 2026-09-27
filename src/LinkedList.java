public final class LinkedList {
    private static final class Node {
        int value;
        Node next;
        Node(int value) { this.value = value; }
    }

    private Node head, tail;
    private int size;
    private long accesses, comparisons, movements;

    public int size() { return size; }
    public long accesses() { return accesses; }
    public long comparisons() { return comparisons; }
    public long movements() { return movements; }
    public void resetMetrics() { accesses = comparisons = movements = 0; }

    public void add(int value) {
        Node node = new Node(value);
        if (tail == null) head = node;
        else tail.next = node;
        tail = node;
        size++;
    }

    public void add(int index, int value) {
        checkPosition(index);
        if (index == size) { add(value); return; }
        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            head = node;
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            previous.next = node;
        }
        size++;
    }

    public int remove(int index) {
        checkElement(index);
        Node removed;
        if (index == 0) {
            removed = head;
            accesses++;
            head = head.next;
            size--;
            if (size == 0) tail = null;
        } else {
            Node previous = nodeAt(index - 1);
            removed = previous.next;
            accesses++;
            previous.next = removed.next;
            size--;
            if (removed == tail) tail = previous;
        }
        return removed.value;
    }

    public int get(int index) {
        checkElement(index);
        return nodeAt(index).value;
    }

    public boolean contains(int value) {
        for (Node node = head; node != null; node = node.next) {
            accesses++;
            comparisons++;
            if (node.value == value) return true;
        }
        return false;
    }

    private Node nodeAt(int index) {
        Node node = head;
        for (int i = 0; i <= index; i++) {
            accesses++;
            if (i != index) node = node.next;
        }
        return node;
    }

    private void checkElement(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(index);
    }

    private void checkPosition(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException(index);
    }
}
