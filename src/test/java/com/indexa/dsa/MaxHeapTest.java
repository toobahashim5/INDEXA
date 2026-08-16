package com.indexa.dsa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaxHeapTest {

    @Test
    void extractMaxAlwaysReturnsTheLargestRemainingElement() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        heap.insert(5);
        heap.insert(1);
        heap.insert(9);
        heap.insert(3);
        heap.insert(7);

        // Extracting repeatedly should yield strictly descending order,
        // proving the sift-up/sift-down logic maintains the heap
        // property correctly.
        assertEquals(9, heap.extractMax());
        assertEquals(7, heap.extractMax());
        assertEquals(5, heap.extractMax());
        assertEquals(3, heap.extractMax());
        assertEquals(1, heap.extractMax());
    }

    @Test
    void peekDoesNotRemoveTheElement() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        heap.insert(10);
        heap.insert(20);

        assertEquals(20, heap.peek());
        assertEquals(2, heap.size()); // peek must not remove anything
    }

    @Test
    void sizeAndIsEmptyTrackCorrectly() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertTrue(heap.isEmpty());

        heap.insert(1);
        assertFalse(heap.isEmpty());
        assertEquals(1, heap.size());
    }

    @Test
    void extractingFromEmptyHeapThrows() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertThrows(IllegalStateException.class, heap::extractMax);
    }

    @Test
    void peekingEmptyHeapThrows() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertThrows(IllegalStateException.class, heap::peek);
    }

    @Test
    void handlesDuplicateValuesCorrectly() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        heap.insert(5);
        heap.insert(5);
        heap.insert(5);

        assertEquals(3, heap.size());
        assertEquals(5, heap.extractMax());
        assertEquals(5, heap.extractMax());
        assertEquals(5, heap.extractMax());
    }
}
