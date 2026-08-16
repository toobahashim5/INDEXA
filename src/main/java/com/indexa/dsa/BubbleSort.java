package com.indexa.dsa;

import com.indexa.model.Document;

import java.util.List;

/**
 * Bubble Sort: repeatedly steps through the list, comparing adjacent
 * elements and swapping them if they're out of order. Each full pass
 * "bubbles" the largest remaining unsorted element to its correct
 * position. Simple to reason about, but O(n^2) - included here to
 * satisfy the project's sorting-algorithm requirement and to back the
 * "Sort by Title" filter, not because it's the fastest choice.
 */
public class BubbleSort {

    /**
     * Sorts documents alphabetically by title, in place.
     *
     * Time complexity: O(n^2) average and worst case, O(n) best case
     * (already-sorted input still needs one full pass to confirm no
     * swaps are needed).
     */
    public static void sortByTitle(List<Document> documents) {
        if (documents == null) {
            return;
        }
        int n = documents.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                String titleA = documents.get(j).getTitle();
                String titleB = documents.get(j + 1).getTitle();
                if (titleA.compareToIgnoreCase(titleB) > 0) {
                    swap(documents, j, j + 1);
                    swapped = true;
                }
            }
            // Optimization: if a full pass made no swaps, the list is
            // already sorted - stop early instead of doing needless passes.
            if (!swapped) {
                break;
            }
        }
    }

    private static void swap(List<Document> list, int i, int j) {
        Document temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    /**
     * Overload for the Search Results "Sort by Title" filter, which
     * works on ranked SearchResult objects rather than raw Document
     * objects - same bubble-sort logic, just comparing each result's
     * underlying document title.
     */
    public static void sortResultsByTitle(List<com.indexa.model.SearchResult> results) {
        if (results == null) {
            return;
        }
        int n = results.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                String titleA = results.get(j).getDocument().getTitle();
                String titleB = results.get(j + 1).getDocument().getTitle();
                if (titleA.compareToIgnoreCase(titleB) > 0) {
                    com.indexa.model.SearchResult temp = results.get(j);
                    results.set(j, results.get(j + 1));
                    results.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }
}
