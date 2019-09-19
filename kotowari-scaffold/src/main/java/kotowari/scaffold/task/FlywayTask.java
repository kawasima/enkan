package kotowari.scaffold.task;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.CaseConverter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * If flyway component is enabled, generate a flyway migration code.
 * Otherwise, generate a `create table` statement.
 *
 * @author kawasima
 */
public class FlywayTask implements GenTask {
    private static final DateTimeFormatter VERSION_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final String tableName;
    private final String destination;
    private final String createTableStatement;

    public FlywayTask(String destination, String tableName, String createTableStatement) {
        this.destination = destination;
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("db.migration")));
        List<ImportDeclaration> imports = new ArrayList<>();
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.flywaydb.core.api.migration.BaseJavaMigration"), false, false));
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.flywaydb.core.api.migration.Context"), false, false));
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.sql.Statement"), false, false));
        cu.setImports(imports);

        String version = LocalDateTime.now().format(VERSION_FMT);
        ClassOrInterfaceDeclaration migrationClass = new ClassOrInterfaceDeclaration(
                ModifierSet.PUBLIC, false, "V" + version + "__Create"
                + CaseConverter.pascalCase(tableName));
        ASTHelper.addTypeDeclaration(cu, migrationClass);

        List<ClassOrInterfaceType> extendsList = new ArrayList<>();
        extendsList.add(new ClassOrInterfaceType("BaseJavaMigration"));
        migrationClass.setExtends(extendsList);

        MethodDeclaration migrateMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "migrate");
        ASTHelper.addMember(migrationClass, migrateMethod);
        ASTHelper.addParameter(migrateMethod, ASTHelper.createParameter(
                ASTHelper.createReferenceType("Context", 0), "context"
        ));
        migrateMethod.setThrows(Collections.singletonList(ASTHelper.createNameExpr("Exception")));

        List<AnnotationExpr> annotations = new ArrayList<>();
        annotations.add(new MarkerAnnotationExpr(ASTHelper.createNameExpr("Override")));
        migrateMethod.setAnnotations(annotations);

        BlockStmt block = new BlockStmt();
        migrateMethod.setBody(block);

        TryStmt tryStmt = new TryStmt(createStatementExecuteBlock(), null, null);
        tryStmt.setResources(Collections.singletonList(createAssignStatement()));
        ASTHelper.addStmt(block, tryStmt);

        try (Writer writer = new OutputStreamWriter(
                pathResolver.destinationAsStream(
                        "src/main/java/db/migration/" + migrationClass.getName() + ".java"))) {
            writer.write(cu.toString());
        }
    }

    private VariableDeclarationExpr createAssignStatement() {
        VariableDeclarationExpr expr = ASTHelper.createVariableDeclarationExpr(ASTHelper.createReferenceType("Statement", 0), "stmt");
        expr.setVars(Collections.singletonList(new VariableDeclarator(
                new VariableDeclaratorId("stmt"),
                new MethodCallExpr(
                        new MethodCallExpr(ASTHelper.createNameExpr("context"), "getConnection"),
                        "createStatement")
        )));
        return expr;
    }
    private BlockStmt createStatementExecuteBlock() {
        BlockStmt block = new BlockStmt();
        MethodCallExpr stmtExecute = new MethodCallExpr(ASTHelper.createNameExpr("stmt"), "execute");
        ASTHelper.addArgument(stmtExecute, new StringLiteralExpr(createTableStatement));
        ASTHelper.addStmt(block, stmtExecute);
        return block;
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
