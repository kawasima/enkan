package enkan.middleware.normalizer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class IcuNormalizerTest {
    @Test
    public void half2full() {
        IcuNormalizer normalizer = new IcuNormalizer("Halfwidth-Fullwidth");
        assertEquals("１２３４", normalizer.normalize("1234"));
    }

    @Test
    public void full2half() {
        IcuNormalizer normalizer = new IcuNormalizer("Fullwidth-Halfwidth");
        assertEquals("西新宿ｸﾞﾗﾝﾄﾞ1−5−4", normalizer.normalize("西新宿グランド１−５−４"));
    }
}
