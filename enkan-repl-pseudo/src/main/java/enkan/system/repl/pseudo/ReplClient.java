package enkan.system.repl.pseudo;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.StringsCompleter;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class ReplClient {
    private Transport transport;

    ExecutorService receiverThread = Executors.newSingleThreadExecutor();

    public void start() throws Exception {
        Terminal terminal = TerminalFactory.create();
        terminal.init();
        ConsoleReader reader = new ConsoleReader();
        reader.setPrompt("> ");
        reader.addCompleter(new StringsCompleter("/connect", "/exit"));
        CandidateListCompletionHandler handler = new CandidateListCompletionHandler();
        reader.setCompletionHandler(handler);
        while (true) {
            String line = reader.readLine().trim();
            if (line.startsWith("/connect")) {
                String[] arguments = line.split("\\s+");
                if (arguments.length > 1) {
                    int port = Integer.parseInt(arguments[1]);
                    Socket socket = new Socket("localhost", port);
                    transport = new Transport(socket, msg -> {
                        reader.println(msg);
                        reader.flush();
                    });
                    receiverThread.execute(transport);
                } else {
                    reader.println("/connect [port]");
                }
            } else if (line.equals("/exit")){
                receiverThread.shutdown();
                reader.shutdown();
                return;
            } else {
                if (transport == null) {
                    reader.println("Unconnected to enkan system.");
                } else {
                    transport.send(line);
                }
            }
        }
    }
}
