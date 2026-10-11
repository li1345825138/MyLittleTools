package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageStackerTest {

    @TempDir
    Path dir;

    @Test
    void layoutStacksWithNoGapBetweenImages() {
        ImageStacker.Layout layout = ImageStacker.layout(List.of(
                new Dimension(10, 10), new Dimension(10, 10)));

        assertEquals(10, layout.width());
        assertEquals(20, layout.height());
        assertEquals(List.of(0, 10), layout.yOffsets());
    }

    @Test
    void layoutTakesTheWidestImageAndSumsEveryHeight() {
        ImageStacker.Layout layout = ImageStacker.layout(List.of(
                new Dimension(4, 5), new Dimension(7, 3), new Dimension(2, 6)));

        assertEquals(7, layout.width());
        assertEquals(14, layout.height());
        assertEquals(List.of(0, 5, 8), layout.yOffsets());
    }

    @Test
    void layoutOfOneImageIsThatImage() {
        ImageStacker.Layout layout = ImageStacker.layout(List.of(new Dimension(3, 9)));

        assertEquals(3, layout.width());
        assertEquals(9, layout.height());
        assertEquals(List.of(0), layout.yOffsets());
    }

    @Test
    void layoutOfNoImagesIsAnEmptyCanvas() {
        ImageStacker.Layout layout = ImageStacker.layout(List.of());

        assertEquals(0, layout.width());
        assertEquals(0, layout.height());
        assertTrue(layout.yOffsets().isEmpty());
    }

    /** Regression test: the last image used to lose its bottom row. */
    @Test
    void stackKeepsEveryImageWhole() throws IOException {
        Path red = writeJpg("01.jpg", 10, 10, Color.RED);
        Path blue = writeJpg("02.jpg", 10, 10, Color.BLUE);

        ImageStacker.Result result =
                new ImageStacker().stack(List.of(red, blue), dir.resolve("out.jpg"), new RecordingProgress());

        assertEquals(2, result.imageCount());
        assertEquals(10, result.width());
        assertEquals(20, result.height());

        BufferedImage stacked = ImageIO.read(result.output().toFile());
        assertEquals(20, stacked.getHeight());
        assertRedish(stacked.getRGB(5, 9), "last row of the first image");
        assertBlueish(stacked.getRGB(5, 10), "first row of the second image");
        assertBlueish(stacked.getRGB(5, 19), "last row of the second image, previously clipped");
    }

    private Path writeJpg(String name, int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        Path path = dir.resolve(name);
        ImageIO.write(image, "jpg", path.toFile());
        return path;
    }

    private static void assertRedish(int rgb, String where) {
        int red = (rgb >> 16) & 0xFF;
        int blue = rgb & 0xFF;
        assertTrue(red > 150 && blue < 100, "expected red at " + where + ", got " + Integer.toHexString(rgb));
    }

    private static void assertBlueish(int rgb, String where) {
        int red = (rgb >> 16) & 0xFF;
        int blue = rgb & 0xFF;
        assertTrue(blue > 150 && red < 100, "expected blue at " + where + ", got " + Integer.toHexString(rgb));
    }
}
