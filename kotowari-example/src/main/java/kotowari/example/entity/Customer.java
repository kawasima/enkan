package kotowari.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * @author kawasima
 */
@Entity
@Data
@EqualsAndHashCode
public class Customer implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;
    private String password;

    private String email;

    private String gender;

    private LocalDate birthday;
}
