package com.smithkatakkar.hauntingmod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;


public class EntityGhost extends EntityPig {

	private Class entity_class;
	
	public EntityGhost(World worldIn) {
		super(worldIn);
        this.setSize(0.9F, 0.9F);
        this.tasks.addTask(5, new EntityAIMoveTowardsTarget(this, 1.5d, 100.0F));
	}

	public void setEntityClass(Class entity_class) {
		this.entity_class = entity_class;
	}
	
	public Class getEntityClass() {
		return entity_class;
	}
	
	public boolean canBeCollidedWith() {
		return false;
	}
	
	public boolean canBePushed() {
		return false;
	}
	
    public boolean isEntityInvulnerable(DamageSource source)
    {
        return true;
    }
/*
	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		// TODO Auto-generated method stub
		return null;
	}
	*/
    
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);

        if (getAttackTarget() != null) {
        	tagCompound.setInteger("Target", getAttackTarget().getEntityId());
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        
        if (tagCompund.hasKey("Target", 10))
        {
            EntityLivingBase target = (EntityLivingBase) worldObj.getEntityByID(tagCompund.getInteger("Target"));
            this.setAttackTarget(target);
        }
    }
}
