package enkan.graalvm;

import enkan.component.SystemComponent;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
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
        // Only generate a binder when the class has an accessible no-arg constructor.
        // Classes that rely on constructor injection fall back to ComponentInjector.
        try {
            componentClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            System.err.println("[EnkanFeature] Skipping binder for " + componentClass.getName()
                    + " — no no-arg constructor; will use ComponentInjector fallback.");
            return;
        }
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

        // Only inject @Named fields from the class hierarchy up to SystemComponent (mirrors ComponentInjector behavior).
        // Unnamed @Inject fields and @PostConstruct are handled by NativeComponentInjector
        // after bind() returns, so they are not emitted here.
        List<Field> namedInjectFields = collectNamedInjectFields(componentClass);

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

                        // Inject @Named @Inject fields only — use map.get(name) directly.
                        // Unnamed @Inject fields are handled by NativeComponentInjector.inject().
                        for (Field f : namedInjectFields) {
                            // Use ofDescriptor to correctly handle array types (e.g. String[])
                            ClassDesc fieldTypeDesc = ClassDesc.ofDescriptor(f.getType().descriptorString());
                            String name = f.getAnnotation(Named.class).value();
                            // Use declaring class as putfield owner to avoid verifier errors
                            ClassDesc ownerDesc = ClassDesc.of(f.getDeclaringClass().getName());

                            cb.aload(2);        // component instance
                            cb.aload(1);        // Map arg
                            cb.ldc(name);
                            cb.invokeinterface(mapDesc, "get",
                                    MethodTypeDesc.of(CD_Object, CD_Object));
                            cb.checkcast(fieldTypeDesc);
                            cb.putfield(ownerDesc, f.getName(), fieldTypeDesc);
                        }

                        cb.aload(2);
                        cb.areturn();
                    }));
        });
    }

    /**
     * Collect only {@code @Named @Inject} fields from the class hierarchy up to SystemComponent,
     * matching {@code ComponentInjector}'s scope.  Unnamed {@code @Inject} fields are resolved
     * by type-scanning in {@code ComponentInjector.inject()}, which cannot be faithfully
     * replicated in generated bytecode without an invokedynamic type-match loop; those fields
     * are handled by the fallback reflection path in {@link NativeComponentInjector}.
     */
    private List<Field> collectNamedInjectFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class
                && !current.getName().equals("enkan.component.SystemComponent")) {
            for (Field f : current.getDeclaredFields()) {
                if (f.getAnnotation(Inject.class) != null && f.getAnnotation(Named.class) != null) {
                    result.add(f);
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

}
