import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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
    public void process(String[] arguments) throws NoSuchAlgorithmException, IOException {
        switch (this.option) {
            // compare two file hash value
            case "-hash" -> compareFileHash(arguments[1], arguments[2]);

            // turn multiple webp images into jpg images format
            case "-w" -> {
                LinkedList<File> imageFileList = getFilesFrom(arguments[1], ".webp");
                convertWEBPToJPG(imageFileList);
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
     * Convert multiple JPG format images into a single PDF file
     * @param imageList a list of jpg files
     * @param saveName final output pdf save name
     */
    private void convertJPGToPDF(LinkedList<File> imageList, String saveName) throws IOException {
        if (imageList == null || imageList.isEmpty()) return;
        if (saveName == null || saveName.isEmpty()) saveName = "output.pdf";
        PDDocument document = new PDDocument();
        PDPageContentStream contentStream = null;
        try {
            while (!imageList.isEmpty()) {
                File imageFile = imageList.pop();
                System.out.printf("[+] Reading %s...", imageFile.getPath());
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                PDImageXObject imageXObject = PDImageXObject.createFromFile(imageFile.getPath(), document);
                contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(imageXObject, 0,0,PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
                System.out.println("Done!");
            }
            System.out.printf("Writing output: %s\n", saveName);
            document.save(saveName);
            System.out.println("Done!");
        } finally {
            if(contentStream != null) contentStream.close();
            document.close();
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
        return (files != null) ? new LinkedList<>(Arrays.stream(files).toList()) : null;
    }

    /**
     * Accept a list of webp files and turn each of webp image into jpg format
     * @param imageList webp format image file list
     */
    private void convertWEBPToJPG(LinkedList<File> imageList) throws IOException {
        if (imageList == null) return;
        String newFileName = null;
        int subIndex = -1;
        while (!imageList.isEmpty()) {
            File currImage = imageList.pop();
            System.out.printf("[+] Reading: %s...\n", currImage.getPath());
            subIndex = currImage.getPath().lastIndexOf('.');
            newFileName = currImage.getPath().substring(0, subIndex) + ".jpg";
            Thumbnails.of(currImage).outputFormat("jpg").toFile(newFileName);
            System.out.printf("[+] Writing: %s...\n", newFileName);
        }
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
