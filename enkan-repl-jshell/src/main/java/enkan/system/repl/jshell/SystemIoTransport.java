package enkan.system.repl.jshell;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import jline.internal.InputStreamReader;

import static enkan.system.ReplResponse.ResponseStatus.DONE;

public class SystemIoTransport implements Transport {
    public static final String CHUNK_DELIMITER = "-----------------END------------------";
    private final BufferedReader reader;

    public SystemIoTransport() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void send(ReplResponse response) {
        synchronized (this) {
            String out = response.getOut();
            if (out != null) {
                System.out.println(out);
                System.out.flush();
            }
            String err = response.getErr();
            if (err != null) {
                System.err.println(err);
                System.err.flush();
            }

            if (response.getStatus().contains(DONE)) {
                System.out.println(CHUNK_DELIMITER);
                System.out.flush();
            }
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
