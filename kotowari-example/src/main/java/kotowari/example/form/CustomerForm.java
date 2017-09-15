package kotowari.example.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author kawasima
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerForm extends FormBase {
    @NotBlank
    @Size(max = 10)
    private String name;

    @NotBlank
    private String password;

    @Email
    private String email;

    @Pattern(regexp = "[MF]")
    private String gender;

    private String birthday;
}
