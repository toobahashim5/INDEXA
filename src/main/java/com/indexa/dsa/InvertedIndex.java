package com.indexa.dsa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The Inverted Index is the heart of INDEXA's search engine.
 *
 * Instead of storing "document -> words it contains" (which would
 * force us to scan every document on every search), we store the
 * reverse mapping: "word -> set of documents that contain it".
 * That's why it's called an "inverted" index.
 *
 * Structure: HashMap<String, HashSet<Integer>>
 *   key   = a keyword (already lowercased/cleaned by TextProcessor)
 *   value = the set of document IDs that contain that keyword
 *
 * Example after indexing a few documents:
 *   "java"  -> {1, 3, 5}
 *   "array" -> {2, 4}
 *   "tree"  -> {1, 2, 6}
 *
 * Why HashMap for the outer structure:
 *   Looking up all documents containing a keyword becomes a single
 *   HashMap.get() call - average O(1) - instead of scanning every
 *   document's text (O(n) documents x O(m) words each).
 *
 * Why HashSet for the inner structure (not ArrayList):
 *   1. A document should only be listed ONCE per keyword even if the
 *      keyword appears 5 times in that document (HashSet naturally
 *      prevents duplicate document IDs).
 *   2. Checking "does this document already contain this keyword?"
 *      or removing a document ID is average O(1) instead of O(n).
 */
public class InvertedIndex {

    private final Map<String, HashSet<Integer>> index;

    public InvertedIndex() {
        this.index = new HashMap<>();
    }

    /**
     * Adds one keyword occurrence for a document.
     * Called once per keyword while a document is being indexed
     * (IndexingService, Step 9, will loop over all of a document's
     * processed tokens and call this for each one).
     *
     * Time complexity: average O(1) - one HashMap lookup/insert plus
     * one HashSet insert.
     */
    public void addDocument(String keyword, int documentId) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        // computeIfAbsent: if this keyword has never been seen before,
        // create a new empty HashSet for it first. Either way, we get
        // back the HashSet to add the document ID into.
        index.computeIfAbsent(keyword, k -> new HashSet<>()).add(documentId);
    }

    /**
     * Removes a single keyword -> document mapping. Used when
     * re-indexing a document (its old keywords are cleared first).
     *
     * Time complexity: average O(1).
     */
    public void removeDocument(String keyword, int documentId) {
        HashSet<Integer> docIds = index.get(keyword);
        if (docIds != null) {
            docIds.remove(documentId);
            // Clean up: if no documents contain this keyword anymore,
            // remove the keyword entirely so getUniqueKeywordCount()
            // stays accurate.
            if (docIds.isEmpty()) {
                index.remove(keyword);
            }
        }
    }

    /**
     * Removes every occurrence of a document across the whole index.
     * Used when a document is deleted, or before re-indexing it.
     *
     * Time complexity: O(k) where k = number of unique keywords in
     * the entire index (we must check each keyword's set).
     */
    public void removeDocumentEverywhere(int documentId) {
        for (HashSet<Integer> docIds : index.values()) {
            docIds.remove(documentId);
        }
        // Clean up any keywords left with no documents.
        index.values().removeIf(Set::isEmpty);
    }

    /**
     * Returns true if the given keyword exists anywhere in the index.
     * Time complexity: average O(1).
     */
    public boolean searchKeyword(String keyword) {
        return keyword != null && index.containsKey(keyword.toLowerCase());
    }

    /**
     * Returns the set of document IDs containing the given keyword.
     * Returns an empty set (never null) if the keyword is unknown, so
     * callers don't need null checks.
     *
     * Time complexity: average O(1).
     */
    public Set<Integer> getDocumentsForKeyword(String keyword) {
        if (keyword == null) {
            return Collections.emptySet();
        }
        HashSet<Integer> docIds = index.get(keyword.toLowerCase());
        return docIds != null ? docIds : Collections.emptySet();
    }

    /**
     * Wipes the entire index. Used by "Clear Index" and before a full
     * "Re-index All" operation.
     */
    public void clearIndex() {
        index.clear();
    }

    /**
     * Number of distinct keywords currently indexed - shown on the
     * Index Statistics screen (Step 9 area).
     */
    public int getUniqueKeywordCount() {
        return index.size();
    }

    /**
     * Total number of (keyword, document) mappings in the index -
     * another useful statistic. Not the same as unique keyword count,
     * since one keyword can map to many documents.
     */
    public int getTotalIndexedTerms() {
        int total = 0;
        for (HashSet<Integer> docIds : index.values()) {
            total += docIds.size();
        }
        return total;
    }
}
