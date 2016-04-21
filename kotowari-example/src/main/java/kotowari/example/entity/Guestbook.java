package kotowari.example.entity;

import lombok.Data;
import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;

import java.time.LocalDateTime;

/**
 * @author kawasima
 */
@Entity
@Data
public class Guestbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String message;

    private LocalDateTime postedAt;
}

