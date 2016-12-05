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

	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		if(event.getSource().getEntity() instanceof EntityPlayer
			&& (event.getEntity().isCreatureType(EnumCreatureType.CREATURE, false)
				|| event.getEntity() instanceof EntityVillager)) {

			EntityPlayer player = (EntityPlayer) event.getSource().getEntity();
			World world = player.worldObj;

			EntityGhost ghost;
			try {
				ghost = new EntityGhost(world);
				ghost.setDeceasedClass((Class<? extends EntityLiving>)event.getEntity().getClass());
				ghost.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
				ghost.setHauntee(player);
				world.spawnEntityInWorld(ghost);
			} catch (Exception e) {
				FMLLog.info(event.getEntity().getClass() + " - failed to generate ghost - " + e.getMessage());
			}
		}
	}
}
