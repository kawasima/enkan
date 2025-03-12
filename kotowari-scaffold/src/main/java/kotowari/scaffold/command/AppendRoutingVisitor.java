package kotowari.scaffold.command;

import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import static com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType;

/**
 * @author kawasima
 */
public class AppendRoutingVisitor extends VoidVisitorAdapter<RoutingDefineContext> {
    private final String controllerClassName;

    public AppendRoutingVisitor(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }

    public void visit(final MethodCallExpr n, final RoutingDefineContext arg) {
        if (n.getName().asString().equals("define") && n.getScope().filter(s -> s.equals(new NameExpr("Routes"))).isPresent()) {
            arg.setInRoutingDefine(true);
            super.visit(n, arg);
            arg.setInRoutingDefine(false);
        } else {
            super.visit(n, arg);
        }
    }

    public void visit(final LambdaExpr n, final RoutingDefineContext arg) {
        if (arg.isInRoutingDefine()) {
            arg.setRoutingParameter(n.getParameters().get(0));
            n.getBody().accept(this, arg);
        }
    }

    public void visit(final BlockStmt n, final RoutingDefineContext arg) {
        if (arg.isInRoutingDefine()) {
            MethodCallExpr call = new MethodCallExpr(
                    new NameExpr(arg.getRoutingParameter().getNameAsString()),
                    "resource");
            call.addArgument(new ClassExpr(parseClassOrInterfaceType(controllerClassName)));
            n.addStatement(call);
        } else {
            super.visit(n, arg);
        }
    }

}
