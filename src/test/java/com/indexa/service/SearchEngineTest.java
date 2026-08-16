package com.indexa.service;

import com.indexa.dsa.InvertedIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {

    private SearchEngine searchEngine;
    private InvertedIndex invertedIndex;
    private TextProcessor textProcessor;

    @BeforeEach
    void setUp() {
        textProcessor = new TextProcessor();
        invertedIndex = new InvertedIndex();
        searchEngine = new SearchEngine(invertedIndex, textProcessor);

        // Simulate 3 tiny indexed "documents":
        // doc 1: "java arrays"
        // doc 2: "binary search tree"
        // doc 3: "java sorting algorithms"
        index(1, "java arrays");
        index(2, "binary search tree");
        index(3, "java sorting algorithms");
    }

    private void index(int docId, String text) {
        for (String keyword : textProcessor.process(text)) {
            invertedIndex.addDocument(keyword, docId);
        }
    }

    @Test
    void normalSearchFindsMatchingDocuments() {
        assertEquals(Set.of(1, 3), searchEngine.search("java"));
    }

    @Test
    void multipleKeywordSearchUsesOrLogic() {
        // "java" matches docs 1 & 3, "tree" matches doc 2 -> union of all three
        assertEquals(Set.of(1, 2, 3), searchEngine.search("java tree"));
    }

    @Test
    void emptySearchReturnsEmptySet() {
        assertTrue(searchEngine.search("").isEmpty());
        assertTrue(searchEngine.search(null).isEmpty());
    }

    @Test
    void unknownKeywordReturnsNoResults() {
        assertTrue(searchEngine.search("photosynthesis").isEmpty());
    }

    @Test
    void searchIsCaseInsensitive() {
        assertEquals(searchEngine.search("JAVA"), searchEngine.search("java"));
    }

    @Test
    void exactPhraseSearchRequiresAllWordsInSameDocument() {
        // "binary search" -> only doc 2 has BOTH words
        assertEquals(Set.of(2), searchEngine.searchExactPhrase("binary search"));
    }

    @Test
    void exactPhraseSearchReturnsEmptyWhenWordsSpanDifferentDocuments() {
        // "arrays tree" - "arrays" is only in doc 1, "tree" only in doc 2 -> no doc has both
        assertTrue(searchEngine.searchExactPhrase("arrays tree").isEmpty());
    }

    @Test
    void noResultsCaseReturnsEmptyNotNull() {
        Set<Integer> result = searchEngine.search("nonexistentword");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
