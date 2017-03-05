package com.smithkatakkar.hauntingmod;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGhost extends RenderLiving<EntityGhost> {
    private float red=0.75f;
    private float green=0.75f;
    private float blue=1.0f;
    private float alpha=0.70f;

    public RenderGhost(RenderManager renderManager, ModelBase model, float f) {
        super(renderManager, model, f);
    }

    @Override
    public void doRender(EntityGhost entity, double x, double y, double z, float f, float partialTicks) {
        EntityLivingBase deceased = ((EntityGhost)entity).getDeceasedEntity();
        if (deceased == null)
            return;
        Render render = renderManager.getEntityRenderObject(deceased);

        EntityLivingBase entity_living = (EntityLivingBase)entity;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float factor = (EntityGhost.ticksToExist-entity_living.ticksExisted)/(float)EntityGhost.ticksToExist;
        GlStateManager.color(red, green, blue, alpha*factor);

        render.doRender(deceased, x, y, z, f, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGhost entity) {
        return null;
    }
}
