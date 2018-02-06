package enkan.system.repl.jshell;

import jdk.jshell.SourceCodeAnalysis;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Set;
import java.util.function.Predicate;

public class CompletionServer implements Runnable {
    private final ZMQ.Socket socket;
    private final SourceCodeAnalysis analysis;
    private final Set<String> commandNames;

    public CompletionServer(ZMQ.Socket socket, SourceCodeAnalysis analysis, Set<String> commandNames) {
        this.socket = socket;
        this.analysis = analysis;
        this.commandNames = commandNames;
    }
    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            ZMsg msg = ZMsg.recvMsg(socket);
            ZFrame clientAddress = msg.pop();
            String input = msg.popString();
            int cursor = Integer.parseInt(msg.popString());
            int[] anchor = {-1};

            ZMsg reply = new ZMsg();
            reply.add(clientAddress.duplicate());

            String trimmedCommand = input.trim();
            if (trimmedCommand.startsWith("/")) {
                if (!trimmedCommand.contains(" ")) {
                    Predicate<String> filter = trimmedCommand.equals("/") ?
                            n -> true : n -> n.startsWith(trimmedCommand.substring(1));

                    commandNames.stream()
                            .filter(filter)
                            .forEach(s -> reply.add("/" + s));
                    anchor[0] = 0;
                }
            } else {
                try {
                    analysis.completionSuggestions(input, cursor, anchor).stream()
                            .map(SourceCodeAnalysis.Suggestion::continuation)
                            .forEach(reply::add);
                    anchor[0] += cursor + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reply.send(socket, true);
        }
    }
}
