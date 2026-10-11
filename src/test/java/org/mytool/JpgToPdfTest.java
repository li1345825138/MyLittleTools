package org.mytool;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpgToPdfTest {

    @TempDir
    Path dir;

    @Test
    void writesOnePagePerImage() throws IOException {
        Path first = writeJpg("01.jpg", 20, 10);
        Path second = writeJpg("02.jpg", 20, 10);
        Path output = dir.resolve("out.pdf");

        JpgToPdf.Result result =
                new JpgToPdf().convert(List.of(first, second), output, new RecordingProgress());

        assertEquals(2, result.pageCount());
        assertTrue(Files.size(output) > 0);
        try (PDDocument document = Loader.loadPDF(output.toFile())) {
            assertEquals(2, document.getNumberOfPages());
        }
    }

    @Test
    void reportsThePathItWrote() throws IOException {
        Path output = dir.resolve("single.pdf");

        JpgToPdf.Result result = new JpgToPdf()
                .convert(List.of(writeJpg("only.jpg", 8, 8)), output, new RecordingProgress());

        assertEquals(output, result.output());
    }

    private Path writeJpg(String name, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        Path path = dir.resolve(name);
        ImageIO.write(image, "jpg", path.toFile());
        return path;
    }
}
