package kotowari.graalvm;

import kotowari.routing.Route;
import kotowari.routing.Routes;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.constant.ConstantDescs.*;

/**
 * GraalVM {@link Feature} that generates a reflection-free {@code KotowariDispatcher} class
 * at native image build time.
 *
 * <p>At build time this Feature:
 * <ol>
 *   <li>Resolves all registered {@link Routes} via {@link RouteRegistry} (or the
 *       {@code kotowari.routes.factory} system property).</li>
 *   <li>Extracts controller class names and action names from the route list.</li>
 *   <li>Registers all controller classes and their action methods for reflection
 *       (needed to instantiate controllers and resolve {@link kotowari.inject.ParameterInjector}s).</li>
 *   <li>Generates a {@code KotowariDispatcher} class (using the Class File API) with a
 *       {@code dispatch(String, Object, Object[])} method that dispatches via a series of
 *       string equality checks and direct {@code invokevirtual} call sites.</li>
 * </ol>
 *
 * <p>Usage — in your application:
 * <pre>{@code
 * // Option A: static registry
 * RouteRegistry.register(Routes.define(r -> { ... }).compile());
 *
 * // Option B: system property at native-image build time
 * // -Dkotowari.routes.factory=com.example.AppRoutes
 * // where AppRoutes has: public static Routes routes() { ... }
 * }</pre>
 */
