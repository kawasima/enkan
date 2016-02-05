package enkan.middleware.multipart;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author kawasima
 */
public class MultipartCollector {
    private BiFunction<String, String, File> tempfileFactory;
    private List<MimePart> mimeParts = new ArrayList<>();
    private int openFiles = 0;

    public MultipartCollector(BiFunction<String, String, File> tempfileFactory) {
        this.tempfileFactory = tempfileFactory;
    }

    public void onMimeHead(int mimeIndex, String head, String filename, String contentType, String name) throws FileNotFoundException {
        if (filename != null) {
            File tempfile = tempfileFactory.apply(filename, contentType);
            OutputStream body = new BufferedOutputStream(new FileOutputStream(tempfile));
            openFiles += 1;
            mimeParts.add(new TempfilePart(body, head, filename, contentType, name));
        } else {
            // TODO Buffer
        }

    }

    private void checkOpenFiles() {

    }

    public void onMimeBody(int mimeIndex, byte[] content) throws IOException {
        mimeParts.get(mimeIndex).getBody().write(content);
    }
}
