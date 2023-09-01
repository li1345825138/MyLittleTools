import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;

/**
 * @author li1345825138
 * @date 2023/9/1
 */
public class DefaultResourceCacheWrapper extends DefaultResourceCache {
    public DefaultResourceCacheWrapper(){
    }

    @Override
    public void put(COSObject indirect, PDXObject xobject) {
        // do nothing
    }
}
