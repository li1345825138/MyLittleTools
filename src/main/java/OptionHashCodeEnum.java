/**
 * Enumerate all the operation for the command
 * @author li1345825138
 * @date 2023/10/31
 */
enum OptionHashCodeEnum {

    // -hash: compare two file hash value
    HASH_COMPARE(44753595),

    // -w: convert WEBP image to JPG format
    WEBP_TO_JPG(1514),

    // -p: Convert JPG files into one single PDF file
    JPGS_TO_PDF(1507),

    // -h: help message
    HELP_MSG(1499),

    // -m: Merge Multi PDFs into single PDF
    MERGE_PDFS(1504),

    // -mi: Merge multi JPGs into single JPG
    MERGE_MULTI_JPGS(46729),

    // -extractImages
    EXTRACT_IMAGES(2120062796),

    // -randpass
    RAND_PASS(519346403);

    /**
     * Enum custom value Con-structor
     * @param value custom value pair with enum member
     */
    OptionHashCodeEnum(int value) {}

    /**
     * Turn int type value into OptionHashCodeEnum type
     * @param value int type value to convert
     * @return OptionHashCodeEnum type of int value that pair with it.
     */
    public static OptionHashCodeEnum valueOf(int value) {
        return switch (value) {
            case 44753595 -> HASH_COMPARE;
            case 1514 -> WEBP_TO_JPG;
            case 1507 -> JPGS_TO_PDF;
            case 1499 -> HELP_MSG;
            case 1504 -> MERGE_PDFS;
            case 46729 -> MERGE_MULTI_JPGS;
            case 2120062796 -> EXTRACT_IMAGES;
            case 519346403 -> RAND_PASS;
            default -> null;
        };
    }
}
