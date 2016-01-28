package kotowari.example.entity;

import lombok.Data;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;

/**
 * @author kawasima
 */
@Entity
@Data
public class Customer {
    @Id
    Long id;

    String name;


}
