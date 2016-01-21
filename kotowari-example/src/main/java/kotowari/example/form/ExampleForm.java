package kotowari.example.form;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author kawasima
 */
public class ExampleForm implements Serializable {
    @Size(max = 4)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
