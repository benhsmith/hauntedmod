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
            if (entity instanceof EntityGhost && !((EntityGhost)entity).isDead) {
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
                60.0D, 60.0D, 10.0D), ghost);
        
        if (closestEntity != null) {
            Class avoidClass = ghost.getDeceasedEntity().getClass();
            if (avoidClass.isInstance(closestEntity)) {
                closestEntity.tasks.addTask(20, new EntityAIAvoidEntity<EntityGhost>((EntityCreature) closestEntity,
                    EntityGhost.class, new AvoidPredicate(avoidClass), 60.0F, 1.5D, 2.7D));
            }
        }
        
        return false;
    }

}
