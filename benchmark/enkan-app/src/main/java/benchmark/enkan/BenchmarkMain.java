package benchmark.enkan;

import enkan.system.EnkanSystem;

public class BenchmarkMain {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("enkan.env", "production");
        EnkanSystem system = new BenchmarkSystemFactory().create();
        system.start();
        System.out.println("Enkan benchmark app started on port 8080");

        Runtime.getRuntime().addShutdownHook(new Thread(system::stop));
        Thread.currentThread().join();
    }
}
