package org.mytool;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A Progress that records the steps instead of printing them, so a test can
 * assert what a Tool reported without capturing standard output.
 */
final class RecordingProgress implements Progress {

    final List<String> steps = new ArrayList<>();

    @Override
    public void reading(Path file) {
        steps.add("reading " + file.getFileName());
    }

    @Override
    public void writing(Path file) {
        steps.add("writing " + file.getFileName());
    }

    @Override
    public void done() {
        steps.add("done");
    }
}