public class KotowariFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        Routes routes = resolveRoutes(access);
        if (routes == null) {
            return;
        }

        List<RouteEntry> entries = extractEntries(routes);
        if (entries.isEmpty()) {
            return;
        }

        registerControllerReflection(access, entries);

        byte[] dispatcherBytes = generateDispatcher(entries);
        Class<?> dispatcherClass = defineDispatcherClass(dispatcherBytes);
        access.registerAsUsed(dispatcherClass);
        // Register the dispatcher for reflection so NativeControllerInvokerMiddleware
        // can look it up via Class.forName + getMethod at runtime.
        RuntimeReflection.register(dispatcherClass);
        try {
            RuntimeReflection.register(dispatcherClass.getMethod(
                    "dispatch", String.class, Object.class, Object[].class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register KotowariDispatcher.dispatch", e);
        }
    }

    private Routes resolveRoutes(BeforeAnalysisAccess access) {
        Routes routes = RouteRegistry.get();
        if (routes != null) {
            return routes;
        }

        String factoryClassName = System.getProperty("kotowari.routes.factory");
        if (factoryClassName == null) {
            return null;
        }

        try {
            Class<?> factoryClass = access.findClassByName(factoryClassName);
            if (factoryClass == null) {
                return null;
            }
            Method routesMethod = factoryClass.getMethod("routes");
            return (Routes) routesMethod.invoke(null);
        } catch (Exception e) {
            System.err.println("[KotowariFeature] Could not invoke routes factory: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<RouteEntry> extractEntries(Routes routes) {
        List<RouteEntry> entries = new ArrayList<>();
        try {
            Field routeListField = Routes.class.getDeclaredField("routeList");
            routeListField.setAccessible(true);
            List<Route> routeList = (List<Route>) routeListField.get(routes);
            for (Route route : routeList) {
                Field constraintsField = Route.class.getDeclaredField("constraints");
                constraintsField.setAccessible(true);
                enkan.collection.OptionMap constraints =
                        (enkan.collection.OptionMap) constraintsField.get(route);
                String controllerName = constraints.getString("controller");
                String action = constraints.getString("action");
                if (controllerName != null && action != null) {
                    entries.add(new RouteEntry(controllerName, action));
                }
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("[KotowariFeature] Could not extract route list: " + e.getMessage());
        }
        return entries;
    }

    private void registerControllerReflection(BeforeAnalysisAccess access, List<RouteEntry> entries) {
        for (RouteEntry entry : entries) {
            Class<?> ctrl = access.findClassByName(entry.controllerClassName());
            if (ctrl == null) {
                continue;
            }
            RuntimeReflection.register(ctrl);
            RuntimeReflection.registerAllConstructors(ctrl);
            // Register @Inject fields and @PostConstruct methods so ComponentInjector
            // can wire the controller instance in native mode.
            RuntimeReflection.registerAllDeclaredFields(ctrl);
            RuntimeReflection.registerAllDeclaredMethods(ctrl);
        }
    }

    /**
     * Generate a {@code KotowariDispatcher} class with the Class File API.
     *
     * <p>The generated class has a single static method:
     * <pre>{@code
     * public static Object dispatch(String key, Object controller, Object[] args)
     * }</pre>
     *
     * The method body is an if-chain over the {@code key} string (Controller#action format),
     * each branch casting {@code controller} to the concrete type and calling the action method
     * via {@code invokevirtual}.
     */
    private byte[] generateDispatcher(List<RouteEntry> entries) {
        ClassDesc dispatcherDesc = ClassDesc.of("kotowari.graalvm.KotowariDispatcher");
        ClassDesc objectArrayDesc = CD_Object.arrayType();

        return ClassFile.of().build(dispatcherDesc, classBuilder -> {
            classBuilder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL);

            classBuilder.withMethod("dispatch",
                    MethodTypeDesc.of(CD_Object, CD_String, CD_Object, objectArrayDesc),
                    ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                    methodBuilder -> methodBuilder.withCode(codeBuilder -> {
                        buildDispatchBody(codeBuilder, entries, dispatcherDesc);
                    }));
        });
    }

    private void buildDispatchBody(CodeBuilder code, List<RouteEntry> entries, ClassDesc dispatcherDesc) {
        for (RouteEntry entry : entries) {
            try {
                Class<?> ctrlClass = Class.forName(entry.controllerClassName());
                // Find the action method
                Method actionMethod = null;
                for (Method m : ctrlClass.getDeclaredMethods()) {
                    if (m.getName().equals(entry.actionName()) && Modifier.isPublic(m.getModifiers())) {
                        actionMethod = m;
                        break;
                    }
                }
                if (actionMethod == null) {
                    continue;
                }

                String key = entry.controllerClassName() + "#" + entry.actionName();
                ClassDesc ctrlDesc = ClassDesc.of(entry.controllerClassName());

                // Emit: if (key.equals(arg0)) { return ((CtrlClass)arg1).action(args); }
                var skipLabel = code.newLabel();
                code.aload(0);                              // load key arg
                code.ldc(key);                              // push literal key
                code.invokevirtual(CD_String,
                        "equals",
                        MethodTypeDesc.of(CD_boolean, CD_Object));
                code.ifeq(skipLabel);                       // skip if not equal

                code.aload(1);
                code.checkcast(ctrlDesc);

                // Load arguments from Object[] arg2
                Parameter[] params = actionMethod.getParameters();
                for (int i = 0; i < params.length; i++) {
                    code.aload(2);
                    code.ldc(i);
                    code.aaload();
                    Class<?> paramType = params[i].getType();
                    if (paramType.isPrimitive()) {
                        // unbox: checkcast wrapper, then invoke XxxValue()
                        code.checkcast(wrapperDesc(paramType));
                        emitUnbox(code, paramType);
                    } else {
                        code.checkcast(toClassDesc(paramType));
                    }
                }

                MethodTypeDesc actionDesc = buildMethodTypeDesc(actionMethod);
                code.invokevirtual(ctrlDesc, actionMethod.getName(), actionDesc);

                // Box primitive return or push null for void
                Class<?> returnType = actionMethod.getReturnType();
                if (returnType == void.class) {
                    code.aconst_null();
                } else if (returnType.isPrimitive()) {
                    emitBox(code, returnType);
                }
                code.areturn();

                code.labelBinding(skipLabel);

            } catch (ClassNotFoundException e) {
                // Controller class not on build-time classpath; skip
            }
        }

        // Fallback: throw IllegalArgumentException
        code.new_(ClassDesc.of("java.lang.IllegalArgumentException"));
        code.dup();
        code.aload(0);
        code.invokespecial(ClassDesc.of("java.lang.IllegalArgumentException"),
                INIT_NAME,
                MethodTypeDesc.of(CD_void, CD_String));
        code.athrow();
    }

    private MethodTypeDesc buildMethodTypeDesc(Method method) {
        ClassDesc returnDesc = mapType(method.getReturnType());
        ClassDesc[] paramDescs = new ClassDesc[method.getParameterCount()];
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            paramDescs[i] = mapType(paramTypes[i]);
        }
        return MethodTypeDesc.of(returnDesc, paramDescs);
    }

    private ClassDesc mapType(Class<?> type) {
        if (type == void.class) return CD_void;
        if (type == int.class) return CD_int;
        if (type == long.class) return CD_long;
        if (type == boolean.class) return CD_boolean;
        if (type == double.class) return CD_double;
        if (type == float.class) return CD_float;
        if (type == byte.class) return CD_byte;
        if (type == short.class) return CD_short;
        if (type == char.class) return CD_char;
        return toClassDesc(type);
    }

    /** Convert a reference or array type to {@link ClassDesc} using the descriptor string. */
    private ClassDesc toClassDesc(Class<?> type) {
        // type.descriptorString() works for all types: reference, array, and primitive
        return ClassDesc.ofDescriptor(type.descriptorString());
    }

    /** Return the {@link ClassDesc} of the wrapper type for a primitive. */
    private ClassDesc wrapperDesc(Class<?> primitive) {
        if (primitive == int.class)     return ClassDesc.of("java.lang.Integer");
        if (primitive == long.class)    return ClassDesc.of("java.lang.Long");
        if (primitive == boolean.class) return ClassDesc.of("java.lang.Boolean");
        if (primitive == double.class)  return ClassDesc.of("java.lang.Double");
        if (primitive == float.class)   return ClassDesc.of("java.lang.Float");
        if (primitive == byte.class)    return ClassDesc.of("java.lang.Byte");
        if (primitive == short.class)   return ClassDesc.of("java.lang.Short");
        if (primitive == char.class)    return ClassDesc.of("java.lang.Character");
        throw new IllegalArgumentException("Not a primitive: " + primitive);
    }

    /** Emit an unboxing call (e.g. {@code Integer.intValue()}) for the given primitive type. */
    private void emitUnbox(CodeBuilder code, Class<?> primitive) {
        String method;
        ClassDesc wrapDesc = wrapperDesc(primitive);
        ClassDesc primDesc = mapType(primitive);
        if (primitive == int.class)     method = "intValue";
        else if (primitive == long.class)    method = "longValue";
        else if (primitive == boolean.class) method = "booleanValue";
        else if (primitive == double.class)  method = "doubleValue";
        else if (primitive == float.class)   method = "floatValue";
        else if (primitive == byte.class)    method = "byteValue";
        else if (primitive == short.class)   method = "shortValue";
        else if (primitive == char.class)    method = "charValue";
        else throw new IllegalArgumentException("Not a primitive: " + primitive);
        code.invokevirtual(wrapDesc, method, MethodTypeDesc.of(primDesc));
    }

    /** Emit a boxing call (e.g. {@code Integer.valueOf(int)}) for the given primitive type. */
    private void emitBox(CodeBuilder code, Class<?> primitive) {
        ClassDesc wrapDesc = wrapperDesc(primitive);
        ClassDesc primDesc = mapType(primitive);
        code.invokestatic(wrapDesc, "valueOf", MethodTypeDesc.of(wrapDesc, primDesc));
    }

    private Class<?> defineDispatcherClass(byte[] bytes) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            return lookup.defineClass(bytes);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to define KotowariDispatcher", e);
        }
    }

    record RouteEntry(String controllerClassName, String actionName) {}
}
