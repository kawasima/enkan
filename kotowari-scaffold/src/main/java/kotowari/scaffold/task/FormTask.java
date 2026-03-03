package kotowari.scaffold.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.VoidType;
import kotowari.scaffold.model.EntityField;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.CaseConverter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import static com.github.javaparser.ast.NodeList.nodeList;

/**
 * @author kawasima
 */
public class FormTask implements GenTask {
    private final String destination;
    private final String pkgName;
    private final String tableName;
    private final List<EntityField> fields;

    public FormTask(String pkgName, String tableName, List<EntityField> fields) {
        this.pkgName = pkgName;
        this.tableName = tableName;
        this.fields = fields;
        destination = "src/main/java/" + pkgName.replace('.', '/')
                + "form/" + CaseConverter.pascalCase(tableName) + "Form.java";
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(pkgName + "form");

        ClassOrInterfaceDeclaration formClass = new ClassOrInterfaceDeclaration(
                nodeList(Modifier.publicModifier()), false, CaseConverter.pascalCase(tableName) + "Form");
        cu.addType(formClass);
        formClass.setExtendedTypes(nodeList(
                StaticJavaParser.parseClassOrInterfaceType("FormBase")
        ));

        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> formClass.addMember(fieldDeclaration(f)));
        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> formClass.addMember(getterDeclaration(f)));
        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> formClass.addMember(setterDeclaration(f)));

        try (Writer writer = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            writer.write(cu.toString());
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }

    private FieldDeclaration fieldDeclaration(EntityField field) {
        return new FieldDeclaration(nodeList(Modifier.publicModifier()),
                StaticJavaParser.parseClassOrInterfaceType(field.getType().getSimpleName()),
                field.getName());

    }

    private MethodDeclaration getterDeclaration(EntityField field) {
        MethodDeclaration decl = new MethodDeclaration(nodeList(Modifier.publicModifier()),
                StaticJavaParser.parseClassOrInterfaceType(field.getType().getSimpleName()),
                "get" + CaseConverter.pascalCase(field.getName()));
        BlockStmt body = new BlockStmt();
        body.setStatements(
                nodeList(
                        new ReturnStmt(
                                new FieldAccessExpr(new ThisExpr(), field.getName()))));
        decl.setBody(body);
        return decl;
    }

    private MethodDeclaration setterDeclaration(EntityField field) {
        MethodDeclaration decl = new MethodDeclaration(nodeList(Modifier.publicModifier()),
                new VoidType(),
                "set" + CaseConverter.pascalCase(field.getName()),
                Collections.singletonList(new Parameter(
                        ASTHelper.createReferenceType(field.getType().getSimpleName(), 0),
                        field.getName())));

        BlockStmt body = new BlockStmt();
        body.setStatements(
                nodeList(
                        new ExpressionStmt(
                                new AssignExpr(
                                        new FieldAccessExpr(new ThisExpr(), field.getName()),
                                        ASTHelper.createNameExpr(field.getName()),
                                        AssignExpr.Operator.assign
                                ))));
        decl.setBody(body);
        return decl;
    }

}
