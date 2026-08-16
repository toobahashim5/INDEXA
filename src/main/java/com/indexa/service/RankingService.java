package com.indexa.service;

import com.indexa.dsa.MaxHeap;
import com.indexa.model.Document;
import com.indexa.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calculates a relevance score for each matching document and returns
 * them ordered highest-first, using the MaxHeap (Step 8).
 *
 * Scoring rules (exactly as specified in the project requirements):
 *   Title keyword match:      +10 per query keyword found in the title
 *   Keyword occurrence:       +2 per occurrence in the document body
 *   Exact phrase match:       +15 if the full query appears as a
 *                              contiguous phrase in the content
 *   More matching terms:      +5 per distinct query keyword matched
 *                              anywhere (rewards documents that cover
 *                              more of the query, not just one word
 *                              repeated many times)
 *
 * No random or hardcoded scores anywhere - every point comes from an
 * actual count against the document's real text.
 */
public class RankingService {

    private final TextProcessor textProcessor;

    public RankingService(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
    }

    /**
     * Scores every candidate document against the query, then uses a
     * MaxHeap to pop them out in highest-relevance-first order.
     *
     * @param query           the raw user query, e.g. "binary search"
     * @param matchingDocIds  document IDs already found by
     *                        SearchEngine.search() (Step 7)
     * @param allDocuments    the full documents so we can read their
     *                        title/content to score them
     */
    public List<SearchResult> rankResults(String query, Set<Integer> matchingDocIds,
                                           List<Document> allDocuments) {
        long startTime = System.nanoTime();

        MaxHeap<SearchResult> heap = new MaxHeap<>();
        List<String> queryKeywords = textProcessor.process(query);
        String normalizedQuery = String.join(" ", queryKeywords);

        for (Document doc : allDocuments) {
            if (!matchingDocIds.contains(doc.getId())) {
                continue; // only score documents SearchEngine already matched
            }
            SearchResult result = scoreDocument(doc, queryKeywords, normalizedQuery);
            heap.insert(result);
        }

        // Find the highest score so we can normalize everything else
        // into a 0-100% relevance display, without ever hardcoding it.
        double maxScore = heap.isEmpty() ? 0 : heap.peek().getRelevanceScore();

        List<SearchResult> ranked = new ArrayList<>();
        while (!heap.isEmpty()) {
            SearchResult result = heap.extractMax();
            double percent = maxScore > 0 ? (result.getRelevanceScore() / maxScore) * 100.0 : 0;
            result.setRelevancePercent(Math.round(percent * 10.0) / 10.0); // 1 decimal place
            ranked.add(result);
        }

        long elapsedNanos = System.nanoTime() - startTime;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        System.out.printf("[INDEXA] Ranked %d result(s) in %.4f seconds%n", ranked.size(), elapsedSeconds);

        return ranked;
    }

    /**
     * Computes one document's raw score plus a short snippet and its
     * list of matched keywords, packaged into a SearchResult.
     */
    private SearchResult scoreDocument(Document doc, List<String> queryKeywords, String normalizedQuery) {
        String titleLower = doc.getTitle() == null ? "" : doc.getTitle().toLowerCase();
        String contentLower = doc.getContent() == null ? "" : doc.getContent().toLowerCase();

        double score = 0;
        List<String> matchedKeywords = new ArrayList<>();

        for (String keyword : queryKeywords) {
            boolean matchedThisKeyword = false;

            // Title match: +10
            if (titleLower.contains(keyword)) {
                score += 10;
                matchedThisKeyword = true;
            }

            // Occurrence count in the body: +2 each
            int occurrences = countOccurrences(contentLower, keyword);
            if (occurrences > 0) {
                score += occurrences * 2;
                matchedThisKeyword = true;
            }

            if (matchedThisKeyword) {
                matchedKeywords.add(keyword);
            }
        }

        // More matching distinct terms = higher relevance
        score += matchedKeywords.size() * 5;

        // Exact phrase bonus: +15 if the whole query appears as a
        // contiguous substring in the content (e.g. "binary search"
        // appearing next to each other, not just both present anywhere)
        if (!normalizedQuery.isBlank() && contentLower.contains(normalizedQuery)) {
            score += 15;
        }

        String snippet = buildSnippet(doc.getContent(), queryKeywords);

        return new SearchResult(doc, score, matchedKeywords, snippet);
    }

    /**
     * Counts how many times a keyword appears in the text (non-overlapping).
     * Used for the "+2 per occurrence" scoring rule.
     */
    private int countOccurrences(String text, String keyword) {
        if (text.isBlank() || keyword.isBlank()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    /**
     * Builds a short preview snippet around the first matched keyword,
     * so results show "...data structures provide efficient..." style
     * previews instead of the entire document.
     */
    private String buildSnippet(String content, List<String> queryKeywords) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String lower = content.toLowerCase();

        // Skip past the first line (the document's title line) so the
        // snippet always shows BODY text, never a repeat of the title
        // that's already shown above it on the card.
        int bodyStart = lower.indexOf('\n');
        bodyStart = (bodyStart == -1) ? 0 : bodyStart + 1;

        int firstMatchIndex = -1;
        for (String keyword : queryKeywords) {
            int idx = lower.indexOf(keyword, bodyStart);
            if (idx != -1 && (firstMatchIndex == -1 || idx < firstMatchIndex)) {
                firstMatchIndex = idx;
            }
        }

        if (firstMatchIndex == -1) {
            // No keyword found in the body (matched only via title) -
            // just show the start of the body text.
            firstMatchIndex = bodyStart;
        }

        int snippetStart = Math.max(bodyStart, firstMatchIndex - 40);
        int snippetEnd = Math.min(content.length(), firstMatchIndex + 100);
        String snippet = content.substring(snippetStart, snippetEnd).trim();

        if (snippetStart > 0) snippet = "..." + snippet;
        if (snippetEnd < content.length()) snippet = snippet + "...";

        return snippet;
    }
}
