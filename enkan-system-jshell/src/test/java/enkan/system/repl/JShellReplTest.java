package enkan.system.repl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Bootstrap;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.VarSnippet;
import org.junit.Test;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class JShellReplTest {
    private List<String> list;
    
    @Test
    public void test() {
        System.out.println(Bootstrap.virtualMachineManager().allConnectors());
        JShell jshell = JShell.builder()
                .build();


        jshell.eval("import java.util.*;");
        List<SnippetEvent> events = jshell.eval("List list = new ArrayList();");
        System.out.println(events);
        System.out.println(jshell.variables());

        events = jshell.eval("list.add(\"abc\")");
        System.out.println(events);
    }
}
