package com.indexa.dsa;

import com.indexa.model.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers BubbleSort, SelectionSort, and InsertionSort together, since
 * they all operate on the same Document model with a simple shared
 * pattern: build an out-of-order list, sort it, verify the result.
 */
class SortingTest {

    private List<Document> buildUnsortedDocuments() {
        List<Document> docs = new ArrayList<>();
        docs.add(new Document(3, "Zebra Patterns", "p", "TXT", "c", 5, "2026-01-03 10:00:00"));
        docs.add(new Document(1, "Apple Basics", "p", "TXT", "c", 5, "2026-01-01 10:00:00"));
        docs.add(new Document(2, "Mango Guide", "p", "TXT", "c", 5, "2026-01-02 10:00:00"));
        return docs;
    }

    @Test
    void bubbleSortOrdersTitlesAlphabetically() {
        List<Document> docs = buildUnsortedDocuments();
        BubbleSort.sortByTitle(docs);

        assertEquals("Apple Basics", docs.get(0).getTitle());
        assertEquals("Mango Guide", docs.get(1).getTitle());
        assertEquals("Zebra Patterns", docs.get(2).getTitle());
    }

    @Test
    void bubbleSortHandlesAlreadySortedListWithoutErrors() {
        List<Document> docs = buildUnsortedDocuments();
        BubbleSort.sortByTitle(docs);
        BubbleSort.sortByTitle(docs); // sorting an already-sorted list should be a no-op
        assertEquals("Apple Basics", docs.get(0).getTitle());
    }

    @Test
    void selectionSortOrdersByDateNewestFirst() {
        List<Document> docs = buildUnsortedDocuments();
        SelectionSort.sortByDate(docs);

        assertEquals("2026-01-03 10:00:00", docs.get(0).getCreatedAt());
        assertEquals("2026-01-02 10:00:00", docs.get(1).getCreatedAt());
        assertEquals("2026-01-01 10:00:00", docs.get(2).getCreatedAt());
    }

    @Test
    void insertionSortOrdersByIdAscending() {
        List<Document> docs = buildUnsortedDocuments();
        InsertionSort.sortById(docs);

        assertEquals(1, docs.get(0).getId());
        assertEquals(2, docs.get(1).getId());
        assertEquals(3, docs.get(2).getId());
    }

    @Test
    void sortingEmptyListDoesNotThrow() {
        List<Document> empty = new ArrayList<>();
        assertDoesNotThrow(() -> BubbleSort.sortByTitle(empty));
        assertDoesNotThrow(() -> SelectionSort.sortByDate(empty));
        assertDoesNotThrow(() -> InsertionSort.sortById(empty));
    }

    @Test
    void sortingSingleElementListDoesNotThrow() {
        List<Document> single = new ArrayList<>(List.of(
                new Document(1, "Only Doc", "p", "TXT", "c", 5, "2026-01-01 10:00:00")));
        assertDoesNotThrow(() -> InsertionSort.sortById(single));
        assertEquals("Only Doc", single.get(0).getTitle());
    }
}
