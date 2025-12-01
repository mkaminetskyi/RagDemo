package com.michael.tabularDataSearch.utils;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentUtilsTest {

    @Test
    void splitsLongParagraphsIntoTokenBoundedChunks() {
        String content = """
                Paragraph one has a moderate amount of content to seed the initial chunk.\n\n
                The second paragraph is intentionally verbose and contains multiple sentences. It should cause the chunker to
                spill into additional chunks once the estimated token ceiling is reached. Token estimates are approximate, but
                the algorithm should avoid returning any chunk that is empty or untrimmed. Another sentence keeps the count
                growing so the test has something to assert against. This should be enough to cross the threshold.
                """;

        List<String> chunks = DocumentUtils.splitIntoChunks(content, 40);

        assertThat(chunks)
                .hasSizeGreaterThan(1)
                .allSatisfy(chunk -> assertThat(chunk).isNotBlank());
    }

    @Test
    void readsCsvIntoDocuments() throws IOException {
        String csv = "name,price\nWidget,10\nGadget,15";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        List<Document> documents = DocumentUtils.readCsvAsDocuments(file);

        assertThat(documents)
                .hasSize(2)
                .allSatisfy(doc -> assertThat(doc.getText())
                        .contains("name:")
                        .contains("price:"));
    }
}
