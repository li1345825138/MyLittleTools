import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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
    public void process(String[] arguments) throws NoSuchAlgorithmException, IOException, IllegalArgumentException {
        switch (this.option) {
            // compare two file hash value
            case "-hash" -> compareFileHash(arguments[1], arguments[2]);
            case "-w" -> {
                LinkedList<File> imageList = getFilesFrom(arguments[1], ".webp");
                convertWebpToJPG(imageList);
            }
            // convert multiple jpg images into single pdf
            case "-p" -> {
                LinkedList<File> imageList = getFilesFrom(arguments[1], ".jpg");
                convertJPGToPDF(imageList, arguments[2]);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    /**
     * convert webp images into jpg format
     * @param webpList list of webp format images
     */
    private void convertWebpToJPG(LinkedList<File> webpList) throws IOException, NullPointerException {
        if (webpList == null) return;
        while (!webpList.isEmpty()) {
            File webpImage = webpList.poll();
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
    private void convertJPGToPDF(LinkedList<File> imageList, String saveName) throws IOException {
        if (imageList == null || imageList.isEmpty()) return;
        if (saveName == null || saveName.isEmpty()) saveName = "output.pdf";
        try (PDDocument document = new PDDocument()) {
            while (!imageList.isEmpty()) {
                File imageFile = imageList.poll();
                System.out.printf("[+] Reading %s...", imageFile.getPath());
                PDPage page = new PDPage();
                document.addPage(page);
                PDImageXObject imageXObject = PDImageXObject.createFromFile(imageFile.getPath(), document);

                // current image width and height
                float imageWidth = imageXObject.getWidth();
                float imageHeight = imageXObject.getHeight();

                // calculate image scale to fit
                float scale = Math.min(page.getMediaBox().getWidth() / imageWidth, page.getMediaBox().getHeight() / imageHeight);
                imageWidth *= scale;
                imageHeight *= scale;

                // make image in pdf page middle
                float offsetX = (page.getMediaBox().getWidth() - imageWidth) * 0.5f;
                float offsetY = (page.getMediaBox().getHeight() - imageHeight) * 0.5f;

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(imageXObject, offsetX, offsetY, imageWidth, imageHeight);
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
    private LinkedList<File> getFilesFrom(String path, String fileExt) {
        File dir = new File(path);
        if (!dir.isDirectory()) return null;
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(fileExt));

        // if file extension cannot be done by lowercase try uppercase
        if (files == null) {
            files = dir.listFiles(((dir1, name) -> name.endsWith(fileExt.toUpperCase())));
        }

        return (files != null) ? new LinkedList<>(Arrays.stream(files).toList()) : null;
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
