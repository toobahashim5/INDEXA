package com.indexa.service;

import com.indexa.dsa.InvertedIndex;

import java.util.HashSet;
import java.util.Set;

/**
 * The core search pipeline, following the exact workflow from the
 * project spec:
 *
 *   User enters query
 *        -> Text preprocessing (TextProcessor: lowercase, punctuation,
 *           tokenize, stop-word removal)
 *        -> Inverted Index lookup
 *        -> Find matching documents
 *
 * Ranking (relevance scoring + Max Heap) is layered on top of this in
 * Step 8 by RankingService - this class's job is only to find WHICH
 * documents match, not to rank them.
 *
 * Note: this class does NOT touch SQLite at all. Matching is done
 * entirely through the in-memory InvertedIndex, per the project rule
 * that the core search must depend on Java DSA, not SQL queries.
 */
public class SearchEngine {

    private final InvertedIndex invertedIndex;
    private final TextProcessor textProcessor;

    public SearchEngine(InvertedIndex invertedIndex, TextProcessor textProcessor) {
        this.invertedIndex = invertedIndex;
        this.textProcessor = textProcessor;
    }

    /**
     * Multi-keyword search (OR semantics): returns every document ID
     * that contains AT LEAST ONE of the query's keywords.
     *
     * Example: query "java array" matches any document containing
     * "java" OR "array" (or both). RankingService will later use the
     * NUMBER of matching keywords to rank documents with more matches
     * higher.
     *
     * Time complexity: O(k) InvertedIndex lookups (k = number of
     * keywords in the query, after stop-word removal), each average
     * O(1), plus O(m) to merge the result sets (m = total matching
     * document references across all keywords).
     */
    public Set<Integer> search(String query) {
        Set<Integer> matchingDocumentIds = new HashSet<>();

        if (query == null || query.isBlank()) {
            return matchingDocumentIds;
        }

        // Same TextProcessor pipeline used when documents were indexed,
        // so "Data Structures!" and "data structures" behave identically.
        var keywords = textProcessor.process(query);

        for (String keyword : keywords) {
            Set<Integer> docsForKeyword = invertedIndex.getDocumentsForKeyword(keyword);
            matchingDocumentIds.addAll(docsForKeyword);
        }

        return matchingDocumentIds;
    }

    /**
     * Exact-phrase search (AND semantics, on a single combined phrase):
     * returns only documents that contain EVERY keyword in the phrase.
     *
     * Example: query "binary search" only matches documents containing
     * BOTH "binary" AND "search" - unlike the OR-based search() above.
     * (True phrase adjacency - i.e. the words appearing next to each
     * other in that exact order - is refined further in Step 8's
     * RankingService, which re-checks the raw document content for an
     * exact substring match to award the +15 phrase bonus.)
     *
     * Time complexity: O(k) lookups + O(m) set intersection, same
     * variables as search() above.
     */
    public Set<Integer> searchExactPhrase(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            return new HashSet<>();
        }

        var keywords = textProcessor.process(phrase);
        if (keywords.isEmpty()) {
            return new HashSet<>();
        }

        // Start with the documents matching the first keyword...
        Set<Integer> result = new HashSet<>(invertedIndex.getDocumentsForKeyword(keywords.get(0)));

        // ...then keep only documents that ALSO appear for every other
        // keyword (set intersection = AND logic).
        for (int i = 1; i < keywords.size(); i++) {
            result.retainAll(invertedIndex.getDocumentsForKeyword(keywords.get(i)));
        }

        return result;
    }
}
