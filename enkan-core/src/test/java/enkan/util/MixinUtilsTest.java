package enkan.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author kawasima
 */
public class MixinUtilsTest {
    @Test
    public void mixin() {
        Money m1 = new MoneyImpl(5);
        m1 = MixinUtils.mixin(m1, ComparableMoney.class);

        assertTrue(ComparableMoney.class.cast(m1).isBigger(new MoneyImpl(3)));
    }

    @Test(expected = ClassCastException.class)
    public void argumentIsNotInterface() {
        ImplOnly impl = new ImplOnly();
        impl = MixinUtils.mixin(impl, ComparableMoney.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyImplClass() {
        ImplOnly impl = new ImplOnly();
        impl = MixinUtils.mixin(impl, MoneyImpl.class);
    }

    @Test
    public void multipleCall() {
        Money m1 = new MoneyImpl(5);
        m1 = MixinUtils.mixin(m1, ComparableMoney.class);
        Money m2 = MixinUtils.mixin(m1, ComparableMoney.class);
        Assert.assertEquals(m2, m1);
    }

    public static class MoneyImpl implements Money {
        private int amount;

        public MoneyImpl(int amount) {
            this.amount = amount;
        }

        @Override
        public int getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return Integer.toString(amount);
        }
    }

    public interface Money {
        int getAmount();
    }

    public interface ComparableMoney extends Money {
        default boolean isBigger(Money other) {
            return getAmount() > other.getAmount();
        }
    }

    public static class ImplOnly {

    }

}
