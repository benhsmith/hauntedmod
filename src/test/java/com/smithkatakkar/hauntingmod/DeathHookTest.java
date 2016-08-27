package com.smithkatakkar.hauntingmod;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import org.junit.Test;

public class DeathHookTest {

    @Mocked World world;
    @Mocked EntityPlayer player;
    @Mocked EntityPig pig;
    @Mocked EntityGhost ghost;
	   
	@Test
	public void testEntityDeath() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		DamageSource source = DamageSource.causePlayerDamage(player);
		
		LivingDeathEvent event = new LivingDeathEvent(pig, source);
		
		new Expectations() {{
			pig.isCreatureType(EnumCreatureType.CREATURE, false); result = true;
			player.worldObj = world;
		}};
		
		DeathHook hook = new DeathHook();
		hook.entityDeath(event);
		
		new Verifications() {{
			ghost.setDeceasedClass(EntityPig.class);
			world.spawnEntityInWorld(withInstanceOf(EntityGhost.class));
		}};		
	}

}
