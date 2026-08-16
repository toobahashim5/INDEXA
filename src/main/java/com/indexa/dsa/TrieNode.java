package com.indexa.dsa;

import java.util.HashMap;
import java.util.Map;

/**
 * A single node in the Trie (prefix tree).
 *
 * Each node represents one character position. Its children map holds
 * "what character comes next" -> "the node for that character".
 *
 * Example: after inserting "java", the path through the tree is:
 *   root -> 'j' -> 'a' -> 'v' -> 'a' (this last node has isEndOfWord = true)
 *
 * HashMap<Character, TrieNode> is used for children (instead of a
 * fixed 26-slot array) because it only allocates space for characters
 * that are actually used, and lookup is still average O(1).
 */
public class TrieNode {

    private final Map<Character, TrieNode> children;
    private boolean isEndOfWord;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean hasChild(char c) {
        return children.containsKey(c);
    }

    public TrieNode getChild(char c) {
        return children.get(c);
    }

    public TrieNode addChild(char c) {
        return children.computeIfAbsent(c, k -> new TrieNode());
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        this.isEndOfWord = endOfWord;
    }
}
