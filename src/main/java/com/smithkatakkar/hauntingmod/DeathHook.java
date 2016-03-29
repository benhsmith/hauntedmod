package com.smithkatakkar.hauntingmod;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DeathHook {

	/**
	 * @param args
	 */
	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		if(event.source.getEntity() instanceof EntityPlayer
			&& event.entity instanceof EntityPig) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			World world = player.worldObj;
			Vec3 pos = event.entity.getPositionVector();
			
			EntityGhost ghost = new EntityGhost(world);
			ghost.setPosition(event.entity.posX, event.entity.posY, event.entity.posZ);
			ghost.setAttackTarget(player);
			world.spawnEntityInWorld(ghost);
		}
	}
}
