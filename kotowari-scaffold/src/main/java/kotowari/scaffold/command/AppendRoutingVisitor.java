package kotowari.scaffold.command;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * @author kawasima
 */
public class AppendRoutingVisitor extends VoidVisitorAdapter<RoutingDefineContext> {
    private String controllerClassName;

    public AppendRoutingVisitor(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }

    public void visit(final MethodCallExpr n, final RoutingDefineContext arg) {
        if (n.getName().equals("define") && n.getScope().equals(ASTHelper.createNameExpr("Routes"))) {
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
                    ASTHelper.createNameExpr(arg.getRoutingParameter().getId().getName()),
                    "resource");

            ReferenceType rt = ASTHelper.createReferenceType(controllerClassName, 0);

            ASTHelper.addArgument(call, new ClassExpr(rt.getType()));
            ASTHelper.addStmt(n, call);
        } else {
            super.visit(n, arg);
        }
    }

}
