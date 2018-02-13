package kotowari.scaffold.task;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import kotowari.scaffold.model.EntityField;
import kotowari.scaffold.util.BasePackageDetector;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.CaseConverter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author kawasima
 */
public class FormTask implements GenTask {
    private final String destination;
    @SuppressWarnings("FieldCanBeLocal")
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
        String basePackage = BasePackageDetector.detect();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(basePackage + "form")));

        ClassOrInterfaceDeclaration formClass = new ClassOrInterfaceDeclaration(
                ModifierSet.PUBLIC, false, CaseConverter.pascalCase(tableName) + "Form");
        ASTHelper.addTypeDeclaration(cu, formClass);
        formClass.setExtends(Collections.singletonList(
                new ClassOrInterfaceType("FormBase")
        ));

        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> ASTHelper.addMember(formClass, fieldDeclaration(f)));
        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> ASTHelper.addMember(formClass, getterDeclaration(f)));
        fields.stream()
                .filter(f -> !f.isId())
                .forEach(f -> ASTHelper.addMember(formClass, setterDeclaration(f)));

        try (Writer writer = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            writer.write(cu.toString());
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }

    private FieldDeclaration fieldDeclaration(EntityField field) {
        VariableDeclarator variableDec = new VariableDeclarator(new VariableDeclaratorId(field.getName()));
        List<AnnotationExpr> annoList = new ArrayList<>();
        return new FieldDeclaration(ModifierSet.PRIVATE,
                annoList,
                ASTHelper.createReferenceType(field.getType().getSimpleName(), 0),
                Collections.singletonList(variableDec));
    }

    private MethodDeclaration getterDeclaration(EntityField field) {
        MethodDeclaration decl = new MethodDeclaration(ModifierSet.PUBLIC,
                ASTHelper.createReferenceType(field.getType().getSimpleName(), 0),
                "get" + CaseConverter.pascalCase(field.getName()));
        BlockStmt body = new BlockStmt();
        body.setStmts(
                Collections.singletonList(
                        new ReturnStmt(
                                new FieldAccessExpr(new ThisExpr(), field.getName()))));
        decl.setBody(body);
        return decl;
    }

    private MethodDeclaration setterDeclaration(EntityField field) {
        MethodDeclaration decl = new MethodDeclaration(ModifierSet.PUBLIC,
                new VoidType(),
                "set" + CaseConverter.pascalCase(field.getName()),
                Collections.singletonList(new Parameter(
                        ASTHelper.createReferenceType(field.getType().getSimpleName(), 0),
                        new VariableDeclaratorId(field.getName()))));

        BlockStmt body = new BlockStmt();
        body.setStmts(
                Collections.singletonList(
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
