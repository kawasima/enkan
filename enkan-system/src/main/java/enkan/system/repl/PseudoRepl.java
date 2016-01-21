package enkan.system.repl;

import enkan.system.EnkanSystem;
import enkan.exception.UnrecoverableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author kawasima
 */
public class PseudoRepl implements Runnable {
    private EnkanSystem system;

    public PseudoRepl(EnkanSystem system) {
        this.system = system;
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
            throw UnrecoverableException.raise(ex);
        }
    }

}
