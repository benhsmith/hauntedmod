package com.smithkatakkar.hauntingmod.testrunner;

import net.minecraft.init.Bootstrap;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ben on 31/01/17.
 */
public class MinecraftBootstrapper {
    public static void main(String [] args) {
        Loader.injectData(
            String.valueOf(ForgeVersion.majorVersion),
            String.valueOf(ForgeVersion.minorVersion),
            String.valueOf(ForgeVersion.revisionVersion),
            String.valueOf(ForgeVersion.buildVersion),
            ForgeVersion.mcVersion,
            ForgeVersion.mcpVersion,
            new File("."),
            new ArrayList<String>()
        );

        Bootstrap.register();
    }

    private void init() {
        Bootstrap.register();
    }
}
