package org.mytool;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfMergerTest {

    @TempDir
    Path dir;

    @Test
    void keepsEveryPageOfEverySource() throws IOException {
        Path one = pdfWithPages("one.pdf", 1);
        Path two = pdfWithPages("two.pdf", 2);
        Path output = dir.resolve("merged.pdf");

        PdfMerger.Result result =
                new PdfMerger().merge(List.of(one, two), output, new RecordingProgress());

        assertEquals(2, result.sourceCount());
        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    @Test
    void mergesInTheOrderGiven() throws IOException {
        Path output = dir.resolve("merged.pdf");

        PdfMerger.Result result = new PdfMerger().merge(
                List.of(pdfWithPages("a.pdf", 2), pdfWithPages("b.pdf", 3)),
                output, new RecordingProgress());

        assertEquals(output, result.output());
        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            assertEquals(5, document.getNumberOfPages());
        }
    }

    private Path pdfWithPages(String name, int pages) throws IOException {
        Path path = dir.resolve(name);
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(path.toFile());
        }
        return path;
    }
}
