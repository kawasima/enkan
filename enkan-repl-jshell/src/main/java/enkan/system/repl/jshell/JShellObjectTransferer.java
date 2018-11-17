package enkan.system.repl.jshell;

import java.io.*;
import java.util.Base64;

public class JShellObjectTransferer {
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    @SuppressWarnings("unchecked")
    public static <T> T readFromBase64(String base64, Class<T> clazz) throws IOException, ClassNotFoundException {
        byte[] blob = BASE64_DECODER.decode(base64);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blob))) {
            Object obj = ois.readObject();
            if (!clazz.isInstance(obj)) {
                throw new IllegalArgumentException("Object is not a instance of " + clazz);
            }
            return (T) obj;
        }
    }

    public static String writeToBase64(Object obj) throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return BASE64_ENCODER.encodeToString(baos.toByteArray());
        }
    }
}
