package enkan.util;

import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.Traceable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author kawasima
 */
public class HttpRequestUtilsTest {
    @Test
    public void testMixin() {
        HttpRequest request = new DefaultHttpRequest();
        request = MixinUtils.mixin(request, Traceable.class);
        Assert.assertTrue(request != null);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            request.setId("ABC123");
        }
        System.out.println(System.currentTimeMillis() - t1);
        Assert.assertEquals("ABC123", request.getId());
    }

    @Test
    public void testMixin2() {
        class TraceableReq extends DefaultHttpRequest implements Traceable {
        }
        TraceableReq request = new TraceableReq();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            request.setId("ABC123");
        }
        System.out.println(System.currentTimeMillis() - t1);
        Assert.assertEquals("ABC123", request.getId());
    }

    @Test
    public void testMixin3() throws Exception {
        class TraceableReq extends DefaultHttpRequest implements Traceable {
        }
        TraceableReq request = new TraceableReq();
        Method m = TraceableReq.class.getMethod("setId", String.class);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            m.invoke(request, "ABC123");
        }
        System.out.println(System.currentTimeMillis() - t1);
        Assert.assertEquals("ABC123", request.getId());
    }

}
