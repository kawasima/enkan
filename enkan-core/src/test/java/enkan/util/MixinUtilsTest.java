package enkan.util;

import enkan.data.Extendable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class MixinUtilsTest {
    @Test
    public void mixin() {
        Money m1 = new MoneyImpl(5);
        m1 = MixinUtils.mixin(m1, ComparableMoney.class);

        assertThat(((ComparableMoney) m1).isBigger(new MoneyImpl(3)))
                .isTrue();
    }

    @Test
    public void argumentIsNotInterface() {
        assertThatThrownBy(() -> {
            ImplOnly impl = new ImplOnly();
            impl = MixinUtils.mixin(impl, ComparableMoney.class);
            assertThat((ComparableMoney) impl).isNotNull();
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

    public record MoneyImpl(int amount) implements Money {

        @Override
            public String toString() {
                return Integer.toString(amount);
            }
        }

    public interface Money {
        int amount();
    }

    public interface ComparableMoney extends Money {
        default boolean isBigger(Money other) {
            return amount() > other.amount();
        }
    }

    public static class ImplOnly {

    }

    // --- Extendable (ByteBuddy) path tests ---

    public static class SimpleExtendable implements Extendable {
        private final Map<String, Object> extensions = new HashMap<>();
        private String name;

        protected SimpleExtendable() {}

        public SimpleExtendable(String name) {
            this.name = name;
        }

        public String getName() { return name; }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getExtension(String key) { return (T) extensions.get(key); }

        @Override
        public <T> void setExtension(String key, T value) { extensions.put(key, value); }
    }

    public interface Taggable extends Extendable {
        default String getTag() { return getExtension("tag"); }
        default void setTag(String tag) { setExtension("tag", tag); }
    }

    public interface Prioritizable extends Extendable {
        default int getPriority() {
            Integer p = getExtension("priority");
            return p != null ? p : 0;
        }
        default void setPriority(int p) { setExtension("priority", p); }
    }

    @Test
    public void extendableMixinProducesByteBuddySubclass() {
        SimpleExtendable obj = new SimpleExtendable("test");
        Object mixed = MixinUtils.mixin(obj, Taggable.class);

        assertThat(mixed).isInstanceOf(SimpleExtendable.class);
        assertThat(mixed).isInstanceOf(Taggable.class);
        assertThat(mixed).isInstanceOf(MixinGenerated.class);
        assertThat(((SimpleExtendable) mixed).getName()).isEqualTo("test");
    }

    @Test
    public void extendableMixinDefaultMethodsWork() {
        SimpleExtendable obj = new SimpleExtendable("test");
        Taggable mixed = (Taggable) MixinUtils.mixin(obj, Taggable.class);

        mixed.setTag("important");
        assertThat(mixed.getTag()).isEqualTo("important");
    }

    @Test
    public void extendableLayeredMixin() {
        SimpleExtendable obj = new SimpleExtendable("test");
        Object mixed = MixinUtils.mixin(obj, Taggable.class);
        mixed = MixinUtils.mixin(mixed, Prioritizable.class);

        assertThat(mixed).isInstanceOf(Taggable.class);
        assertThat(mixed).isInstanceOf(Prioritizable.class);
        assertThat(mixed).isInstanceOf(MixinGenerated.class);

        ((Taggable) mixed).setTag("layered");
        ((Prioritizable) mixed).setPriority(5);
        assertThat(((Taggable) mixed).getTag()).isEqualTo("layered");
        assertThat(((Prioritizable) mixed).getPriority()).isEqualTo(5);
    }

    @Test
    public void extendableMixinSkipsAlreadyImplemented() {
        SimpleExtendable obj = new SimpleExtendable("test");
        Object mixed = MixinUtils.mixin(obj, Taggable.class);
        Object same = MixinUtils.mixin(mixed, Taggable.class);

        // Should return same object since Taggable is already implemented
        assertThat(same).isSameAs(mixed);
    }

    @Test
    public void extendableMixinPreservesFieldsAcrossLayers() {
        SimpleExtendable obj = new SimpleExtendable("original");
        Taggable first = (Taggable) MixinUtils.mixin(obj, Taggable.class);
        first.setTag("preserved");

        Prioritizable second = (Prioritizable) MixinUtils.mixin(first, Prioritizable.class);
        // Tag set on previous layer should be preserved via field copy
        assertThat(((Taggable) second).getTag()).isEqualTo("preserved");
        assertThat(((SimpleExtendable) second).getName()).isEqualTo("original");
    }
}
