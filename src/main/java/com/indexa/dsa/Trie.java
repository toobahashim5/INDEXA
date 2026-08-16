package com.indexa.dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Trie (prefix tree) used for search-box autocomplete.
 *
 * Every keyword that gets added to the InvertedIndex during document
 * indexing (Step 9) is ALSO inserted here. That's what lets the UI
 * show live suggestions as the user types, e.g. typing "str" suggests
 * "structures", "string", "stream" - all words that exist somewhere
 * in the indexed documents.
 *
 * Why a Trie instead of just scanning all keywords with String.startsWith():
 *   Insert/search/prefix-lookup all run in O(L), where L is the length
 *   of the word/prefix being processed - completely independent of how
 *   many total words are stored. Scanning a flat list of N keywords
 *   with startsWith() would cost O(N x L) instead.
 */
public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    /**
     * Inserts a word into the Trie, one character at a time, creating
     * new nodes only where a path doesn't already exist.
     *
     * Time complexity: O(L), L = word length.
     */
    public void insert(String word) {
        if (word == null || word.isBlank()) {
            return;
        }
        word = word.toLowerCase();
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.addChild(c);
        }
        current.setEndOfWord(true);
    }

    /**
     * Returns true only if the exact word was inserted before
     * (not just a prefix of some other word).
     *
     * Time complexity: O(L), L = word length.
     */
    public boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord();
    }

    /**
     * Returns true if ANY inserted word starts with the given prefix.
     *
     * Time complexity: O(L), L = prefix length.
     */
    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    /**
     * Returns up to `limit` complete words that start with the given
     * prefix - this is exactly what powers the autocomplete dropdown.
     *
     * How it works:
     *   1. Walk down the Trie following the prefix's characters - O(L).
     *   2. From that point, explore all branches below it (DFS) and
     *      collect every complete word found, stopping once we have
     *      `limit` suggestions.
     */
    public List<String> getSuggestions(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        if (prefix == null || prefix.isBlank()) {
            return results;
        }
        prefix = prefix.toLowerCase();

        TrieNode prefixNode = findNode(prefix);
        if (prefixNode == null) {
            return results; // nothing indexed starts with this prefix
        }

        collectWords(prefixNode, new StringBuilder(prefix), results, limit);
        return results;
    }

    // ---------- Internal helpers ----------

    /**
     * Walks the Trie along the given string's characters and returns
     * the node reached at the end, or null if the path breaks early
     * (meaning no inserted word matches that path).
     */
    private TrieNode findNode(String text) {
        if (text == null) {
            return null;
        }
        text = text.toLowerCase();
        TrieNode current = root;
        for (char c : text.toCharArray()) {
            current = current.getChild(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Depth-first traversal from a given node, appending each
     * character it visits to `prefixSoFar`. Whenever it passes through
     * a node marked isEndOfWord, the accumulated string is a real
     * indexed word, so it's added to results.
     */
    private void collectWords(TrieNode node, StringBuilder prefixSoFar,
                               List<String> results, int limit) {
        if (results.size() >= limit) {
            return;
        }
        if (node.isEndOfWord()) {
            results.add(prefixSoFar.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            if (results.size() >= limit) {
                return;
            }
            prefixSoFar.append(entry.getKey());
            collectWords(entry.getValue(), prefixSoFar, results, limit);
            // backtrack: remove the character we just added so the
            // StringBuilder is correct for the NEXT sibling branch.
            prefixSoFar.deleteCharAt(prefixSoFar.length() - 1);
        }
    }
}
