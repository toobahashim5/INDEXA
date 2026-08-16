package com.indexa.dsa;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTest {

    @Test
    void findsElementInTheMiddle() {
        List<Integer> sorted = List.of(1, 3, 5, 7, 9, 11);
        assertEquals(2, BinarySearch.search(sorted, 5));
    }

    @Test
    void findsFirstElement() {
        List<Integer> sorted = List.of(1, 3, 5, 7);
        assertEquals(0, BinarySearch.search(sorted, 1));
    }

    @Test
    void findsLastElement() {
        List<Integer> sorted = List.of(1, 3, 5, 7);
        assertEquals(3, BinarySearch.search(sorted, 7));
    }

    @Test
    void returnsMinusOneForMissingValue() {
        List<Integer> sorted = List.of(1, 3, 5, 7);
        assertEquals(-1, BinarySearch.search(sorted, 4));
    }

    @Test
    void emptyListReturnsMinusOne() {
        assertEquals(-1, BinarySearch.search(List.of(), 5));
    }

    @Test
    void genericVersionWorksWithStrings() {
        List<String> sortedTitles = List.of("arrays", "graphs", "trees");
        assertEquals(1, BinarySearch.search(sortedTitles, "graphs"));
        assertEquals(-1, BinarySearch.search(sortedTitles, "stacks"));
    }
}
