package enkan.graalvm;

import enkan.component.SystemComponent;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.constant.ConstantDescs.*;

/**
 * GraalVM {@link Feature} that generates reflection-free {@link ComponentBinder} classes
 * at native image build time for each component class registered in
 * {@link NativeComponentRegistry}.
 *
 * <p>Component classes are discovered via the system property
 * {@code enkan.component.classes} (comma-separated fully-qualified class names).
 *
 * <p>For each discovered class the Feature:
 * <ol>
 *   <li>Registers the class and its constructors/fields/methods for reflection
 *       (needed by the fallback {@code ComponentInjector} path in non-native JVM mode).</li>
 *   <li>Generates a hidden {@link ComponentBinder} class as a nestmate of the component
 *       class, using the Java Class File API.  The generated {@code bind()} method
 *       uses direct {@code putfield} and {@code invokevirtual} instructions — no
 *       {@code setAccessible} calls.</li>
 *   <li>Registers the generated binder in {@link NativeComponentRegistry}.</li>
 * </ol>
 */
public class EnkanFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        String classNames = System.getProperty("enkan.component.classes");
        if (classNames == null || classNames.isBlank()) {
            return;
        }

        for (String className : classNames.split(",")) {
            className = className.strip();
            if (className.isEmpty()) continue;
            Class<?> componentClass = access.findClassByName(className);
            if (componentClass == null) continue;
            if (!SystemComponent.class.isAssignableFrom(componentClass)) continue;

            registerForReflection(componentClass);
            generateAndRegisterBinder(componentClass);
        }
    }

    private void registerForReflection(Class<?> componentClass) {
        RuntimeReflection.register(componentClass);
        RuntimeReflection.registerAllConstructors(componentClass);
        RuntimeReflection.registerAllFields(componentClass);
        RuntimeReflection.registerAllMethods(componentClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateAndRegisterBinder(Class<?> componentClass) {
        try {
            byte[] bytes = generateBinderBytecode(componentClass);

            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    componentClass, MethodHandles.lookup());
            MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(
                    bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
            Class<?> binderClass = hiddenLookup.lookupClass();
            ComponentBinder binder = (ComponentBinder) binderClass.getConstructor().newInstance();
            NativeComponentRegistry.register((Class) componentClass, binder);
        } catch (Exception e) {
            System.err.println("[EnkanFeature] Could not generate binder for "
                    + componentClass.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Generate bytecode for a {@link ComponentBinder} implementation.
     *
     * <p>The generated class implements {@link ComponentBinder#bind(Map)} and for each
     * {@code @Inject}-annotated field in the component class it emits a direct
     * {@code putfield} instruction (works because the generated class is a nestmate).
     * If a {@code @PostConstruct} method exists it is called via {@code invokevirtual}.
     */
    private byte[] generateBinderBytecode(Class<?> componentClass) {
        ClassDesc binderDesc = ClassDesc.of(componentClass.getName() + "$$Binder");
        ClassDesc componentDesc = ClassDesc.of(componentClass.getName());
        ClassDesc mapDesc = ClassDesc.of("java.util.Map");
        ClassDesc componentBinderDesc = ClassDesc.of(ComponentBinder.class.getName());

        List<Field> injectFields = collectInjectFields(componentClass);
        Method postConstructMethod = findPostConstruct(componentClass);

        return ClassFile.of().build(binderDesc, classBuilder -> {
            classBuilder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL);
            classBuilder.withInterfaceSymbols(componentBinderDesc);

            classBuilder.withMethod(INIT_NAME, MethodTypeDesc.of(CD_void),
                    ClassFile.ACC_PUBLIC,
                    mb -> mb.withCode(cb -> {
                        cb.aload(0);
                        cb.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void));
                        cb.return_();
                    }));

            classBuilder.withMethod("bind",
                    MethodTypeDesc.of(CD_Object, mapDesc),
                    ClassFile.ACC_PUBLIC,
                    mb -> mb.withCode(cb -> {
                        // Create instance via default constructor
                        cb.new_(componentDesc);
                        cb.dup();
                        cb.invokespecial(componentDesc, INIT_NAME, MethodTypeDesc.of(CD_void));
                        cb.astore(2); // store component instance at local var 2

                        // Inject each @Inject field
                        for (Field f : injectFields) {
                            ClassDesc fieldTypeDesc = ClassDesc.of(f.getType().getName());
                            String lookupKey = resolveLookupKey(f);

                            cb.aload(2);        // component instance
                            cb.aload(1);        // Map arg
                            cb.ldc(lookupKey);
                            cb.invokeinterface(mapDesc, "get",
                                    MethodTypeDesc.of(CD_Object, CD_Object));
                            cb.checkcast(fieldTypeDesc);
                            cb.putfield(componentDesc, f.getName(), fieldTypeDesc);
                        }

                        // Call @PostConstruct if present
                        if (postConstructMethod != null) {
                            cb.aload(2);
                            cb.invokevirtual(componentDesc,
                                    postConstructMethod.getName(),
                                    MethodTypeDesc.of(CD_void));
                        }

                        cb.aload(2);
                        cb.areturn();
                    }));
        });
    }

    private List<Field> collectInjectFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getAnnotation(Inject.class) != null) {
                    result.add(f);
                }
            }
            c = c.getSuperclass();
        }
        return result;
    }

    private String resolveLookupKey(Field f) {
        Named named = f.getAnnotation(Named.class);
        return named != null ? named.value() : f.getType().getName();
    }

    private Method findPostConstruct(Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(PostConstruct.class) != null) {
                return m;
            }
        }
        return null;
    }
}
