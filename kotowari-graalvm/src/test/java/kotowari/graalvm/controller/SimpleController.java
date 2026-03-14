package kotowari.graalvm.controller;

import enkan.data.HttpRequest;

public class SimpleController {
    public String index() {
        return "index";
    }

    public String show(HttpRequest request) {
        return "show";
    }
}
