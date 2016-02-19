package kotowari.example.entity;

import lombok.Data;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;

import javax.persistence.GeneratedValue;
import java.time.LocalDateTime;

/**
 * @author kawasima
 */
@Entity
@Data
public class Guestbook {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String message;

    private LocalDateTime postedAt;
}

