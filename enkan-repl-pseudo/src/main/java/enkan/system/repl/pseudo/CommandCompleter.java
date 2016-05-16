package enkan.system.repl.pseudo;

import jline.console.completer.Completer;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static jline.internal.Preconditions.checkNotNull;

/**
 * A completer for REPL commands.
 *
 * @author kawasima
 */
public class CommandCompleter implements Completer {
    private final SortedSet<String> commands = new TreeSet<>();


    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // buffer could be null
        checkNotNull(candidates);

        if (buffer == null) {
            candidates.addAll(commands);
        }
        else {
            for (String match : commands.tailSet(buffer)) {
                if (!match.startsWith(buffer)) {
                    break;
                }

                candidates.add(match);
            }
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    public void addCommand(String command) {
        commands.add(command);
    }
}
