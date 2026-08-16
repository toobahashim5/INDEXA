package com.indexa.dsa;

import com.indexa.model.Document;

import java.util.List;

/**
 * Selection Sort: repeatedly finds the minimum (here: earliest date)
 * element from the unsorted portion and moves it to the front,
 * growing a sorted portion one element at a time. Backs the
 * "Sort by Date" filter.
 */
public class SelectionSort {

    /**
     * Sorts documents by creation date, most recent first, in place.
     *
     * Time complexity: O(n^2) in all cases - unlike Bubble Sort, it
     * always makes the same number of comparisons regardless of the
     * input's initial order, since it must scan the full unsorted
     * portion every pass to find the correct next element.
     */
    public static void sortByDate(List<Document> documents) {
        if (documents == null) {
            return;
        }
        int n = documents.size();
        for (int i = 0; i < n - 1; i++) {
            int selected = i; // index of the most-recent date found so far
            for (int j = i + 1; j < n; j++) {
                // ISO-formatted timestamps ("YYYY-MM-DD HH:MM:SS") sort
                // correctly with plain string comparison.
                String candidateDate = documents.get(j).getCreatedAt();
                String selectedDate = documents.get(selected).getCreatedAt();
                if (candidateDate != null && selectedDate != null
                        && candidateDate.compareTo(selectedDate) > 0) {
                    selected = j;
                }
            }
            if (selected != i) {
                swap(documents, i, selected);
            }
        }
    }

    private static void swap(List<Document> list, int i, int j) {
        Document temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    /**
     * Overload for the Search Results "Sort by Date" filter - same
     * selection-sort logic, operating on ranked SearchResult objects.
     */
    public static void sortResultsByDate(List<com.indexa.model.SearchResult> results) {
        if (results == null) {
            return;
        }
        int n = results.size();
        for (int i = 0; i < n - 1; i++) {
            int selected = i;
            for (int j = i + 1; j < n; j++) {
                String candidateDate = results.get(j).getDocument().getCreatedAt();
                String selectedDate = results.get(selected).getDocument().getCreatedAt();
                if (candidateDate != null && selectedDate != null
                        && candidateDate.compareTo(selectedDate) > 0) {
                    selected = j;
                }
            }
            if (selected != i) {
                com.indexa.model.SearchResult temp = results.get(i);
                results.set(i, results.get(selected));
                results.set(selected, temp);
            }
        }
    }
}
