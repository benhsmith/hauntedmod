package com.smithkatakkar.hauntingmod;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLLog;

public class DeathHook {

	/**
	 * @param args
	 */
	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		if(event.source.getEntity() instanceof EntityPlayer
			&& (event.entity.isCreatureType(EnumCreatureType.CREATURE, false)
				|| event.entity instanceof EntityVillager)) {
			
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			World world = player.worldObj;
			
			EntityGhost ghost;
			try {
				ghost = new EntityGhost(world);
				ghost.setDeceasedClass((Class<? extends EntityLiving>)event.entity.getClass());
				ghost.setPosition(event.entity.posX, event.entity.posY, event.entity.posZ);
				ghost.setHauntee(player);
				world.spawnEntityInWorld(ghost);
			} catch (Exception e) {
				FMLLog.info(event.entity.getClass() + " - failed to generate ghost - " + e.getMessage());
			} 
		}
	}
}
