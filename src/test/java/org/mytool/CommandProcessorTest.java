package org.mytool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    @TempDir
    Path dir;

    @Test
    void anEmptyDirectoryIsAUsageErrorForEveryDirectoryTool() {
        String[] arguments = {dir.toString(), dir.resolve("out").toString()};

        for (Option option : List.of(Option.WEBP_TO_JPG, Option.JPGS_TO_PDF,
                Option.MERGE_PDFS, Option.MERGE_MULTI_JPGS)) {
            CommandProcessor processor = new CommandProcessor(option, new RecordingProgress());
            assertThrows(UsageException.class, () -> processor.process(arguments),
                    option + " should report an empty directory as a usage error");
        }
    }

    @Test
    void aPathThatIsNotADirectoryIsAUsageError() {
        CommandProcessor processor =
                new CommandProcessor(Option.MERGE_MULTI_JPGS, new RecordingProgress());

        assertThrows(UsageException.class, () -> processor.process(
                new String[]{dir.resolve("missing").toString(), "out.jpg"}));
    }

    @Test
    void mergeMultiJpgsWritesTheStackedImage() throws Exception {
        writeJpg("01.jpg", 10, 10);
        writeJpg("02.jpg", 10, 10);
        Path output = dir.resolve("out.jpg");

        new CommandProcessor(Option.MERGE_MULTI_JPGS, new RecordingProgress())
                .process(new String[]{dir.toString(), output.toString()});

        assertTrue(Files.exists(output));
        BufferedImage stacked = ImageIO.read(output.toFile());
        assertEquals(20, stacked.getHeight(), "two 10px images stacked with no gap");
    }

    @Test
    void jpgsToPdfWritesOnePagePerImage() throws Exception {
        writeJpg("01.jpg", 10, 10);
        writeJpg("02.jpg", 10, 10);
        Path output = dir.resolve("out.pdf");

        new CommandProcessor(Option.JPGS_TO_PDF, new RecordingProgress())
                .process(new String[]{dir.toString(), output.toString()});

        assertTrue(Files.exists(output));
    }

    @Test
    void hashCompareReportsBothDigestsAndTheirVerdict() throws Exception {
        Path a = Files.writeString(dir.resolve("a.txt"), "AAA\n");
        Path b = Files.writeString(dir.resolve("b.txt"), "BBB\n");

        String output = captureStdout(() -> new CommandProcessor(Option.HASH_COMPARE, new RecordingProgress())
                .process(new String[]{a.toString(), b.toString()}));

        assertTrue(output.contains("FILE 1: " + a), output);
        assertTrue(output.contains("FILE 2: " + b), output);
        assertTrue(output.contains("File has been modify"), output);
        assertTrue(output.matches("(?s).*Hash: [0-9A-F]{64}.*"), output);
    }

    @Test
    void aBlankOutputNameIsAUsageError() throws Exception {
        writeJpg("01.jpg", 4, 4);
        CommandProcessor processor =
                new CommandProcessor(Option.MERGE_MULTI_JPGS, new RecordingProgress());

        assertThrows(UsageException.class, () -> processor.process(
                new String[]{dir.toString(), "  "}));
    }

    @Test
    void aNonNumericPasswordLengthIsAUsageError() {
        CommandProcessor processor =
                new CommandProcessor(Option.RAND_PASS, new RecordingProgress());

        assertThrows(UsageException.class, () -> processor.process(new String[]{"abc"}));
    }

    private static String captureStdout(ThrowingRunnable action) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void writeJpg(String name, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        ImageIO.write(image, "jpg", dir.resolve(name).toFile());
    }
}
