package com.indexa.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    private Trie trie;

    @BeforeEach
    void setUp() {
        trie = new Trie();
        trie.insert("structures");
        trie.insert("string");
        trie.insert("stream");
        trie.insert("search");
        trie.insert("java");
    }

    @Test
    void exactSearchFindsInsertedWord() {
        assertTrue(trie.search("java"));
    }

    @Test
    void exactSearchFailsForPrefixThatIsNotAFullWord() {
        // "jav" is a valid prefix of "java" but was never inserted as
        // its own word, so search() must return false.
        assertFalse(trie.search("jav"));
    }

    @Test
    void prefixSearchFindsMultipleMatches() {
        List<String> suggestions = trie.getSuggestions("str", 5);
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.containsAll(List.of("structures", "string", "stream")));
    }

    @Test
    void suggestionsRespectTheLimitParameter() {
        List<String> suggestions = trie.getSuggestions("str", 2);
        assertEquals(2, suggestions.size());
    }

    @Test
    void unknownPrefixReturnsEmptySuggestions() {
        assertTrue(trie.getSuggestions("xyz", 5).isEmpty());
    }

    @Test
    void startsWithDetectsAnyValidPrefix() {
        assertTrue(trie.startsWith("str"));
        assertFalse(trie.startsWith("xyz"));
    }

    @Test
    void insertAndSearchAreCaseInsensitive() {
        trie.insert("Java");
        assertTrue(trie.search("java")); // already true, same word
        assertTrue(trie.search("JAVA")); // search also lowercases internally
    }

    @Test
    void emptyOrBlankPrefixReturnsNoSuggestions() {
        assertTrue(trie.getSuggestions("", 5).isEmpty());
        assertTrue(trie.getSuggestions(null, 5).isEmpty());
    }
}
