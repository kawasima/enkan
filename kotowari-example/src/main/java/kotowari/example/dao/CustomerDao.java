package kotowari.example.dao;

import kotowari.example.DomaConfig;
import kotowari.example.entity.Customer;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;

/**
 * @author kawasima
 */
@Dao(config = DomaConfig.class)
public interface CustomerDao {
    @Select
    Customer selectById(Long id);

    @Insert
    int insert(Customer customer);
}
