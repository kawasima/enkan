package enkan.util;

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
        default boolean isBigger(enkan.util.MixinUtilsTest.Money other) {
            return getAmount() > other.getAmount();
        }
    }
}
