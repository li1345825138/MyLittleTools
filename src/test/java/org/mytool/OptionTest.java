package org.mytool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionTest {

    @Test
    void resolvesEveryDocumentedName() {
        assertEquals(Option.HASH_COMPARE, Option.resolve("-hash").orElseThrow());
        assertEquals(Option.WEBP_TO_JPG, Option.resolve("-w").orElseThrow());
        assertEquals(Option.JPGS_TO_PDF, Option.resolve("-p").orElseThrow());
        assertEquals(Option.HELP_MSG, Option.resolve("-h").orElseThrow());
        assertEquals(Option.MERGE_PDFS, Option.resolve("-m").orElseThrow());
        assertEquals(Option.MERGE_MULTI_JPGS, Option.resolve("-mi").orElseThrow());
        assertEquals(Option.RAND_PASS, Option.resolve("-randpass").orElseThrow());
        assertEquals(Option.VIEW_SQL, Option.resolve("-viewsql").orElseThrow());
    }

    @Test
    void matchingIsCaseSensitive() {
        assertTrue(Option.resolve("-HASH").isEmpty());
        assertTrue(Option.resolve("-Mi").isEmpty());
    }

    @Test
    void resolvesNothingForUnknownOrMissingNames() {
        assertTrue(Option.resolve("-nope").isEmpty());
        assertTrue(Option.resolve("").isEmpty());
        assertTrue(Option.resolve(null).isEmpty());
        assertTrue(Option.resolve("hash").isEmpty(), "the leading dash is part of the name");
    }

    @Test
    void theRemovedExtractImagesNameNoLongerResolves() {
        assertTrue(Option.resolve("-extractImages").isEmpty());
    }

    @Test
    void everyOptionDeclaresHowManyArgumentsItExpects() {
        assertEquals(0, Option.HELP_MSG.argCount());
        assertEquals(1, Option.WEBP_TO_JPG.argCount());
        assertEquals(1, Option.RAND_PASS.argCount());
        assertEquals(1, Option.VIEW_SQL.argCount());
        assertEquals(2, Option.HASH_COMPARE.argCount());
        assertEquals(2, Option.JPGS_TO_PDF.argCount());
        assertEquals(2, Option.MERGE_PDFS.argCount());
        assertEquals(2, Option.MERGE_MULTI_JPGS.argCount());
    }
}
