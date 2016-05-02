package com.smithkatakkar.hauntingmod;

import com.google.common.base.Function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

@Mod(modid = HauntingMod.MODID, version = HauntingMod.VERSION)
public class HauntingMod
{
	@SidedProxy(clientSide="com.smithkatakkar.hauntingmod.CombinedClientProxy",
                serverSide="com.smithkatakkar.hauntingmod.DedicatedServerProxy")
	public static CommonProxy proxy;

	
    public static final String MODID = "hauntingmod";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	int id =0;
    	EntityRegistry.registerModEntity(EntityGhost.class, "Ghost", id, this, 80, 1, true);//id is an internal mob id, you can start at 0 and continue adding them up.
        id++;
        
        EntityRegistration registration = EntityRegistry.instance().lookupModSpawn(EntityGhost.class, true);
        
        MinecraftForge.EVENT_BUS.register(new DeathHook());
        proxy.init_resources();
    }
}
