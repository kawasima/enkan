package kotowari.example.graalvm;

import enkan.system.EnkanSystem;

public class NativeMain {
    public static void main(String[] args) {
        EnkanSystem system = new NativeSystemFactory().create();
        system.start();
        Runtime.getRuntime().addShutdownHook(new Thread(system::stop));
    }
}
