package com.indexa.service;

import com.indexa.dao.DocumentDAO;
import com.indexa.dsa.InvertedIndex;
import com.indexa.dsa.Trie;
import com.indexa.model.Document;
import com.indexa.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full document indexing pipeline, following the
 * exact steps from the project spec:
 *
 *   1. Read the file
 *   2. Extract text
 *   3. Process text (TextProcessor)
 *   4. Tokenize
 *   5. Remove stop words
 *   6. Add terms to Trie
 *   7. Add terms to Inverted Index
 *   8. Save document metadata in SQLite
 *
 * This is the class that finally replaces the "fake documents" used
 * for testing in Steps 5-8 with real files read from disk.
 */
public class IndexingService {

    private final InvertedIndex invertedIndex;
    private final Trie trie;
    private final TextProcessor textProcessor;
    private final DocumentDAO documentDAO;

    public IndexingService(InvertedIndex invertedIndex, Trie trie,
                            TextProcessor textProcessor, DocumentDAO documentDAO) {
        this.invertedIndex = invertedIndex;
        this.trie = trie;
        this.textProcessor = textProcessor;
        this.documentDAO = documentDAO;
    }

    /**
     * Indexes a single TXT file: reads it, processes its text, adds
     * every keyword to the InvertedIndex and Trie, and saves its
     * metadata to SQLite.
     *
     * Returns the saved Document (with its database-generated id),
     * or null if the file was unsupported/unreadable (caller can
     * report this through a JavaFX Alert in the real UI - Step 10+).
     */
    public Document indexDocument(File file) throws IOException, SQLException {
        if (!FileUtil.isSupportedFile(file)) {
            System.out.println("[INDEXA] Skipped unsupported file: " + file.getName());
            return null;
        }

        if (documentDAO.existsByFilePath(file.getPath())) {
            System.out.println("[INDEXA] Skipped already-indexed file: " + file.getName());
            return null;
        }

        String content = FileUtil.readFileContent(file);
        if (content.isBlank()) {
            System.out.println("[INDEXA] Skipped empty file: " + file.getName());
            return null;
        }

        String title = extractTitle(content, file);
        int wordCount = FileUtil.countWords(content);

        Document document = new Document(title, file.getPath(), "TXT", content, wordCount);
        document = documentDAO.insertDocument(document); // now has a real database id

        // Process the FULL text (title + body) so searches also match
        // words that only appear in the title.
        List<String> keywords = textProcessor.process(title + " " + content);
        for (String keyword : keywords) {
            invertedIndex.addDocument(keyword, document.getId());
            trie.insert(keyword);
        }

        System.out.println("[INDEXA] Indexed: " + title + " (" + keywords.size() + " keyword occurrences)");
        return document;
    }

    /**
     * Indexes every supported file in a directory. Used for the
     * initial load of the sample-documents folder, and for "Add
     * Document" workflows pointed at a folder.
     */
    public List<Document> indexDirectory(String directoryPath) {
        List<Document> indexed = new ArrayList<>();
        List<File> files = FileUtil.listSupportedFiles(directoryPath);

        if (files.isEmpty()) {
            System.out.println("[INDEXA] No supported files found in: " + directoryPath);
            return indexed;
        }

        for (File file : files) {
            try {
                Document doc = indexDocument(file);
                if (doc != null) {
                    indexed.add(doc);
                }
            } catch (IOException | SQLException e) {
                // Per the error-handling rule: never show raw stack
                // traces. In the real UI this becomes a JavaFX Alert;
                // for now we log a clean message and continue with
                // the remaining files instead of stopping everything.
                System.err.println("[INDEXA] Failed to index " + file.getName() + ": " + e.getMessage());
            }
        }
        return indexed;
    }

    /**
     * Wipes the in-memory index (InvertedIndex) and the DOCUMENTS
     * table, then re-indexes everything from the given directory.
     * This backs the "Re-index All" button (Step 9 UI area).
     *
     * Note: the Trie is intentionally left as a fresh instance by the
     * caller when doing a full re-index in the real UI, since Trie
     * has no "remove" operation (removal from a Trie is unnecessary
     * for this project's autocomplete use case).
     */
    public List<Document> reindexAll(String directoryPath) throws SQLException {
        invertedIndex.clearIndex();
        documentDAO.deleteAllDocuments();
        return indexDirectory(directoryPath);
    }

    // ---------- Internal helper ----------

    /**
     * Uses the file's first non-blank line as the title if it looks
     * like a reasonable title (short), otherwise falls back to
     * converting the filename into a title.
     */
    private String extractTitle(String content, File file) {
        String firstLine = content.strip().split("\\r?\\n", 2)[0].strip();
        if (!firstLine.isBlank() && firstLine.length() <= 80) {
            return firstLine;
        }
        return FileUtil.filenameToTitle(file);
    }
}
