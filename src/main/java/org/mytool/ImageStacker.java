package org.mytool;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stacks images vertically into one taller JPG, with no gap between them.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class ImageStacker {

    /** Where each image sits on the output canvas. */
    record Layout(int width, int height, List<Integer> yOffsets) {
    }

    record Result(Path output, int imageCount, int width, int height) {
    }

    /**
     * Works out the canvas that exactly fits the given images stacked with no
     * gap, so the last one is never clipped.
     *
     * @param sizes the size of each image, top to bottom
     * @return the canvas size and the y offset of each image
     */
    static Layout layout(List<Dimension> sizes) {
        int width = 0;
        int height = 0;
        List<Integer> yOffsets = new ArrayList<>(sizes.size());
        for (Dimension size : sizes) {
            yOffsets.add(height);
            width = Math.max(width, size.width);
            height += size.height;
        }
        return new Layout(width, height, List.copyOf(yOffsets));
    }

    Result stack(List<Path> images, Path output, Progress progress) throws IOException {
        List<BufferedImage> loaded = loadEach(images, progress);

        Layout layout = layout(loaded.stream()
                .map(image -> new Dimension(image.getWidth(), image.getHeight()))
                .toList());

        BufferedImage canvas = new BufferedImage(layout.width(), layout.height(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            for (int i = 0; i < loaded.size(); i++) {
                graphics.drawImage(loaded.get(i), 0, layout.yOffsets().get(i), null);
            }
        } finally {
            graphics.dispose();
        }

        progress.writing(output);
        if (!ImageIO.write(canvas, "jpg", output.toFile())) {
            throw new IOException("No JPG writer available for " + output);
        }
        progress.done();

        return new Result(output, loaded.size(), layout.width(), layout.height());
    }

    private static List<BufferedImage> loadEach(List<Path> images, Progress progress) throws IOException {
        List<BufferedImage> loaded = new ArrayList<>(images.size());
        for (Path image : images) {
            progress.reading(image);
            BufferedImage buffered = ImageIO.read(image.toFile());
            if (buffered == null) {
                throw new IOException("Not a readable image: " + image);
            }
            loaded.add(buffered);
            progress.done();
        }
        return loaded;
    }
}
