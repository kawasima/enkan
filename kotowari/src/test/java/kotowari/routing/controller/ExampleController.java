package kotowari.routing.controller;

import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class ExampleController {
    public String method1() {
        return "method1";
    }

    public String method2(Map<String, List<String>> params) {
        System.out.println(params);
        return "method2 " + params.get("name");
    }

    public String method3() {
        return "method3";
    }
    public String method4() {
        return "method4";
    }
    public String method5() {
        return "method5";
    }
    public String method6() {
        return "method6";
    }

}
