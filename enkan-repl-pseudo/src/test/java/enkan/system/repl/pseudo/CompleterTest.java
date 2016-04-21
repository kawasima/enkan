package enkan.system.repl.pseudo;

import jline.console.completer.AggregateCompleter;
import jline.console.completer.StringsCompleter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class CompleterTest {
    @Test
    public void testStringsCompleter() {
        StringsCompleter completer = new StringsCompleter("abc", "abd", "ac");
        List<CharSequence> candidates = new ArrayList<>();
        completer.complete("a", 1, candidates);
        System.out.println(candidates);
        candidates.clear();
        completer.complete("ab", 2, candidates);
        System.out.println(candidates);
    }

}
