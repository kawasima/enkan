package enkan.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class MixinUtilsTest {
    @Test
    public void mixin() {
        Money m1 = new MoneyImpl(5);
        m1 = MixinUtils.mixin(m1, ComparableMoney.class);

        assertThat(ComparableMoney.class.cast(m1).isBigger(new MoneyImpl(3)))
                .isTrue();
    }

    @Test
    public void argumentIsNotInterface() {
        assertThatThrownBy(() -> {
            ImplOnly impl = new ImplOnly();
            MixinUtils.mixin(impl, ComparableMoney.class);
        }).isExactlyInstanceOf(ClassCastException.class);
    }

    @Test
    public void proxyImplClass() {
        assertThatThrownBy(() -> {
            ImplOnly impl = new ImplOnly();
            MixinUtils.mixin(impl, MoneyImpl.class);
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void multipleCall() {
        Money m1 = new MoneyImpl(5);
        m1 = MixinUtils.mixin(m1, ComparableMoney.class);
        Money m2 = MixinUtils.mixin(m1, ComparableMoney.class);
        assertThat(m2).isEqualTo(m1);
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
