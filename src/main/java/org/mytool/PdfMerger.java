package org.mytool;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Merges several PDFs into one, in the order given.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class PdfMerger {

    record Result(Path output, int sourceCount) {
    }

    Result merge(List<Path> sources, Path output, Progress progress) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        for (Path source : sources) {
            progress.reading(source);
            merger.addSource(source.toFile());
            progress.done();
        }
        progress.writing(output);
        merger.setDestinationFileName(output.toString());
        merger.mergeDocuments(null);
        progress.done();
        return new Result(output, sources.size());
    }
}
