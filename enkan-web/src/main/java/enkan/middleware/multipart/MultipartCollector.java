package enkan.middleware.multipart;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

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

    public void onMimeHead(int mimeIndex, String head, String filename, String contentType, String name) throws IOException {
        if (filename != null) {
            File tempfile = tempfileFactory.apply(filename, contentType);
            openFiles += 1;
            mimeParts.add(new TempfilePart(tempfile, head, filename, contentType, name));
        } else {
            mimeParts.add(new BufferPart(head, filename, contentType, name));
        }

    }

    private void checkOpenFiles() {

    }

    public void onMimeBody(int mimeIndex, byte[] content) throws IOException {
        mimeParts.get(mimeIndex).getBody().write(content);
    }

    public void onMimeFinish(int mimeIndex) {
        mimeParts.get(mimeIndex).close();
    }

    public Stream<MimePart> stream() {
        return mimeParts.stream();
    }
}
