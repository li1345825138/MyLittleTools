package org.mytool;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebpConverterTest {

    @Test
    void replacesOnlyTheExtension() {
        assertEquals(Path.of("/tmp/webp/a.jpg"),
                WebpConverter.jpgNameFor(Path.of("/tmp/webp/a.webp")));
    }

    @Test
    void leavesADirectoryThatMerelyContainsWebpAlone() {
        assertEquals(Path.of("/photos/webp/shots/a.jpg"),
                WebpConverter.jpgNameFor(Path.of("/photos/webp/shots/a.webp")));

        assertEquals(Path.of("webp/target/a.jpg"),
                WebpConverter.jpgNameFor(Path.of("webp/target/a.webp")));
    }

    @Test
    void theOutputStaysBesideItsSource() {
        assertEquals(Path.of("out/a.jpg"),
                WebpConverter.jpgNameFor(Path.of("out/a.WEBP")));
    }
}
