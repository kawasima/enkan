package enkan.scaffold;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.application.WebApplication;
import enkan.collection.Parameters;
import enkan.endpoint.ResourceEndpoint;
import enkan.middleware.*;
import enkan.predicate.NonePredicate;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 * @author kawasima
 */
public class ScaffoldTest {
    private Application createApplication() {
        Application app = new WebApplication();

        // Enkan
        app.use(new DefaultCharsetMiddleware());
        app.use(new NonePredicate(), new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(new StacktraceMiddleware());
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware());
        app.use(new HttpStatusCatMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new MultipartParamsMiddleware());
        app.use(new MethodOverrideMiddleware("_method"));
        app.use(new NormalizationMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new SessionMiddleware());
        app.use(new ResourceMiddleware());
        app.use(new HtmlRenderer());

        return app;
    }

    public void middlewareParser(Middleware<?,?> middleware) {
        Constructor<?>[] constructors = middleware.getClass().getConstructors();
        for (Constructor<?> constructor : constructors) {
        }
    }

    public void addMiddleware(BlockStmt block, List<MiddlewareChain<?, ?>> chains) {
        for (MiddlewareChain<?,?> chain : chains) {
            NameExpr app = new NameExpr("app");
            MethodCallExpr call = new MethodCallExpr(app, "use");
            ClassOrInterfaceType middleware = new ClassOrInterfaceType(chain.getMiddleware().getClass().getName());
            ObjectCreationExpr middlewareCreation = new ObjectCreationExpr(null, middleware, null);
            ASTHelper.addArgument(call, middlewareCreation);
            ASTHelper.addStmt(block, call);
        }
    }

    @Test
    public void test() {
        Application app = createApplication();

        CompilationUnit cu = new CompilationUnit();
        ClassOrInterfaceType application = new ClassOrInterfaceType(Application.class.getName());

        cu.setImports(Arrays.asList(new ImportDeclaration(ASTHelper.createNameExpr("enkan.component.ComponentInjector"), false, false)));

        // Class
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("java.parser.test")));
        ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "MyApplicationFactory");
        type.setImplements(Arrays.asList(new ClassOrInterfaceType("enkan.application.WebApplication")));
        ASTHelper.addTypeDeclaration(cu, type);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, application, "create");
        ASTHelper.addMember(type, method);
        method.setAnnotations(Arrays.asList(new MarkerAnnotationExpr(ASTHelper.createNameExpr("Override"))));


        Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("enkan.component.ComponentInjector", 0), "injector");
        ASTHelper.addParameter(method, param);

        BlockStmt block = new BlockStmt();
        method.setBody(block);

        // add a statement do the method body
        addMiddleware(block, app.getMiddlewareStack());
        System.out.println(cu.toString());
    }
}
