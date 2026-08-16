package com.indexa.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("java", 1);
        index.addDocument("java", 3);
        index.addDocument("tree", 1);
        index.addDocument("tree", 2);
    }

    @Test
    void getDocumentsForKeywordReturnsCorrectSet() {
        assertEquals(Set.of(1, 3), index.getDocumentsForKeyword("java"));
    }

    @Test
    void unknownKeywordReturnsEmptySetNotNull() {
        Set<Integer> result = index.getDocumentsForKeyword("nonexistent");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addingSameDocumentTwiceDoesNotCreateDuplicates() {
        // HashSet inside the index should naturally prevent the same
        // document ID from appearing twice for one keyword, even if
        // the keyword occurs multiple times in that document.
        index.addDocument("java", 1); // already added above
        assertEquals(2, index.getDocumentsForKeyword("java").size());
    }

    @Test
    void searchKeywordIsCaseInsensitive() {
        assertTrue(index.searchKeyword("JAVA"));
    }

    @Test
    void removeDocumentRemovesOnlyThatMapping() {
        index.removeDocument("java", 1);
        assertEquals(Set.of(3), index.getDocumentsForKeyword("java"));
    }

    @Test
    void removingLastDocumentForAKeywordRemovesTheKeywordEntirely() {
        index.removeDocument("tree", 1);
        index.removeDocument("tree", 2);
        assertFalse(index.searchKeyword("tree"));
    }

    @Test
    void removeDocumentEverywhereClearsAllOccurrences() {
        index.removeDocumentEverywhere(1);
        assertFalse(index.getDocumentsForKeyword("java").contains(1));
        assertFalse(index.getDocumentsForKeyword("tree").contains(1));
    }

    @Test
    void uniqueKeywordCountIsAccurate() {
        assertEquals(2, index.getUniqueKeywordCount()); // "java" and "tree"
    }

    @Test
    void clearIndexRemovesEverything() {
        index.clearIndex();
        assertEquals(0, index.getUniqueKeywordCount());
        assertTrue(index.getDocumentsForKeyword("java").isEmpty());
    }
}
