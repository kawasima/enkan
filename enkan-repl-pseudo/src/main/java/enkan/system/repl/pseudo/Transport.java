package enkan.system.repl.pseudo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author kawasima
 */
public class Transport implements Runnable {
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private CommandProcessor commandProcessor;

    public Transport(Socket socket) throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
        this.socket = socket;
    }

    public Transport(Socket socket, CommandProcessor commandProcessor) throws IOException {
        this(socket);
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String msg = recv();
                if (msg == null) return;
                commandProcessor.execute(msg);
            } catch (IOException ex) {
                //ignore
            }
        }
    }

    public void send(String message) throws IOException {
        out.write(message.getBytes("UTF-8"));
        out.flush();
    }

    public String recv() throws IOException {
        byte[] buf = new byte[1024];
        int readed = in.read(buf);
        if (readed < 0) return null;

        return new String(buf, 0, readed, StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    public interface CommandProcessor {
        void execute(String cmd) throws IOException;
    }

}
