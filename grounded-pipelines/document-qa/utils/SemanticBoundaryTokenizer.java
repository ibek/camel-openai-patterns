package org.apache.camel.tokenizer;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class SemanticBoundaryTokenizer {

    // Default configurations
    private int maxChars = 1024;
    private int overlapChars = 10;

    // Standard setters allow Camel to override defaults via Bean properties
    public void setMaxChars(int maxChars) { this.maxChars = maxChars; }
    public void setOverlapChars(int overlapChars) { this.overlapChars = overlapChars; }

    /**
     * @Handler tells Camel this is the default method to call on this bean.
     * @Body tells Camel to extract the Exchange IN body and pass it as this parameter.
     */
    @Handler
    public List<String> tokenize(@Body String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return chunk(text, this.maxChars, this.overlapChars);
    }

    // ------------------------------------------------------------------------
    // EMBEDDED RECURSIVE CHUNKING LOGIC
    // ------------------------------------------------------------------------
    private List<String> chunk(String text, int maxChars, int overlapChars) {
        List<String> finalChunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (paragraph.length() > maxChars) {
                if (currentChunk.length() > 0) {
                    finalChunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                finalChunks.addAll(chunkBySentence(paragraph, maxChars, overlapChars));
                continue; 
            }

            if (currentChunk.length() + paragraph.length() + 2 > maxChars && currentChunk.length() > 0) {
                finalChunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
            }
            
            currentChunk.append(paragraph).append("\n\n");
        }

        if (currentChunk.length() > 0) {
            finalChunks.add(currentChunk.toString().trim());
        }

        return finalChunks;
    }

    private List<String> chunkBySentence(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getSentenceInstance();
        boundary.setText(text);
        
        List<String> sentences = new ArrayList<>();
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            sentences.add(text.substring(start, end).trim());
        }

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (sentence.isEmpty()) continue;

            if (sentence.length() > maxChars) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                chunks.addAll(chunkFixedSize(sentence, maxChars, overlapChars));
                continue;
            }

            if (currentChunk.length() + sentence.length() + 1 > maxChars && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
            }
            
            currentChunk.append(sentence).append(" ");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> chunkFixedSize(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            chunks.add(text.substring(start, end));
            
            if (end == text.length()) {
                break;
            }
            start = end - overlapChars;
        }
        return chunks;
    }
}
