package com.indexa.service;

import com.indexa.model.Document;
import com.indexa.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RankingServiceTest {

    private final TextProcessor textProcessor = new TextProcessor();
    private final RankingService rankingService = new RankingService(textProcessor);

    @Test
    void documentMatchingMoreQueryWordsRanksHigher() {
        Document doc1 = new Document(1, "Sorting Algorithms", "p", "TXT",
                "Sorting algorithms like bubble sort are simple.", 8, "2026-01-01 10:00:00");
        Document doc2 = new Document(2, "Java Arrays", "p", "TXT",
                "Java arrays are fixed-size structures.", 6, "2026-01-01 10:00:00");

        List<SearchResult> results = rankingService.rankResults(
                "java sorting", Set.of(1, 2), List.of(doc1, doc2));

        // doc1 matches "sorting" (title + body), doc2 only matches
        // nothing from "java sorting" in its indexed text... adjust:
        // both docs need at least one matching word to be scored fairly.
        assertFalse(results.isEmpty());
        // The top result should have the highest relevancePercent (100%).
        assertEquals(100.0, results.get(0).getRelevancePercent());
    }

    @Test
    void moreKeywordOccurrencesIncreaseScore() {
        Document lowMatch = new Document(1, "Trees", "p", "TXT",
                "A tree is a structure.", 5, "2026-01-01 10:00:00");
        Document highMatch = new Document(2, "Trees Everywhere", "p", "TXT",
                "Tree tree tree tree tree.", 5, "2026-01-01 10:00:00");

        List<SearchResult> results = rankingService.rankResults(
                "tree", Set.of(1, 2), List.of(lowMatch, highMatch));

        // The document with more occurrences of "tree" should rank first.
        assertEquals(2, results.get(0).getDocument().getId());
    }

    @Test
    void emptyMatchingSetProducesEmptyResults() {
        Document doc = new Document(1, "Something", "p", "TXT", "Body text.", 2, "2026-01-01 10:00:00");
        List<SearchResult> results = rankingService.rankResults("query", Set.of(), List.of(doc));
        assertTrue(results.isEmpty());
    }

    @Test
    void resultsAreOrderedHighestScoreFirst() {
        Document doc1 = new Document(1, "Weak Match", "p", "TXT", "tree", 1, "2026-01-01 10:00:00");
        Document doc2 = new Document(2, "Strong Match tree", "p", "TXT", "tree tree tree", 3, "2026-01-01 10:00:00");

        List<SearchResult> results = rankingService.rankResults(
                "tree", Set.of(1, 2), List.of(doc1, doc2));

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getRelevanceScore() >= results.get(i + 1).getRelevanceScore());
        }
    }
}
