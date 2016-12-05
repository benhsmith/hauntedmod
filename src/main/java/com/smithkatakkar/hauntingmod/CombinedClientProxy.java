package com.smithkatakkar.hauntingmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CombinedClientProxy implements CommonProxy {
	@Override
    public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class,
				new IRenderFactory<EntityGhost>() {
					@Override
					public Render createRenderFor(RenderManager manager) {
						return new RenderGhost(manager, new ModelPig(), 0.7F);
					}
				}
		);
	}

	@Override
	public void init_resources() {
	}
}
