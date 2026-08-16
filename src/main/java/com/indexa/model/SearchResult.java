package com.indexa.model;

import java.util.List;

/**
 * Represents one ranked result produced by the search engine for a
 * single query. This is NOT a database table - it's built fresh every
 * time a search runs, by combining a Document with a relevance score
 * calculated by RankingService (Step 8).
 *
 * Implements Comparable so the MaxHeap (Step 8) can order results by
 * score without needing a separate Comparator.
 */
public class SearchResult implements Comparable<SearchResult> {

    private Document document;
    private double relevanceScore;   // raw score, e.g. 27.0
    private double relevancePercent; // normalized for display, e.g. 94.0 (%)
    private List<String> matchedKeywords;
    private String snippet;

    public SearchResult(Document document, double relevanceScore, List<String> matchedKeywords, String snippet) {
        this.document = document;
        this.relevanceScore = relevanceScore;
        this.matchedKeywords = matchedKeywords;
        this.snippet = snippet;
    }

    public Document getDocument() {
        return document;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public double getRelevancePercent() {
        return relevancePercent;
    }

    public void setRelevancePercent(double relevancePercent) {
        this.relevancePercent = relevancePercent;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public String getSnippet() {
        return snippet;
    }

    /**
     * Defines the natural ordering: higher score = "greater".
     * The MaxHeap uses this to always keep the highest-scoring result
     * at the top, so results come out most-relevant-first.
     */
    @Override
    public int compareTo(SearchResult other) {
        return Double.compare(this.relevanceScore, other.relevanceScore);
    }

    @Override
    public String toString() {
        return "SearchResult{doc='" + document.getTitle() + "', score=" + relevanceScore + "}";
    }
}
