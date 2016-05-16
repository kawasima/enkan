package kotowari.scaffold.task;

import kotowari.scaffold.model.EntityField;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.CaseConverter;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class PatternTemplateTask implements GenTask {
    private final String destination;
    private final String tableName;
    private final List<EntityField> fields;
    private final Map<Class<?>, String> typeTemplateMap;

    public PatternTemplateTask(String destination, String tableName, List<EntityField> fields) {
        this.destination = destination;
        this.tableName = tableName;
        this.fields = fields;
        typeTemplateMap = new HashMap<>();
    }

    public PatternTemplateTask addMapping(Class<?> type, String patternTemplate) {
        typeTemplateMap.put(type, patternTemplate);
        return this;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(pathResolver.destinationAsStream(destination)))) {
            fields.stream()
                    .filter(f -> !f.isId())
                    .map(f -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(pathResolver.templateAsStream(typeTemplateMap.get(f.getType()))))) {
                            return reader.lines()
                                    .map(line -> line
                                            .replaceAll("\\$Entity\\$", CaseConverter.pascalCase(tableName))
                                            .replaceAll("\\$entity\\$", CaseConverter.camelCase(tableName))
                                            .replaceAll("\\$Field\\$", CaseConverter.camelCase(f.getName()))
                                            .replaceAll("\\$field\\$", CaseConverter.camelCase(f.getName())))
                                    .collect(Collectors.joining("\n"));
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    })
                    .forEach(str -> {
                        try {
                            writer.write(str);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
