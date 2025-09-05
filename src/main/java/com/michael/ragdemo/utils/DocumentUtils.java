package com.michael.ragdemo.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DocumentUtils {
    private static final int CHUNK_SIZE = 1000;

    public static List<Document> readCsvAsDocuments(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : csvParser) {
                StringBuilder contentBuilder = new StringBuilder();

                csvParser.getHeaderNames().forEach(header -> {
                    String value = record.get(header);
                    if (value != null && !value.trim().isEmpty()) {
                        contentBuilder.append(header).append(": ").append(value.trim()).append("\n");
                    }
                });

                String content = contentBuilder.toString().trim();
                if (!content.isEmpty()) {
                    documents.add(new Document(content));
                }
            }
        }

        return documents;
    }

    public static List<String> splitIntoChunks(String text, int maxTokens) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("(?<=\\.|\\n\\n)");

        StringBuilder currentChunk = new StringBuilder();
        int currentTokenCount = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            int paragraphTokens = estimateTokens(paragraph);

            if (paragraphTokens > maxTokens) {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentTokenCount = 0;
                }
                String[] sentences = paragraph.split("(?<=\\.|\\!|\\?)\\s+");
                for (String sentence : sentences) {
                    int sentenceTokens = estimateTokens(sentence);
                    if (currentTokenCount + sentenceTokens > maxTokens) {
                        if (!currentChunk.isEmpty()) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk = new StringBuilder();
                            currentTokenCount = 0;
                        }
                    }
                    currentChunk.append(sentence).append(" ");
                    currentTokenCount += sentenceTokens;
                }
            } else if (currentTokenCount + paragraphTokens > maxTokens) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();

                currentChunk.append(paragraph).append(" ");
                currentTokenCount = paragraphTokens;
            } else {
                currentChunk.append(paragraph).append(" ");
                currentTokenCount += paragraphTokens;
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private int estimateTokens(String text) {
        int wordCount = text.split("\\s+").length;

        return (int) Math.ceil(wordCount * 1.3) + 5;
    }
}
