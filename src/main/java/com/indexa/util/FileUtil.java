package com.indexa.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for reading document files from disk and validating
 * that they are supported types. Currently only TXT is supported, per
 * the project's Step 9 scope - other file types can be added later by
 * extending isSupportedFile().
 */
public class FileUtil {

    /**
     * Reads the entire contents of a text file into a single String.
     */
    public static String readFileContent(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * Returns true if the file exists and has a supported extension.
     */
    public static boolean isSupportedFile(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".txt");
    }

    /**
     * Lists every supported document file inside a directory
     * (non-recursive - just the top level, which is enough for the
     * sample-documents folder).
     */
    public static List<File> listSupportedFiles(String directoryPath) {
        List<File> files = new ArrayList<>();
        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            return files; // caller (IndexingService) decides how to handle "no folder found"
        }

        File[] entries = dir.listFiles();
        if (entries == null) {
            return files;
        }

        for (File entry : entries) {
            if (isSupportedFile(entry)) {
                files.add(entry);
            }
        }
        return files;
    }

    /**
     * Turns a filename like "binary-search.txt" into a readable title
     * like "Binary Search", used as a fallback when a document's first
     * line isn't a good title.
     */
    public static String filenameToTitle(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }
        name = name.replace('-', ' ').replace('_', ' ');

        StringBuilder titleCase = new StringBuilder();
        for (String word : name.split(" ")) {
            if (word.isBlank()) continue;
            titleCase.append(Character.toUpperCase(word.charAt(0)))
                     .append(word.substring(1))
                     .append(" ");
        }
        return titleCase.toString().trim();
    }

    /**
     * Counts words in a block of text by splitting on whitespace.
     * Used to populate Document.wordCount for the statistics screen.
     */
    public static int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
