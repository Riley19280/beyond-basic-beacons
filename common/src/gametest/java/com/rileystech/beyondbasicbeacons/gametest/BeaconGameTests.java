package com.rileystech.beyondbasicbeacons.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

// The single list of GameTests shared by every loader. Each loader's gametest glue registers
// `function()` into the vanilla TEST_FUNCTION registry and `instance()` into TEST_INSTANCE.
public final class BeaconGameTests {
    public static final String NAMESPACE = "beyondbasicbeacons_gametest";
    // An empty 8x8x8 area (src/gametest/resources/data/<NAMESPACE>/structure/empty.nbt).
    public static final Identifier EMPTY_STRUCTURE = Identifier.fromNamespaceAndPath(NAMESPACE, "empty");

    private static final List<Class<?>> TEST_CLASSES = List.of(
            BeaconBaseGameTests.class,
            BeaconEffectsGameTests.class,
            BeaconPacketGameTests.class,
            BeaconPersistenceGameTests.class,
            DatapackLoadingGameTests.class
    );

    public record Test(Identifier id, int maxTicks, Consumer<GameTestHelper> function) {
        public GameTestInstance instance(Holder<TestEnvironmentDefinition<?>> environment) {
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    environment, EMPTY_STRUCTURE, this.maxTicks, 0, true, Rotation.NONE,
                    false, 1, 1, false, 1);
            return new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, this.id), data);
        }
    }

    private BeaconGameTests() {
    }

    // The shared tests plus any loader-only test classes (e.g. for loader-specific menu wiring).
    // Each loader calls this once and keeps the result.
    public static List<Test> collect(Class<?>... loaderTestClasses) {
        List<Class<?>> classes = new ArrayList<>(TEST_CLASSES);
        classes.addAll(List.of(loaderTestClasses));
        List<Test> found = new ArrayList<>();
        for (Class<?> testClass : classes) {
            Object instance = newInstance(testClass);
            List<Method> methods = new ArrayList<>(List.of(testClass.getDeclaredMethods()));
            methods.sort(Comparator.comparing(Method::getName));
            for (Method method : methods) {
                BeaconGameTest annotation = method.getAnnotation(BeaconGameTest.class);
                if (annotation == null) {
                    continue;
                }
                validate(method);
                Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE,
                        toSnakeCase(testClass.getSimpleName()) + "_" + toSnakeCase(method.getName()));
                found.add(new Test(id, annotation.maxTicks(), helper -> invoke(method, instance, helper)));
            }
        }
        if (found.isEmpty()) {
            throw new IllegalStateException("No @BeaconGameTest methods found");
        }
        return List.copyOf(found);
    }

    private static void validate(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1 || method.getParameterTypes()[0] != GameTestHelper.class) {
            throw new IllegalStateException("@BeaconGameTest method " + method
                    + " must be public, non-static, and take a single GameTestHelper");
        }
    }

    private static Object newInstance(Class<?> testClass) {
        try {
            return testClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create GameTest class " + testClass.getName(), e);
        }
    }

    private static void invoke(Method method, Object instance, GameTestHelper helper) {
        try {
            method.invoke(instance, helper);
        } catch (InvocationTargetException e) {
            // Rethrow unwrapped so the framework sees the real GameTestAssertException.
            if (e.getTargetException() instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (e.getTargetException() instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toSnakeCase(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
