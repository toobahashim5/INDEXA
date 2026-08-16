package com.indexa.dsa;

import java.util.List;

/**
 * Binary search: repeatedly halves the search range in a SORTED list
 * until the target is found or the range is empty. Requires the input
 * to already be sorted - if it isn't, results are undefined, which is
 * why INDEXA always sorts (via one of the Sort classes) before using
 * this on document data.
 */
public class BinarySearch {

    /**
     * Searches a sorted list of Integers. Returns the index of target,
     * or -1 if not present.
     *
     * Time complexity: O(log n), since every comparison eliminates
     * half of the remaining search space.
     */
    public static int search(List<Integer> sortedList, int target) {
        if (sortedList == null || sortedList.isEmpty()) {
            return -1;
        }
        int low = 0;
        int high = sortedList.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int midValue = sortedList.get(mid);

            if (midValue == target) {
                return mid;
            } else if (midValue < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Generic version for any Comparable type (e.g. sorted document
     * titles), using the same halving logic.
     */
    public static <T extends Comparable<T>> int search(List<T> sortedList, T target) {
        if (sortedList == null || sortedList.isEmpty() || target == null) {
            return -1;
        }
        int low = 0;
        int high = sortedList.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = sortedList.get(mid).compareTo(target);

            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }
}
