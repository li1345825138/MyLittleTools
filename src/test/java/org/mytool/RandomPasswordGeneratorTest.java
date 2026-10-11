package org.mytool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomPasswordGeneratorTest {

    @Test
    void clampsTheLengthUpToTheDocumentedMinimum() {
        assertEquals(8, RandomPasswordGenerator.clampLength(0));
        assertEquals(8, RandomPasswordGenerator.clampLength(1));
        assertEquals(8, RandomPasswordGenerator.clampLength(-5));
        assertEquals(8, RandomPasswordGenerator.clampLength(8));
    }

    @Test
    void clampsTheLengthDownToTheDocumentedMaximum() {
        assertEquals(1024, RandomPasswordGenerator.clampLength(1024));
        assertEquals(1024, RandomPasswordGenerator.clampLength(5000));
    }

    @Test
    void leavesAReasonableLengthAlone() {
        assertEquals(12, RandomPasswordGenerator.clampLength(12));
        assertEquals(64, RandomPasswordGenerator.clampLength(64));
    }
}
