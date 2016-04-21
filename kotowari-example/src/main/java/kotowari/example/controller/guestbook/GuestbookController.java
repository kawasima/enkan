package kotowari.example.controller.guestbook;

import enkan.collection.Parameters;
import enkan.component.doma2.DomaProvider;
import enkan.data.HttpResponse;
import enkan.security.UserPrincipal;
import kotowari.component.TemplateEngine;
import kotowari.example.dao.GuestbookDao;
import kotowari.example.entity.Guestbook;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static kotowari.routing.UrlRewriter.redirect;

/**
 * @author kawasima
 */
public class GuestbookController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider domaProvider;

    public HttpResponse list() {
        GuestbookDao dao = domaProvider.getDao(GuestbookDao.class);
        List<Guestbook> guestbooks = dao.selectAll();
        return templateEngine.render("guestbook/list",
                "guestbooks", guestbooks);
    }

    @Transactional
    public HttpResponse post(Parameters params, UserPrincipal principal) {
        GuestbookDao dao = domaProvider.getDao(GuestbookDao.class);
        Guestbook guestbook = builder(new Guestbook())
                .set(Guestbook::setName,    principal.getName())
                .set(Guestbook::setMessage, params.get("message"))
                .set(Guestbook::setPostedAt, LocalDateTime.now())
                .build();
        dao.insert(guestbook);
        return redirect(GuestbookController.class, "list", SEE_OTHER);
    }
}
