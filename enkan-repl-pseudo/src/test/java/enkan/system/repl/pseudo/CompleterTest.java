package enkan.system.repl.pseudo;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class CompleterTest {
    @Test
    public void testStringsCompleter() {
        StringsCompleter completer = new StringsCompleter("abc", "abd", "ac");
        List<Candidate> candidates = new ArrayList<>();
        
        LineReader reader = Mockito.mock(LineReader.class);
        ParsedLine line = Mockito.mock(ParsedLine.class);
        Mockito.when(line.word()).thenReturn("a");
        Mockito.when(line.wordCursor()).thenReturn(1);
        
        completer.complete(reader, line, candidates);
        System.out.println(candidates);
        
        candidates.clear();
        Mockito.when(line.word()).thenReturn("ab");
        Mockito.when(line.wordCursor()).thenReturn(2);
        
        completer.complete(reader, line, candidates);
        System.out.println(candidates);
    }
}
