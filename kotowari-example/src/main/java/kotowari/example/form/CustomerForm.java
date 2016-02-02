package kotowari.example.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author kawasima
 */
@Data
public class CustomerForm implements Serializable {
    @NotNull
    private String name;

    @NotNull
    private String password;
}
