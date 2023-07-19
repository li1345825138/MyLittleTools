import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author li1345825138
 * @date 2023/7/18
 */
public class CommandProcessor {
    private String option;
    private String[] arguments;

    public CommandProcessor(String option) {
        this.option = option;
    }

    public void process(String[] arguments) throws NoSuchAlgorithmException, IOException {
        switch (this.option) {
            case "-hash" -> compareFileHash(arguments[1], arguments[2]);
            default -> {
                throw new IllegalArgumentException();
            }
        }
    }

    private String getHexFromHash(byte[] hashBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            int bt = b & 0xff;
            stringBuilder.append((bt < 16) ? 0 : Integer.toHexString(bt));
        }
        return stringBuilder.toString().toUpperCase();
    }

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
