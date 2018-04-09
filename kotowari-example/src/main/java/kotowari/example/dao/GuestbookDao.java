package kotowari.example.dao;

import kotowari.example.entity.Guestbook;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;

import java.util.List;

/**
 * @author kawasima
 */
@Dao
public interface GuestbookDao {
    @Select
    List<Guestbook> selectAll();

    @Insert
    int insert(Guestbook customer);

}
