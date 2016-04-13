package kotowari.scaffold.task;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import net.unit8.amagicman.MoldTask;
import net.unit8.amagicman.PathResolver;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * If flyway component is enabled, generate a flyway migration code.
 * Otherwise, generate a `create table` statement.
 *
 * @author kawasima
 */
public class FlywayTask implements MoldTask {
    private String tableName;
    private String destination;

    public FlywayTask(String destination) {

    }
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("db.migration")));
        List<ImportDeclaration> imports = new ArrayList<>();
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("org.flywaydb.core.api.migration.jdbc.JdbcMigration"), false, false));
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.sql.Connection"), false, false));
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("java.sql.Statement"), false, false));
        cu.setImports(imports);

        String version = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        ClassOrInterfaceDeclaration migrationClass = new ClassOrInterfaceDeclaration(
                ModifierSet.PUBLIC, false, "V" + version + "__Create" + tableName);
        List<ClassOrInterfaceType> implementList = new ArrayList<>();
        implementList.add(new ClassOrInterfaceType("JdbcMigration"));
        migrationClass.setImplements(implementList);
        MethodDeclaration migrateMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "migrate");

        ASTHelper.addParameter(migrateMethod, ASTHelper.createParameter(
                ASTHelper.createReferenceType("Connection", 0), "connection"
        ));
        migrateMethod.setThrows(Arrays.asList(ASTHelper.createNameExpr("Exception")));

        List<AnnotationExpr> annotations = new ArrayList<>();
        annotations.add(new MarkerAnnotationExpr(ASTHelper.createNameExpr("Override")));
        migrateMethod.setAnnotations(annotations);

        BlockStmt block = new BlockStmt();
        migrateMethod.setBody(block);

        try (Writer writer = new OutputStreamWriter(
                pathResolver.destinationAsStream(
                        "src/main/java/db/migration/" + migrationClass.getName() + ".java"))) {
            writer.write(cu.toString());
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
