package org.mytool;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Turns images into a single PDF, one image per page.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class JpgToPdf {

    record Result(Path output, int pageCount) {
    }

    Result convert(List<Path> images, Path output, Progress progress) throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.setResourceCache(new NoCachingResourceCache());
            for (Path image : images) {
                progress.reading(image);
                BufferedImage buffered = ImageIO.read(image.toFile());
                if (buffered == null) {
                    throw new IOException("Not a readable image: " + image);
                }
                PDPage page = new PDPage(new PDRectangle(buffered.getWidth(), buffered.getHeight()));
                document.addPage(page);
                PDImageXObject xObject = PDImageXObject.createFromFile(image.toString(), document);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.drawImage(xObject, 0, 0, buffered.getWidth(), buffered.getHeight());
                }
                progress.done();
            }
            progress.writing(output);
            document.save(output.toFile());
            progress.done();
            return new Result(output, images.size());
        }
    }

    /**
     * Stops PDFBox from caching the full-size images it embeds, which would
     * otherwise hold every page in memory at once.
     */
    private static final class NoCachingResourceCache extends DefaultResourceCache {

        @Override
        public void put(COSObject indirect, PDXObject xobject) {
            // do nothing
        }
    }
}
