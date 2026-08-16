package com.indexa.dsa;

import java.util.List;

/**
 * Linear (sequential) search: checks each element in order until a
 * match is found. Requires no sorting, unlike BinarySearch, but is
 * O(n) in the worst case since it may have to check every element.
 *
 * Used in INDEXA for small, unsorted scans - e.g. checking whether a
 * document ID appears anywhere in a short, unsorted candidate list,
 * where sorting first would cost more than it saves.
 */
public class LinearSearch {

    /**
     * Returns the index of the first element equal to target, or -1
     * if not found.
     *
     * Time complexity: O(n) worst case, O(1) best case (target is the
     * first element).
     */
    public static <T> int search(List<T> list, T target) {
        if (list == null || target == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            if (target.equals(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Case-insensitive convenience version for searching a list of
     * String keywords - matches how INDEXA normalizes text elsewhere
     * (TextProcessor always lowercases first).
     */
    public static int searchKeyword(List<String> words, String target) {
        if (words == null || target == null) {
            return -1;
        }
        String targetLower = target.toLowerCase();
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i) != null && words.get(i).toLowerCase().equals(targetLower)) {
                return i;
            }
        }
        return -1;
    }
}
