package com.smithkatakkar.hauntingmod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.base.Function;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIControlledByPlayer;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityGhost extends EntityTameable implements IEntityAdditionalSpawnData {
	private class EntityAIJumpTo extends EntityAIBase {
		static final float maxDist = 15.0f;
		EntityGhost ghost;
		Entity follow;
		
		EntityAIJumpTo(EntityGhost ghost, Entity follow) {
			this.ghost = ghost;
			this.follow = follow;
		}

		@Override
		public boolean shouldExecute() {
			if (ghost.getPositionVector().distanceTo(follow.getPositionVector()) > maxDist) {
				ghost.setPosition(follow.posX+follow.width+2, follow.posY+follow.width+2, follow.posZ);
			}
			
			return false;
		}
	}

	private class EntityAvoidOtherGhosts extends EntityAIBase {
		EntityGhost ghost;
		
		EntityAvoidOtherGhosts(EntityGhost ghost) {
			this.ghost = ghost;
	        this.setMutexBits(3);
		}
		
		@Override
		public boolean shouldExecute() {
			List entities = ghost.worldObj.getEntitiesWithinAABB(getClass(), ghost.getEntityBoundingBox().expand(6.0D, 2.0D, 6.0D));
			
			if (entities.size() > 0) {
				ghost.posX = ghost.prevPosX;
				ghost.posY = ghost.prevPosY;
				ghost.posZ = ghost.prevPosZ;
			}
			
			return false;
		}
	
	}
	
	public static final int ticksToExist = 10000;
	private EntityLiving deceasedEntity;
	
	public EntityGhost(World worldIn) {
		super(worldIn);
        setSize(0.9F, 0.9F);
        ((PathNavigateGround)this.getNavigator()).func_179690_a(true);

        //tasks.addTask(1, new EntityAvoidOtherGhosts(this));
        tasks.addTask(2, new EntityAIFollowOwner(this, 1.0D, 5.0F, 10.0F));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0f, 0.9f));
        tasks.addTask(4, new EntityAIScareTheLiving(this));
	}

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    public void setHauntee(Entity hauntee) {
		setOwnerId(hauntee.getUniqueID().toString());
        tasks.addTask(4, new EntityAIJumpTo(this, hauntee));   	
    }
    
	public void setDeceasedClass(Class<? extends EntityLiving> entity_class)
			throws NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<? extends EntityLiving> constr = entity_class.getConstructor( new Class[] {World.class} );
		deceasedEntity = constr.newInstance(this.worldObj);
		deceasedEntity.setPosition(posX, posY, posZ);
	}
	
	public EntityLivingBase getDeceasedEntity() {
		return deceasedEntity;
	}
	
	public boolean canBeCollidedWith() {
		return false;
	}
	
	public boolean canBePushed() {
		return false;
	}
	
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }
    
    public void fall(float distance, float damageMultiplier) {
    	
    }


	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onUpdate() {
		super.onUpdate();
		
		deceasedEntity.onUpdate();
		
    	if (ticksExisted > ticksToExist)
    		setDead();    	

	}
	
    protected void despawnEntity() {
    }
    
    @SideOnly(Side.CLIENT)
    public void func_180426_a(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
    	super.func_180426_a(p_180426_1_, p_180426_3_, p_180426_5_, p_180426_7_, p_180426_8_, p_180426_9_,  p_180426_10_);
    	deceasedEntity.func_180426_a(p_180426_1_, p_180426_3_, p_180426_5_, p_180426_7_, p_180426_8_, p_180426_9_,  p_180426_10_);
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
    	super.setVelocity(x,y,z);
    	deceasedEntity.setVelocity(x,y,z);
    }
    
    @SideOnly(Side.CLIENT)
    public void setAngles(float yaw, float pitch) {
    	super.setAngles(yaw, pitch);
    	deceasedEntity.setAngles(yaw, pitch);
    }

    public float getSwingProgress(float progress) {
    	deceasedEntity.getSwingProgress(progress);
    	return super.getSwingProgress(progress);
    }

    public void setRotationYawHead(float rotation) {
    	deceasedEntity.setRotationYawHead(rotation);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);

        if (deceasedEntity != null) {
        	tagCompound.setString("LivingClass", deceasedEntity.getClass().getCanonicalName());        	
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompound)
    {
        super.readEntityFromNBT(tagCompound);
        
        if (getOwnerEntity() == null) {
        	setDead();
        }
        if (tagCompound.hasKey("LivingClass")) {
        	String className = tagCompound.getString("LivingClass");
        	try {
        		setDeceasedClass((Class<? extends EntityLiving>)Class.forName(className));
			} catch (Exception e) {
				FMLLog.info(className + " - failed to reload ghost class - " + e.getMessage());
			} 
        }
    }

	@Override
	public void writeSpawnData(ByteBuf additionalData) {
		if (getOwnerEntity() != null) {
			additionalData.writeInt(getOwnerEntity().getEntityId());
		} else {
			additionalData.writeInt(0);
		}
        ByteBuf tmpBuf = Unpooled.buffer();
        PacketBuffer pb = new PacketBuffer(tmpBuf);
        pb.writeString(deceasedEntity.getClass().getCanonicalName());
		additionalData.writeBytes(pb);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		EntityLivingBase target = (EntityLivingBase) worldObj.getEntityByID(additionalData.readInt());
        if (target == null) {
        	setDead();
        } else {
        	setHauntee(target);
        }

        String className = new PacketBuffer(additionalData).readStringFromBuffer(additionalData.readableBytes());
    	try {
    		setDeceasedClass((Class<? extends EntityLiving>)Class.forName(className));
		} catch (Exception e) {
			FMLLog.info(className + " - failed to reload ghost class - " + e.getMessage());
		}
   	}
}
