package kotowari.scaffold.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import kotowari.scaffold.model.EntityField;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class EntitySourceAnalyzer {
    private static final Pattern RE_GETTER = Pattern.compile("^get([A-Z]?.*)$");

    public List<EntityField> analyze(File source) throws IOException, ParseException {
        List<EntityField> entityFields = new ArrayList<>();
        CompilationUnit cu = JavaParser.parse(source);
        cu.accept(new VoidVisitorAdapter<List<EntityField>>() {
            @Override
            public void visit(FieldDeclaration fd, List<EntityField> f) {
                if (fd.getAnnotations().stream().anyMatch(anno -> anno.getName().getName().equals("Column"))) {
                    Class<?> type = null;
                    switch (fd.getType().toString()) {
                        case "String": type = String.class; break;
                        case "Long": type = Long.class; break;
                        case "Integer": type = Integer.class; break;
                        case "boolean": type = boolean.class; break;
                    }
                    if (type == null) return;
                    f.add(new EntityField(
                            fd.getVariables().get(0).getId().getName(),
                            type,
                            fd.getAnnotations().stream().anyMatch(anno -> anno.getName().getName().equals("Id"))));
                }
            }
        }, entityFields);

        return entityFields;
    }
}
