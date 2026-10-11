package org.mytool;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts WEBP images to JPG, leaving each output beside its source.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class WebpConverter {

    record Result(List<Path> outputs) {
    }

    Result convert(List<Path> webps, Progress progress) throws IOException {
        List<Path> outputs = new ArrayList<>(webps.size());
        for (Path webp : webps) {
            progress.reading(webp);
            BufferedImage image = ImageIO.read(webp.toFile());
            if (image == null) {
                throw new IOException("Not a readable image: " + webp);
            }
            Path jpg = jpgNameFor(webp);
            progress.writing(jpg);
            if (!ImageIO.write(flattenToRgb(image), "jpg", jpg.toFile())) {
                throw new IOException("No JPG writer available for " + jpg);
            }
            progress.done();
            outputs.add(jpg);
        }
        return new Result(List.copyOf(outputs));
    }

    /**
     * JPEG carries no alpha channel. A transparent image is composited onto
     * white first, because ImageIO refuses to write one that still has alpha.
     */
    private static BufferedImage flattenToRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rgb;
    }

    /**
     * Replaces only the file name's extension, so a directory that happens to
     * contain "webp" in its name is left alone.
     */
    static Path jpgNameFor(Path webp) {
        String name = webp.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String stem = (dot > 0) ? name.substring(0, dot) : name;
        return webp.resolveSibling(stem + ".jpg");
    }
}
