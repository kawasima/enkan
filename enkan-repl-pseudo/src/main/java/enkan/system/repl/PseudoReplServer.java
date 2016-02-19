package enkan.system.repl;

import enkan.system.repl.pseudo.Transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kawasima
 */
public class PseudoReplServer {
    ExecutorService executorService;

    public PseudoReplServer() {
        executorService = Executors.newFixedThreadPool(10);
    }

    public void start() throws IOException {
        InetSocketAddress addr = new InetSocketAddress("localhost", 0);
        ServerSocket serverSock = new ServerSocket();
        serverSock.setReuseAddress(true);
        serverSock.bind(addr);

        System.out.println("Listen " + serverSock.getLocalPort());

        do {
            Socket socket = serverSock.accept();
            executorService.submit(new Transport(socket));
        } while(!serverSock.isClosed());

    }

    public static void main(String[] args) throws Exception {
        new PseudoReplServer().start();
    }
}
