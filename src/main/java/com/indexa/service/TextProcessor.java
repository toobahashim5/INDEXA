package com.indexa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts raw text into a clean list of searchable keywords.
 *
 * This exact pipeline is used in TWO places later in the project:
 *   1. When a document is indexed (IndexingService, Step 9) - to decide
 *      which words go into the Trie and InvertedIndex.
 *   2. When a user types a search query (SearchEngine, Step 7) - so the
 *      query is normalized the same way the stored documents were.
 *
 * Using one shared processor for both is what makes "Data Structures!"
 * (typed by a user) correctly match "data structures" (stored in a doc).
 *
 * Example:
 *   Input:  "Data Structures are important!"
 *   Output: [data, structures, important]
 *   ("are" is removed because it's a stop word)
 */
public class TextProcessor {

    /**
     * A reasonable stop-word list: common English words that appear in
     * almost every document and carry no real search meaning. Storing
     * them in the index would waste memory and hurt ranking quality
     * (every document would "match" them).
     *
     * HashSet is used here (not ArrayList) because we only ever ask
     * "is this word a stop word?" - a lookup HashSet answers in
     * average O(1), whereas scanning an ArrayList would be O(n) per
     * word, for every single word in every document.
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "and", "or", "but", "if", "so", "of", "in", "on", "at", "to", "for",
            "with", "as", "by", "from", "about", "into", "through", "during",
            "before", "after", "above", "below", "up", "down", "over", "under",
            "again", "further", "then", "once", "here", "there", "when", "where",
            "why", "how", "all", "any", "both", "each", "few", "more", "most",
            "other", "some", "such", "no", "nor", "not", "only", "own", "same",
            "than", "too", "very", "s", "t", "can", "will", "just", "don",
            "should", "now", "it", "its", "this", "that", "these", "those",
            "i", "you", "he", "she", "we", "they", "them", "his", "her", "their",
            "what", "which", "who", "whom", "have", "has", "had", "having",
            "do", "does", "did", "doing", "am", "im", "would", "could", "shall"
    ));

    /**
     * Runs the full pipeline and returns clean tokens ready for
     * indexing or lookup.
     */
    public List<String> process(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ArrayList<>();
        }

        String lower = toLowerCase(rawText);
        String noPunctuation = removePunctuation(lower);
        String normalized = normalizeSpaces(noPunctuation);
        List<String> tokens = tokenize(normalized);

        return removeStopWords(tokens);
    }

    /**
     * Same pipeline, but keeps stop words. Useful later for features
     * like exact-phrase search, where "to be or not to be" needs its
     * stop words intact to match phrase-for-phrase.
     */
    public List<String> processKeepStopWords(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ArrayList<>();
        }
        String lower = toLowerCase(rawText);
        String noPunctuation = removePunctuation(lower);
        String normalized = normalizeSpaces(noPunctuation);
        return tokenize(normalized);
    }

    // ---------- Pipeline steps (each kept separate & testable) ----------

    public String toLowerCase(String text) {
        return text.toLowerCase();
    }

    /**
     * Removes anything that isn't a letter, digit, or whitespace.
     * Regex explanation: [^a-z0-9\\s] means "any character that is
     * NOT a lowercase letter, digit, or whitespace" - replace it with
     * nothing.
     */
    public String removePunctuation(String text) {
        return text.replaceAll("[^a-z0-9\\s]", " ");
    }

    /**
     * Collapses multiple spaces/tabs/newlines into a single space, and
     * trims leading/trailing whitespace.
     */
    public String normalizeSpaces(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Splits the cleaned text on whitespace into individual word tokens.
     */
    public List<String> tokenize(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split(" ")));
    }

    /**
     * Filters out stop words and empty tokens.
     */
    public List<String> removeStopWords(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank() && !STOP_WORDS.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }

    public boolean isStopWord(String word) {
        return STOP_WORDS.contains(word.toLowerCase());
    }
}
