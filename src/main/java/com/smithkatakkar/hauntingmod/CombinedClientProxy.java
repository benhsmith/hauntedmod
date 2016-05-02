package com.smithkatakkar.hauntingmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPig;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class CombinedClientProxy implements CommonProxy {
	@Override
	public void init_resources() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class,
				new RendererGhost(Minecraft.getMinecraft().getRenderManager(), new ModelPig(), 0.7F)
		);
	}
}
