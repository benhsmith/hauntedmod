package com.smithkatakkar.hauntingmod;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = HauntingMod.MODID, version = HauntingMod.VERSION)
public class HauntingMod
{
    public static final String MODID = "hauntingmod";
    public static final String VERSION = "1.0";

    public static Item itemGhostGun;

    @SidedProxy(clientSide="com.smithkatakkar.hauntingmod.CombinedClientProxy",
                serverSide="com.smithkatakkar.hauntingmod.DedicatedServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        int id =0;
        EntityRegistry.registerModEntity(EntityGhost.class, "Ghost", id, this, 80, 1, true);//id is an internal mob id, you can start at 0 and continue adding them up.
        id++;

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new DeathHook());

        proxy.init_resources();
    }
}
