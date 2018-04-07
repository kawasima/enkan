package kotowari.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;

import java.time.LocalDateTime;

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
        if (o == this) return true;
        if (!(o instanceof User)) return false;
        final User other = (User) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$firstName = this.getFirstName();
        final Object other$firstName = other.getFirstName();
        if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName)) return false;
        final Object this$lastName = this.getLastName();
        final Object other$lastName = other.getLastName();
        if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$admin = this.getAdmin();
        final Object other$admin = other.getAdmin();
        if (this$admin == null ? other$admin != null : !this$admin.equals(other$admin)) return false;
        final Object this$lastLogin = this.getLastLogin();
        final Object other$lastLogin = other.getLastLogin();
        if (this$lastLogin == null ? other$lastLogin != null : !this$lastLogin.equals(other$lastLogin)) return false;
        final Object this$isActive = this.getIsActive();
        final Object other$isActive = other.getIsActive();
        if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
        final Object this$pass = this.getPass();
        final Object other$pass = other.getPass();
        if (this$pass == null ? other$pass != null : !this$pass.equals(other$pass)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $firstName = this.getFirstName();
        result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
        final Object $lastName = this.getLastName();
        result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $admin = this.getAdmin();
        result = result * PRIME + ($admin == null ? 43 : $admin.hashCode());
        final Object $lastLogin = this.getLastLogin();
        result = result * PRIME + ($lastLogin == null ? 43 : $lastLogin.hashCode());
        final Object $isActive = this.getIsActive();
        result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
        final Object $pass = this.getPass();
        result = result * PRIME + ($pass == null ? 43 : $pass.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof User;
    }
}
