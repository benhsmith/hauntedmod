package com.smithkatakkar.hauntingmod;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLLog;

public class DeathHook {

	/**
	 * @param args
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SubscribeEvent
	public void entityDeath(LivingDeathEvent event) {
		if(event.source.getEntity() instanceof EntityPlayer
			&& event.entity.isCreatureType(EnumCreatureType.CREATURE, false)) {
			//&& ! (event.entity instanceof EntityMob)
			//&& ! (event.entity instanceof IMob)) 
			
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			World world = player.worldObj;
			Vec3 pos = event.entity.getPositionVector();
			
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
