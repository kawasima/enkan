package enkan.system.repl.serdes;

import org.fressian.FressianReader;
import org.fressian.FressianWriter;
import org.fressian.handlers.ReadHandler;
import org.fressian.handlers.WriteHandler;
import org.fressian.impl.Handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Fressian {
    private final Map<Object, ReadHandler> readHandlers;
    private final Map<String, WriteHandler> writeHandlers;

    public Fressian() {
        readHandlers = new HashMap<>();
        writeHandlers = new HashMap<>();
    }

    @SuppressWarnings("unckecked")
    public <T> T read(byte[] blob, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            FressianReader reader = new FressianReader(bais, readHandlers::get)) {
            Object obj = reader.readObject();
            if (obj != null && !clazz.isInstance(obj)) {
                throw new IllegalArgumentException(blob + " is not instance of " + clazz);
            }
            return (T) obj;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] write(Object obj) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FressianWriter writer = new FressianWriter(baos, Handlers.customWriteHandlers(key -> {
                WriteHandler h = writeHandlers.get(key.getSimpleName());
                return h != null ? Collections.singletonMap(key.getSimpleName(), h) : null;
            }))) {
            writer.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void putReadHandler(Class<?> key, ReadHandler readHandler) {
        readHandlers.put(key.getSimpleName(), readHandler);
    }

    public void putWriteHandler(Class<?> clazz, WriteHandler writeHandler) {
        writeHandlers.put(clazz.getSimpleName(), writeHandler);
    }

}
