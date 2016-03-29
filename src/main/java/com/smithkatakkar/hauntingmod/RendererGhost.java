package com.smithkatakkar.hauntingmod;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class RendererGhost extends RenderPig {
	private float red=0.75f;
	private float green=0.75f;
	private float blue=1.00f;
	private float alpha=0.40f;

	public RendererGhost(RenderManager renderManager, ModelBase model, float f) {
		super(renderManager, model, f);
	}

	public void doRender(Entity entity, double x, double y, double z, float f, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GlStateManager.color(red, green, blue, alpha);

		//Class entityClass = ((EntityGhost)entity).getEntityClass();
		//Render render = this.renderManager.getEntityClassRenderObject(entityClass);
		super.doRender(entity, x, y, z, f, partialTicks);
		
		GlStateManager.disableBlend();
    }
}
