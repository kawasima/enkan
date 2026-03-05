package enkan.util;

/**
 * Marker interface applied to ByteBuddy-generated mixin subclasses.
 *
 * <p>Used by {@link MixinUtils#mixin} to detect instances that were
 * created via bytecode generation rather than JDK dynamic proxies,
 * so that subsequent mixin calls can accumulate interfaces correctly.
 *
 * @author kawasima
 */
public interface MixinGenerated {
}
