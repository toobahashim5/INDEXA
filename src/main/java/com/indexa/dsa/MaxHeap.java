package com.indexa.dsa;

import java.util.ArrayList;
import java.util.List;

/**
 * A binary Max Heap, implemented manually (array-backed, not
 * java.util.PriorityQueue) so the sift-up / sift-down mechanics are
 * visible and explainable in the DSA report.
 *
 * Used by RankingService (Step 8) to take a list of scored
 * SearchResults and repeatedly pull out the highest-scoring one -
 * that's what produces "most relevant results first" ordering.
 *
 * How a binary heap is stored in a plain array/list:
 *   For a node at index i:
 *     left child index  = 2*i + 1
 *     right child index = 2*i + 2
 *     parent index      = (i - 1) / 2
 *   The "max heap" property: every parent's value >= both its
 *   children's values. This guarantees the single largest element is
 *   always at index 0, in O(1) to peek.
 *
 * Time complexity:
 *   insert       -> O(log n)  (sift up at most tree-height levels)
 *   extractMax   -> O(log n)  (sift down at most tree-height levels)
 *   peek         -> O(1)
 */
public class MaxHeap<T extends Comparable<T>> {

    private final List<T> heap;

    public MaxHeap() {
        this.heap = new ArrayList<>();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Adds a new element and restores the max-heap property by
     * "bubbling" it up toward the root as long as it's bigger than
     * its parent.
     */
    public void insert(T item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    /**
     * Looks at the largest element without removing it.
     */
    public T peek() {
        if (heap.isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty heap");
        }
        return heap.get(0);
    }

    /**
     * Removes and returns the largest element. The last element in
     * the array is moved to the root, then "bubbled" down to restore
     * the max-heap property.
     */
    public T extractMax() {
        if (heap.isEmpty()) {
            throw new IllegalStateException("Cannot extract from an empty heap");
        }
        T max = heap.get(0);
        T last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return max;
    }

    // ---------- Internal helpers ----------

    private int parentIndex(int i) {
        return (i - 1) / 2;
    }

    private int leftChildIndex(int i) {
        return 2 * i + 1;
    }

    private int rightChildIndex(int i) {
        return 2 * i + 2;
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = parentIndex(index);
            // If this node is not bigger than its parent, the heap
            // property already holds - stop.
            if (heap.get(index).compareTo(heap.get(parent)) <= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int left = leftChildIndex(index);
            int right = rightChildIndex(index);
            int largest = index;

            if (left < size && heap.get(left).compareTo(heap.get(largest)) > 0) {
                largest = left;
            }
            if (right < size && heap.get(right).compareTo(heap.get(largest)) > 0) {
                largest = right;
            }
            if (largest == index) {
                break; // both children are smaller - heap property holds
            }
            swap(index, largest);
            index = largest;
        }
    }
}
