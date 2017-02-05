package com.smithkatakkar.hauntingmod.testrunner.tweaker;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by ben on 01/31/17.
 */
public class Tweaker extends FMLTweaker {
    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        acceptOptions(new LinkedList<String>(), new File("."), null, null);
        FMLLaunchHandler.configureForClientLaunch(Launch.classLoader, this);
    }

    @Override
    public String getLaunchTarget() {
        return "com.smithkatakkar.hauntingmod.testrunner.MinecraftBootstrapper";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    @Override
    public void injectCascadingTweak(String tweakClassName) {}
}
