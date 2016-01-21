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
}
