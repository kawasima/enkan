package enkan.middleware.multipart;

import enkan.collection.Multimap;

import java.io.File;
import java.util.List;

/**
 * @author kawasima
 */
public class MultipartInfo {
    private Multimap<String, String> params;
    private List<File> tmpFiles;

    public MultipartInfo(Multimap<String, String> params, List<File> tmpFiles) {
        this.params = params;
        this.tmpFiles = tmpFiles;
    }

    public Multimap<String, String> getParams() {
        return params;
    }

    public List<File> getTmpFiles() {
        return tmpFiles;
    }
}
