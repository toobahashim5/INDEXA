package com.indexa.dsa;

import com.indexa.model.Document;

import java.util.List;

/**
 * Insertion Sort: builds the sorted list one element at a time,
 * taking each new element and inserting it into its correct position
 * among the already-sorted elements before it - like sorting playing
 * cards in your hand. Backs the default ID-ordered view on the
 * Document Management screen.
 */
public class InsertionSort {

    /**
     * Sorts documents by their database id, ascending, in place.
     *
     * Time complexity: O(n^2) worst case (input in reverse order),
     * but close to O(n) best case for already-sorted or nearly-sorted
     * input - each element only needs to move a short distance,
     * unlike Bubble/Selection Sort which always scan the full
     * remaining range regardless of how sorted it already is.
     */
    public static void sortById(List<Document> documents) {
        if (documents == null) {
            return;
        }
        int n = documents.size();
        for (int i = 1; i < n; i++) {
            Document key = documents.get(i);
            int j = i - 1;

            // Shift every element greater than key one position to the
            // right, opening up the correct slot for key.
            while (j >= 0 && documents.get(j).getId() > key.getId()) {
                documents.set(j + 1, documents.get(j));
                j--;
            }
            documents.set(j + 1, key);
        }
    }
}
