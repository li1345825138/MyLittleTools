import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class CommandProcessor {
    private final String option;

    public CommandProcessor(String option) {
        this.option = option;
    }

    /**
     * Process the command send in
     * @param arguments list of arguments
     */
    public void process(String[] arguments) throws Exception {
        OptionHashCodeEnum hashVal = OptionHashCodeEnum.valueOf(this.option.hashCode());
        switch (Objects.requireNonNull(hashVal)) {
            // compare two file hash value
            case HASH_COMPARE -> compareFileHash(arguments[1], arguments[2]);
            case WEBP_TO_JPG -> {
                List<File> imageList = getFilesFrom(arguments[1], ".webp");
                convertWebpToJPG(imageList);
            }
            // convert multiple jpg images into single pdf
            case JPGS_TO_PDF -> {
                List<File> imageList = getFilesFrom(arguments[1], ".jpg");
                convertJPGToPDF(imageList, arguments[2]);
            }
            // print help message
            case HELP_MSG -> Main.printHelp();
            // merge pdfs
            case MERGE_PDFS -> {
                List<File> pdfList = getFilesFrom(arguments[1], ".pdf");
                mergePDF(pdfList, arguments[2]);
            }
            // merge images
            case MERGE_MULTI_JPGS -> {
                List<File> imagesList = getFilesFrom(arguments[1], ".jpg");
                mergeMultiImages(imagesList, arguments[2]);
            }
            // extract images from pdf
            case EXTRACT_IMAGES -> {

            }
            // random password
            case RAND_PASS -> {
                int passLength = Integer.parseInt(arguments[1]);
                try (RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator(passLength)) {
                    String randomPass = passwordGenerator.generateRandomPassword();
                    System.out.printf("Random Password: %s\n", randomPass);
                }
            }
            // view sql
            case VIEW_SQL -> {
                try (SqliteViewer sqliteViewer = new SqliteViewer(arguments[1])) {
                    sqliteViewer.viewSqliteDatabase();
                }
            }
            default -> throw new IllegalArgumentException("Unknown option");
        }
    }

    /**
     * Extract all the images that merge in pdf
     * @param filename pdf file name
     */
    private void extractImagesFromPDF(String filename) {

    }

    /**
     * Merge multiple Images into one single JPG format image
     * @param imagesList the list of images
     * @param finalName final image save name
     */
    private void mergeMultiImages(List<File> imagesList, String finalName) throws IOException {
        if (imagesList == null || imagesList.isEmpty()) return;

        int width = 0;
        int height = 0;
        for (File currImage : imagesList) {
            BufferedImage temp = ImageIO.read(currImage);
            if (temp.getWidth() > width) width = temp.getWidth();
            height += temp.getHeight();
        }

        BufferedImage mergeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = mergeImage.createGraphics();
        try {
            int currentYPos = 0;
            for (File currImage : imagesList) {
                BufferedImage temp = ImageIO.read(currImage);
                g2d.drawImage(temp, 0, currentYPos, null);
                currentYPos += temp.getHeight() + 1;
            }
            File mergeFile = new File(finalName);
            ImageIO.write(mergeImage, "jpg", mergeFile);
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Merge list of pdf format file into one single pdf
     * @param pdfList list of pdf files
     * @param outputName final output name
     */
    private void mergePDF(List<File> pdfList, String outputName) throws IOException {
        if (pdfList == null || pdfList.isEmpty()) return;
        if (outputName == null || outputName.isEmpty()) outputName = "output.pdf";
        PDFMergerUtility mergePDF = new PDFMergerUtility();
        for (File pdfFile : pdfList) {
            System.out.printf("[+] Reading: %s...", pdfFile.getPath());
            mergePDF.addSource(pdfFile);
            System.out.println("Done!");
        }
        mergePDF.setDestinationFileName(outputName);
        System.out.printf("[+] Writing output: %s...", outputName);
        mergePDF.mergeDocuments(null);
        System.out.println("Done!");
    }


    /**
     * convert webp images into jpg format
     * @param webpList list of webp format images
     */
    private void convertWebpToJPG(List<File> webpList) throws IOException, NullPointerException {
        if (webpList == null || webpList.isEmpty()) return;
        for (File webpImage : webpList) {
            System.out.printf("[+] Reading: %s...\n", webpImage.getPath());
            BufferedImage image = ImageIO.read(webpImage);
            if (image == null) throw new NullPointerException("Error: ImageIO read null image");
            String newImageName = webpImage.getPath().replace("webp", "jpg");
            File outputJPG = new File(newImageName);
            System.out.printf("[+] Writing: %s...", newImageName);
            ImageIO.write(image, "jpg", outputJPG);
            System.out.println("Done!");
        }
    }


    /**
     * Convert multiple JPG format images into a single PDF file
     * @param imageList a list of jpg files
     * @param saveName final output pdf save name
     */
    private void convertJPGToPDF(List<File> imageList, String saveName) throws IOException {
        if (imageList == null || imageList.isEmpty()) return;
        if (saveName == null || saveName.isEmpty()) saveName = "output.pdf";
        try (PDDocument document = new PDDocument()) {
            document.setResourceCache(new DefaultResourceCacheWrapper());
            for (File imageFile : imageList) {
                System.out.printf("[+] Reading: %s...", imageFile.getPath());
                BufferedImage image = ImageIO.read(imageFile);
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);
                PDImageXObject imageXObject = PDImageXObject.createFromFile(imageFile.getPath(), document);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(imageXObject, 0, 0, image.getWidth(), image.getHeight());
                }
                System.out.println("Done!");
            }
            System.out.printf("[+] Writing output: %s...", saveName);
            document.save(saveName);
            System.out.println("Done!");
        }
    }


    /**
     * Get all the files from given directory and file extension
     * @param path where is file parent folder locate
     * @param fileExt what is file extension for filter
     * @return list of specific file.
     */
    private List<File> getFilesFrom(String path, String fileExt) {
        File dir = new File(path);
        if (!dir.isDirectory()) return null;
        String lowerCaseFileExt = fileExt.toLowerCase();
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(lowerCaseFileExt));

        // Sort the files by ascending order
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
        }
        return (files != null) ? new LinkedList<>(Arrays.asList(files)) : null;
    }

    /**
     * Turn Hash bytes array into hex string
     * @param hashBytes hash bytes array
     * @return hex string
     */
    private String getHexFromHash(byte[] hashBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            int bt = b & 0xff;
            stringBuilder.append((bt < 16) ? 0 : Integer.toHexString(bt));
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * Compare two file hash and show there result
     * @param file1 original file path
     * @param file2 compare file path
     */
    private void compareFileHash(String file1, String file2) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] file1Bytes = Files.readAllBytes(Paths.get(file1));
        byte[] file2Bytes = Files.readAllBytes(Paths.get(file2));
        digest.update(file1Bytes);
        String file1Hash = getHexFromHash(digest.digest());
        digest.update(file2Bytes);
        String file2Hash = getHexFromHash(digest.digest());
        System.out.printf("FILE 1: %s  Hash: %s\n", file1, file1Hash);
        System.out.printf("FILE 2: %s  Hash: %s\n", file2, file2Hash);
        System.out.printf("Hash Result: %s\n", (file1Hash.equals(file2Hash)) ? "Hash Correct" : "File has been modify");
    }
}
