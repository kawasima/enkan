package enkan.system.repl;

import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.system.loader.EnkanLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author kawasima
 */
public class PseudoRepl implements Runnable {
    private EnkanSystem system;

    public PseudoRepl(String enkanSystemFactoryClassName) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<? extends EnkanSystemFactory> clazz = (Class<? extends EnkanSystemFactory>) loader
                    .loadClass(enkanSystemFactoryClassName);
            system = clazz.newInstance().create();
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

    protected boolean repl(BufferedReader reader) throws IOException {
        System.out.print("REPL> ");
        String cmd = reader.readLine().trim();
        switch (cmd) {
            case "start":
                system.start();
                break;
            case "stop":
                system.stop();
                break;
            case "reset":
                system.stop();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl instanceof EnkanLoader) ((EnkanLoader) cl).reload();
                system.start();
                break;
            case "exit":
                system.stop();
                return false;
            case "":
                printHelp();
                break;
            default:
                System.out.println("Unknown command: " + cmd);
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
