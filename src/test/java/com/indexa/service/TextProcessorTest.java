package com.indexa.service;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    private final TextProcessor processor = new TextProcessor();

    @Test
    void normalTextIsLowercasedTokenizedAndStopWordsRemoved() {
        List<String> result = processor.process("Data Structures are important!");
        assertEquals(List.of("data", "structures", "important"), result);
    }

    @Test
    void emptyStringReturnsEmptyList() {
        assertTrue(processor.process("").isEmpty());
    }

    @Test
    void nullInputReturnsEmptyListInsteadOfCrashing() {
        assertTrue(processor.process(null).isEmpty());
    }

    @Test
    void uppercaseAndLowercaseProduceIdenticalTokens() {
        assertEquals(processor.process("BINARY SEARCH"), processor.process("binary search"));
    }

    @Test
    void punctuationIsStripped() {
        List<String> result = processor.process("Hello, world!!! Testing... 123?");
        assertEquals(List.of("hello", "world", "testing", "123"), result);
    }

    @Test
    void duplicateWordsAreKeptAsSeparateTokens() {
        // process() should NOT deduplicate - occurrence counting in
        // RankingService relies on repeated words staying repeated.
        List<String> result = processor.process("tree tree tree");
        assertEquals(3, result.size());
    }

    @Test
    void isStopWordDetectsCommonWords() {
        assertTrue(processor.isStopWord("the"));
        assertTrue(processor.isStopWord("AND")); // case-insensitive
        assertFalse(processor.isStopWord("algorithm"));
    }
}
