package kotowari.scaffold.task;

import net.unit8.amagicman.PathResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author kawasima
 */
public class PathResolverMock implements PathResolver {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Override
    public File project() {
        return new File("target");
    }

    @Override
    public InputStream templateAsStream(String path) {
        return null;
    }

    @Override
    public File destinationAsFile(String path) throws IOException {
        return new File("target");
    }

    @Override
    public OutputStream destinationAsStream(String path) throws IOException {
        return baos;
    }
}
