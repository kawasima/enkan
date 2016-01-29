package kotowari.example.controller;

import enkan.component.DomaDaoProvider;
import enkan.data.HttpResponse;
import kotowari.component.TemplateEngineComponent;
import kotowari.example.dao.CustomerDao;
import kotowari.example.entity.Customer;
import kotowari.example.form.ExampleForm;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class ExampleController {
    @Inject
    private TemplateEngineComponent templateEngine;

    @Inject
    private DomaDaoProvider daoProvider;

    public HttpResponse index() {
        return templateEngine.render("index");
    }

    public String method2(Map<String, List<String>> params) {
        return "method2です " + params.get("name");
    }

    public String method4(Map<String, List<String>> params) {
        CustomerDao customerDao = daoProvider.get(CustomerDao.class);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("川島");
        customerDao.insert(customer);
        return "insert customer";
    }

    public HttpResponse method3(ExampleForm form) {
        CustomerDao customerDao = daoProvider.get(CustomerDao.class);
        Customer customer = customerDao.selectById(1l);
        return templateEngine.render("example",
                "customer", customer);
    }
}
