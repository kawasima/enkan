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
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.OutputStreamWriter;
import java.util.Arrays;

/**
 * @author kawasima
 */
public class DomaConfigTask implements GenTask {
    private String pkgName;
    private String destination;
    private final AnnotationExpr OVERRIDE_ANNOTATION =
            new MarkerAnnotationExpr(ASTHelper.createNameExpr("Override"));

    public DomaConfigTask(String pkgName) {
        this.pkgName = pkgName;
        destination = "src/main/java/" + pkgName.replace('.', '/') + "/DomaConfig.java";
    }
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(pkgName)));
        cu.setImports(Arrays.asList(
                new ImportDeclaration(ASTHelper.createNameExpr("org.seasar.doma.jdbc.Config"), false, false),
                new ImportDeclaration(ASTHelper.createNameExpr("org.seasar.doma.jdbc.dialect.Dialect"), false, false),
                new ImportDeclaration(ASTHelper.createNameExpr("org.seasar.doma.jdbc.dialect.H2Dialect"), false, false),
                new ImportDeclaration(ASTHelper.createNameExpr("javax.sql.DataSource"), false, false)
        ));

        ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "DomaConfig");
        type.setImplements(Arrays.asList(new ClassOrInterfaceType("Config")));
        ASTHelper.addTypeDeclaration(cu, type);


        MethodDeclaration getDataSourceMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("DataSource", 0), "getDataSource");
        getDataSourceMethod.setAnnotations(Arrays.asList(OVERRIDE_ANNOTATION));
        BlockStmt getDataSourceBody = new BlockStmt();
        ASTHelper.addStmt(getDataSourceBody, new ReturnStmt(new NullLiteralExpr()));
        getDataSourceMethod.setBody(getDataSourceBody);
        ASTHelper.addMember(type, getDataSourceMethod);

        MethodDeclaration getDialectMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType("Dialect", 0), "getDialect");
        getDialectMethod.setAnnotations(Arrays.asList(OVERRIDE_ANNOTATION));
        BlockStmt getDialectBody = new BlockStmt();

        ObjectCreationExpr newDialect = new ObjectCreationExpr(null, new ClassOrInterfaceType("H2Dialect"), null);
        ASTHelper.addStmt(getDialectBody, new ReturnStmt(newDialect));
        getDialectMethod.setBody(getDialectBody);
        ASTHelper.addMember(type, getDialectMethod);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                pathResolver.destinationAsStream(destination))) {
            writer.write(cu.toString());
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }
}
