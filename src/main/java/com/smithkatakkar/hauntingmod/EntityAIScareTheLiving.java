package com.smithkatakkar.hauntingmod;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.DamageSource;

public class EntityAIScareTheLiving extends EntityAIBase {

	class AvoidPredicate implements Predicate {
		Class avoid;
		
		public AvoidPredicate(Class class1) {
			this.avoid = class1;
		}
		
        public boolean apply(Object entity)
        {
        	if (entity instanceof EntityGhost) {
        		return avoid.isInstance(((EntityGhost)entity).getDeceasedEntity());
        	}
        	
        	return false;
        }		
	}
	
	private EntityGhost ghost;
	
	EntityAIScareTheLiving(EntityGhost ghost) {
		this.ghost = ghost;
	}
	
	@Override
	public boolean shouldExecute() {
        EntityLiving closestEntity = (EntityLiving)this.ghost.worldObj.findNearestEntityWithinAABB(
        	ghost.getDeceasedEntity().getClass(), ghost.getEntityBoundingBox().expand(
        		(double)30.0D, 30.0D, (double)2.0D), ghost);
        
        if (closestEntity != null) {
        	Class avoidClass = ghost.getDeceasedEntity().getClass();
        	if (avoidClass.isInstance(closestEntity)) {
	        	closestEntity.tasks.addTask(20, new EntityAIAvoidEntity((EntityCreature) closestEntity,
	        		new AvoidPredicate(avoidClass), 30.0F, 1.33D, 1.33D));
        	}
        }
        
        return false;
	}

}
