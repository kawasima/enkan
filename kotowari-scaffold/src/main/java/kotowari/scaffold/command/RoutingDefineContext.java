package kotowari.scaffold.command;

import com.github.javaparser.ast.body.Parameter;

/**
 * @author kawasima
 */
public class RoutingDefineContext {
    private boolean isInRoutingDefine;
    private Parameter routingParameter;

    public boolean isInRoutingDefine() {
        return isInRoutingDefine;
    }

    public void setInRoutingDefine(boolean inRoutingDefine) {
        isInRoutingDefine = inRoutingDefine;
    }

    public Parameter getRoutingParameter() {
        return routingParameter;
    }

    public void setRoutingParameter(Parameter routingParameter) {
        this.routingParameter = routingParameter;
    }
}
