package kotowari.example.dao;

import kotowari.example.DomaConfig;
import kotowari.example.entity.Customer;
import org.seasar.doma.*;

import java.util.List;

/**
 * @author kawasima
 */
@Dao(config = DomaConfig.class)
public interface CustomerDao {
    @Select
    Customer selectById(Long id);

    @Select
    Customer loginByPassword(String email, String password);

    @Select
    List<Customer> selectAll();

    @Insert
    int insert(Customer customer);

    @Update
    int update(Customer customer);

    @Delete
    int delete(Customer customer);
}
