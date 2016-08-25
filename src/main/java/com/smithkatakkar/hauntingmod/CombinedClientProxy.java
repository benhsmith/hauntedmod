package com.smithkatakkar.hauntingmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class CombinedClientProxy implements CommonProxy {
	@Override
    public void preInit() {
	}

	@Override
	public void init_resources() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class,
				new RenderGhost(Minecraft.getMinecraft().getRenderManager(), new ModelPig(), 0.7F)
		);

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                .register(HauntingMod.itemGhostGun, 0, new ModelResourceLocation(HauntingMod.MODID + ":ghost_gun", "inventory"));
	}
}
