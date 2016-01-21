package kotowari.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kawasima
 */
public class LazyRenderInputStream extends InputStream {
    private LazyRenderer renderer;
    private InputStream in;

    public LazyRenderInputStream(LazyRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public int read() throws IOException {
        if (in == null) {
            in = renderer.render();
        }
        return in.read();
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}
