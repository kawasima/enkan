package enkan.middleware.multipart;

import enkan.collection.Parameters;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author kawasima
 */
public abstract class MimePart {
    protected String head;
    private OutputStream body;
    protected String filename;
    protected String contentType;
    protected String name;

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

    public abstract Parameters getData();
    public abstract void write(byte[] buf) throws IOException;
    public abstract void close();
}
