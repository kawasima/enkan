package benchmark.spring;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class BenchmarkController {

    @GetMapping(value = "/hello", produces = "text/plain")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping(value = "/json", produces = "application/json")
    public Map<String, Object> json() {
        return Map.of(
                "message", "Hello, World!",
                "timestamp", Instant.now().toEpochMilli()
        );
    }

    @GetMapping(value = "/echo", produces = "application/json")
    public Map<String, Object> echo(
            @RequestParam(defaultValue = "world") String name,
            HttpSession session) {
        Integer count = (Integer) session.getAttribute("count");
        if (count == null) count = 0;
        count++;
        session.setAttribute("count", count);
        return Map.of("name", name, "count", count);
    }
}
