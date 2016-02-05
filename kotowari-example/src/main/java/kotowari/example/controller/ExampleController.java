package kotowari.example.controller;

import enkan.collection.Multimap;
import enkan.component.DomaProvider;
import enkan.data.HttpResponse;
import enkan.data.Session;
import enkan.util.BeanBuilder;
import kotowari.component.TemplateEngineComponent;
import kotowari.example.dao.CustomerDao;
import kotowari.example.entity.Customer;
import kotowari.example.form.ExampleForm;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class ExampleController {
    @Inject
    private TemplateEngineComponent templateEngine;

    @Inject
    private DomaProvider daoProvider;

    public HttpResponse index() {
        return templateEngine.render("index");
    }

    public HttpResponse method1(Session session) {
        int count = 0;
        if (session != null) {
            count = session.getAttribute("count");
            count++;
        } else {
            session = new Session();
        }
        session.setAttribute("count", count);
        return (HttpResponse) BeanBuilder.builder(HttpResponse.of(count + "times."))
                .set(HttpResponse::setSession, session)
                .set(HttpResponse::setHeaders, Multimap.of("Content-Type", "text/html"))
                .build();
    }

    public String method2(Map<String, List<String>> params) {
        return "method2です " + params.get("name");
    }

    @Transactional
    public String method4(Map<String, List<String>> params) {
        CustomerDao customerDao = daoProvider.getDao(CustomerDao.class);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Kawasima");
        customerDao.insert(customer);
        return "insert customer";
    }

    public HttpResponse method3(ExampleForm form) {
        CustomerDao customerDao = daoProvider.getDao(CustomerDao.class);
        Customer customer = customerDao.selectById(1L);
        return BeanBuilder.builder(templateEngine.render("example",
                "customer", customer))
                .set(HttpResponse::setStatus, 200)
                .build();
    }
}
