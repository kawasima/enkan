package kotowari.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;

import java.time.LocalDateTime;

/**
 * @author kawasima
 */
@Entity
public class Guestbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String message;

    private LocalDateTime postedAt;

    public Guestbook() {
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getPostedAt() {
        return this.postedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public String toString() {
        return "Guestbook(id=" + this.getId() + ", name=" + this.getName() + ", message=" + this.getMessage() + ", postedAt=" + this.getPostedAt() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Guestbook)) return false;
        final Guestbook other = (Guestbook) o;
        if (!other.canEqual(this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$postedAt = this.getPostedAt();
        final Object other$postedAt = other.getPostedAt();
        if (this$postedAt == null ? other$postedAt != null : !this$postedAt.equals(other$postedAt)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $postedAt = this.getPostedAt();
        result = result * PRIME + ($postedAt == null ? 43 : $postedAt.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof Guestbook;
    }
}

