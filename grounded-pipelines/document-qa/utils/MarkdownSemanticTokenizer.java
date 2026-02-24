package org.apache.camel.tokenizer;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple Markdown splitter.
 *
 * Splits on headers, code blocks, and horizontal rules while tracking
 * the header hierarchy as context for each chunk. Rendering behavior is controlled
 * by `headerMode` (RAW vs RAG_CONTEXT).
 */
public class MarkdownSemanticTokenizer {

    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6}) (.*)");
    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("^(```|~~~)(.*)");
    private static final Pattern HORZ_RULE_PATTERN = Pattern.compile("^(\\*{3,}|-{3,}|_{3,})\\s*$");

    public enum HeaderMode {
        RAG_CONTEXT,   // Strips raw header line, prepends hierarchy (Best for Vector DB)
        RAW            // Keeps raw header line, no hierarchy injection (Best for Archival)
    }

    private HeaderMode mode = HeaderMode.RAG_CONTEXT;
    private boolean returnEachLine = false;

    public void setHeaderMode(String modeStr) {
        this.mode = HeaderMode.valueOf(modeStr.toUpperCase());
    }

    public void setReturnEachLine(String v) { this.returnEachLine = Boolean.parseBoolean(v); }
    public void setReturnEachLine(boolean v) { this.returnEachLine = v; }

    @Handler
    public List<String> tokenize(@Body String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return splitText(text);
    }

    /**
     * Split the input markdown text into chunks.
     */
    private List<String> splitText(String text) {
        List<Chunk> chunks = new ArrayList<>();
        Chunk current = new Chunk();
        List<HeaderEntry> headerStack = new ArrayList<>();

        Deque<String> lines = new ArrayDeque<>(Arrays.asList(text.split("\\r?\\n", -1)));

        while (!lines.isEmpty()) {
            String line = lines.poll();

            Matcher headerMatch = HEADER_PATTERN.matcher(line);
            Matcher codeMatch = CODE_FENCE_PATTERN.matcher(line);
            Matcher horzMatch = HORZ_RULE_PATTERN.matcher(line);

            if (headerMatch.matches()) {
                completeChunk(chunks, current, headerStack);
                current = new Chunk();

                int depth = headerMatch.group(1).length();
                String headerText = headerMatch.group(2).trim();
                resolveHeaderStack(headerStack, depth, headerText);

                if (this.mode == HeaderMode.RAW) {
                    current.content.append(line).append("\n");
                }
            } else if (codeMatch.matches()) {
                completeChunk(chunks, current, headerStack);

                // Collect the entire code block as its own chunk
                String language = codeMatch.group(2).trim();
                String openingFence = codeMatch.group(1);
                StringBuilder codeContent = new StringBuilder(line).append("\n");

                while (!lines.isEmpty()) {
                    String codeLine = lines.poll();
                    codeContent.append(codeLine).append("\n");
                    if (codeLine.trim().startsWith(openingFence)) {
                        break;
                    }
                }

                Chunk codeChunk = new Chunk();
                codeChunk.content.append(codeContent);
                if (!language.isEmpty()) {
                    codeChunk.codeLanguage = language;
                }
                completeChunk(chunks, codeChunk, headerStack);
                current = new Chunk();
            } else if (horzMatch.matches()) {
                completeChunk(chunks, current, headerStack);
                current = new Chunk();
            } else {
                current.content.append(line).append("\n");
            }
        }

        completeChunk(chunks, current, headerStack);

        // Render chunks to strings
        if (returnEachLine) {
            List<String> result = new ArrayList<>();
            for (Chunk chunk : chunks) {
                for (String line : chunk.content.toString().split("\\r?\\n")) {
                    if (!line.isBlank()) {
                        result.add(renderChunk(line, chunk.headerPath, chunk.codeLanguage));
                    }
                }
            }
            return result;
        }

        List<String> result = new ArrayList<>();
        for (Chunk chunk : chunks) {
            result.add(renderChunk(
                    chunk.content.toString().trim(),
                    chunk.headerPath,
                    chunk.codeLanguage));
        }
        return result;
    }

    /**
     * Finalize and store a chunk if it has non-blank content.
     */
    private void completeChunk(List<Chunk> chunks, Chunk chunk, List<HeaderEntry> headerStack) {
        if (chunk.content.length() > 0 && !chunk.content.toString().isBlank()) {
            // Snapshot the current header hierarchy
            chunk.headerPath = new LinkedHashMap<>();
            for (HeaderEntry entry : headerStack) {
                chunk.headerPath.put("Header " + entry.depth, entry.text);
            }
            chunks.add(chunk);
        }
    }

    /**
     * Maintain the header stack: pop headers at the same or deeper level, then push the new one.
     */
    private void resolveHeaderStack(List<HeaderEntry> stack, int depth, String text) {
        stack.removeIf(entry -> entry.depth >= depth);
        stack.add(new HeaderEntry(depth, text));
    }

    /**
     * Render a chunk's content with its header hierarchy prefix.
     */
    private String renderChunk(String content, Map<String, String> headerPath, String codeLanguage) {
        if (this.mode == HeaderMode.RAW) {
            return content;
        }

        StringBuilder sb = new StringBuilder();
        if (headerPath != null && !headerPath.isEmpty()) {
            sb.append(String.join(" > ", headerPath.values()));
            sb.append("\n");
        }
        if (codeLanguage != null && !codeLanguage.isBlank()) {
            sb.append("Code: ").append(codeLanguage).append("\n");
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(content);
        return sb.toString();
    }

    // ----- Internal data classes -----

    private static class Chunk {
        StringBuilder content = new StringBuilder();
        Map<String, String> headerPath;
        String codeLanguage;
    }

    private static class HeaderEntry {
        int depth;
        String text;

        HeaderEntry(int depth, String text) {
            this.depth = depth;
            this.text = text;
        }
    }
}
