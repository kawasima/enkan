package kotowari.example.entity;

import lombok.Data;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;

import java.time.LocalDateTime;

/**
 * @author kawasima
 */
@Entity
@Data
public class User {
    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean admin;

    private LocalDateTime lastLogin;

    private Boolean isActive;

    private String pass;
}
