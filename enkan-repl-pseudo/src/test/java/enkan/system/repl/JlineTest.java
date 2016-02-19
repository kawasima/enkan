package enkan.system.repl;

import enkan.system.repl.pseudo.ReplClient;

/**
 * @author kawasima
 */
public class JlineTest {
    public static void main(String[] args) throws Exception {
        ReplClient client = new ReplClient();
        client.start();
    }
}
