package kotowari.scaffold.command;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.listener.TaskListener;

import java.util.Locale;

/**
 * Logs for executing a task.
 *
 * @author kawasima
 */
public class LoggingTaskListener implements TaskListener {
    private final Transport transport;

    public LoggingTaskListener(Transport transport) {
        this.transport = transport;
    }

    @Override
    public void beforeTask(GenTask task) {
        // Do nothing
    }

    @Override
    public void afterTask(GenTask task) {
        if (task.getDestinationPath() != null) {
            transport.send(ReplResponse.withOut(
                    String.format(Locale.US, "Generate %s", task.getDestinationPath())
            ));
        }
    }
}
