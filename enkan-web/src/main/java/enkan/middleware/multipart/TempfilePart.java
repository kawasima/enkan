package enkan.middleware.multipart;

import java.io.OutputStream;

/**
 * @author kawasima
 */
public class TempfilePart extends MimePart {
    public TempfilePart(OutputStream body, String head, String filename, String contentType, String name) {
        super(body, head, filename, contentType, name);
    }
}
