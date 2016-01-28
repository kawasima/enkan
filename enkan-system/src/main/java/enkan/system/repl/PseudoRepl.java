package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class PseudoRepl implements Runnable {
    private EnkanSystem system;
    private ExecutorService monitorService;

    public PseudoRepl(String enkanSystemFactoryClassName) {
        try {
            system = ((Class<? extends EnkanSystemFactory>) Class.forName(enkanSystemFactoryClassName)).newInstance().create();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected void printHelp() {
        System.out.println("start - Start system\n" +
                "stop - Stop system.\n" +
                "reset - Reset system.\n" +
                "exit - exit repl.\n"
        );
    }

    protected void startChangeMonitor(ClassLoader cl) throws IOException, URISyntaxException {
        if (monitorService == null) {
            monitorService = Executors.newCachedThreadPool();
        }
    }

    protected void stopChangeMonitor() {
        if (monitorService != null) {
            monitorService.shutdown();
            monitorService = null;
        }
    }

    protected boolean repl(BufferedReader reader) throws IOException, URISyntaxException {
        System.out.print("REPL> ");
        String[] cmd = reader.readLine().trim().split("\\s+");
        switch (cmd[0]) {
            case "start":
                system.start();
                if (cmd.length > 1 && cmd[1].equals("auto")) {

                }
                break;
            case "stop":
                system.stop();
                stopChangeMonitor();
                break;
            case "reset": {
                system.stop();
                system.start();
                break;
            }
            case "exit":
                system.stop();
                return false;
            case "":
                printHelp();
                break;
            default:
                System.out.println("Unknown command: " + cmd[0]);
        }
        return true;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            for(;;) {
                if (!repl(reader)) break;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

}
