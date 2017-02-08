package com.smithkatakkar.hauntingmod.testrunner;

import net.minecraft.launchwrapper.Launch;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;

/**
 * Created by ben on 01/02/17.
 *
 * Inspired by https://github.com/vorburger/SwissKnightMinecraft
 */
public class MinecraftTestRunner extends BlockJUnit4ClassRunner {

    public MinecraftTestRunner(Class<?> clazz) throws InitializationError {
        super(getFromTestClassloader(clazz));
    }

    private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError {
        try {
            Launch.main(new String[] {"--tweakClass", "com.smithkatakkar.hauntingmod.testrunner.tweaker.Tweaker"});
            // This is *VERY* important, because without this
            // JUnit will be vey unhappy when in MinecraftTestRunner
            // we replace the class under test with the one from the
            // Minecraft ClassLoader instead of the parent one into
            // which JUnit Framework classes were already loaded
            Launch.classLoader.addClassLoaderExclusion("junit.");
            Launch.classLoader.addClassLoaderExclusion("org.junit.");

            Class<?> startupClass = Class.forName("mockit.internal.startup.Startup", false, Launch.classLoader);
            final Method initializeMethod = startupClass.getMethod("initializeIfPossible");
            initializeMethod.invoke(null);
        } catch (Throwable e) {
            throw new InitializationError(e);
        }
        return clazz;
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        try {
            ClassLoader classLoader = Launch.classLoader;
            Class<?> testClassFromMinecraftClassLoader = classLoader.loadClass(testClass.getName());
            return super.createTestClass(testClassFromMinecraftClassLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

