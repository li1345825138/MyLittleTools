import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.COSBase;

import java.io.IOException;
import java.util.List;

/**
 * Implement extract image from pdf file class
 * extend to override PDFStreamEngine to change operation
 * @author li1345825138
 * @date 2024/2/1
 */
public class ExtractImageFromPDF extends PDFStreamEngine {
    private int imageIndex = 1;

    @Override
    public void processOperator(String operation, List<COSBase> arguments) throws IOException {
        
    }

    public ExtractImageFromPDF() throws IOException {}

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }
}
