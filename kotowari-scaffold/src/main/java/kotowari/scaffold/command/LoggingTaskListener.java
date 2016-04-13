package kotowari.scaffold.command;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import net.unit8.amagicman.MoldTask;
import net.unit8.amagicman.listener.TaskListener;

import java.util.Locale;

/**
 * @author kawasima
 */
public class LoggingTaskListener implements TaskListener {
    private final Transport transport;

    public LoggingTaskListener(Transport transport) {
        this.transport = transport;
    }

    @Override
    public void beforeTask(MoldTask task) {
        // Do nothing
    }

    @Override
    public void afterTask(MoldTask task) {
        transport.send(ReplResponse.withOut(
                String.format(Locale.US, "Generate %s", task.getDestinationPath())
        ));
    }
}
