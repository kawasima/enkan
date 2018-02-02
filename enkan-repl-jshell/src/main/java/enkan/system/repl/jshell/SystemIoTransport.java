package enkan.system.repl.jshell;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import jline.internal.InputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;

import static enkan.system.ReplResponse.ResponseStatus.DONE;

public class SystemIoTransport implements Transport {
    public static final String CHUNK_DELEMETER = "-----------------END------------------";
    private BufferedReader reader;

    public SystemIoTransport() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void send(ReplResponse response) {
        String out = response.getOut();
        if (out != null) {
            System.out.println(out);
        }
        String err = response.getErr();
        if (err != null) {
            System.err.println(err);
        }

        if (response.getStatus().contains(DONE)) {
            System.out.println(CHUNK_DELEMETER);
        }
    }

    @Override
    public String recv(long timeout) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
