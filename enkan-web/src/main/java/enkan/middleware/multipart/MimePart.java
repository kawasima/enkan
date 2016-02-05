package enkan.middleware.multipart;

import java.io.OutputStream;

/**
 * @author kawasima
 */
public abstract class MimePart {
    private String head;
    private OutputStream body;
    private String filename;
    private String contentType;
    private String name;

    public MimePart(OutputStream body, String head, String filename, String contentType, String name) {
        this.head = head;
        this.body = body;
        this.filename = filename;
        this.contentType = contentType;
        this.name = name;
    }

    public OutputStream getBody() {
        return body;
    }
}
