package kotowari.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author kawasima
 */
@Entity
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

    public User() {
    }

    public String getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public Boolean getAdmin() {
        return this.admin;
    }

    public LocalDateTime getLastLogin() {
        return this.lastLogin;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public String getPass() {
        return this.pass;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String toString() {
        return "User(id=" + this.getId() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ", email=" + this.getEmail() + ", admin=" + this.getAdmin() + ", lastLogin=" + this.getLastLogin() + ", isActive=" + this.getIsActive() + ", pass=" + this.getPass() + ")";
    }

    public boolean equals(Object o) {
        return Optional.ofNullable(o)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .filter(u -> Objects.equals(id, u.getId()))
                .isPresent();
    }

    public int hashCode() {
        return id.hashCode();
    }
}
