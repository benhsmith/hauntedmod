package com.smithkatakkar.hauntingmod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
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
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityGhost extends EntityMob implements IEntityAdditionalSpawnData {
	private class EntityAIFollow extends EntityAIBase {
		static final float maxDist = 40.0f;
		static final float minDist = 5.0f;
		static final float walkSpeed = 2.0f;
		EntityGhost ghost;
		Entity follow;
		
		EntityAIFollow(EntityGhost ghost, Entity follow) {
			this.ghost = ghost;
			this.follow = follow;
		}

		@Override
		public boolean shouldExecute() {
			double dist = ghost.getPositionVector().distanceTo(follow.getPositionVector());
			if (dist > maxDist && follow.onGround) {
				ghost.posX = follow.posX+follow.width+2;
				ghost.posY = follow.posY;
				ghost.posZ = follow.posZ+follow.width+2;
				ghost.setPosition(ghost.posX, ghost.posY, ghost.posZ);
			} else if (dist > minDist && dist <= maxDist) {
				return true;
			}
			
			return false;
		}
		
	    public boolean continueExecuting()
	    {
	        return !ghost.getNavigator().noPath() && ghost.getDistanceSqToEntity(follow) > (double)(this.maxDist * this.maxDist);
	    }

	    /**
	     * Execute a one shot task or start executing a continuous task
	     */
	    public void startExecuting()
	    {
	        ((PathNavigateGround)ghost.getNavigator()).func_179690_a(false);
	    }

	    /**
	     * Resets the task
	     */
	    public void resetTask()
	    {
	        ghost.getNavigator().clearPathEntity();
	        ((PathNavigateGround)ghost.getNavigator()).func_179690_a(true);
	    }
	    
	    public void updateTask()
	    {
	        ghost.getLookHelper().setLookPositionWithEntity(follow, 10.0F, (float)ghost.getVerticalFaceSpeed());

	        if (!ghost.getNavigator().tryMoveToEntityLiving(follow, walkSpeed)) {
				ghost.posX = follow.posX+follow.width+2;
				ghost.posY = follow.posY;
				ghost.posZ = follow.posZ+follow.width+2;
				ghost.setPosition(ghost.posX, ghost.posY, ghost.posZ);	        	
	        }
	    }
	}

	private class EntityAvoidOtherGhosts extends EntityAIBase {
		EntityGhost ghost;
	    /** The PathEntity of our entity */
	    private PathEntity entityPathEntity;
		
		EntityAvoidOtherGhosts(EntityGhost ghost) {
			this.ghost = ghost;
	        this.setMutexBits(3);
		}
		
		@Override
		public boolean shouldExecute() {
			List entities = ghost.worldObj.getEntitiesWithinAABB(EntityGhost.class, ghost.getEntityBoundingBox().expand(0.5D, 0.5D, 0.5D));
			
			if (entities.size() > 1) {
				Entity otherGhost = null;
				for(Iterator it = entities.iterator(); it.hasNext();) {
					 Entity entity = (Entity) it.next();
					 if (!entity.equals(ghost)) {
						 otherGhost = ghost;
						 break;
					 }
				}
				Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(ghost, 1, 1, new Vec3(otherGhost.posX, otherGhost.posY, otherGhost.posZ));
				
	            if (vec3 != null)
	            {
	                entityPathEntity = ghost.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
	                return entityPathEntity == null ? false : entityPathEntity.isDestinationSame(vec3);
	            }            
			}
			
			return false;
		}

	    public void startExecuting()
	    {
	    	ghost.getNavigator().setPath(this.entityPathEntity, 1.0D);
	    }

		@Override
		public boolean continueExecuting() {
	        return !ghost.getNavigator().noPath();
		}
	}
	
	public static final int ticksToExist = 20000;
	private EntityLiving deceasedEntity;
	UUID haunteeUUID;
	
	public EntityGhost(World worldIn) {
		super(worldIn);
        setSize(0.9F, 0.9F);
        //noClip = true;
        ((PathNavigateGround)this.getNavigator()).func_179690_a(true);

        tasks.addTask(1, new EntityAvoidOtherGhosts(this));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0f, 0.9f));
        tasks.addTask(4, new EntityAIScareTheLiving(this));
	}

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    public void setHauntee(EntityLivingBase hauntee) {
		this.setAttackTarget(hauntee);
        tasks.addTask(4, new EntityAIFollow(this, hauntee));
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

    protected void func_180433_a(double p_180433_1_, boolean p_180433_3_, Block p_180433_4_, BlockPos p_180433_5_) {}

	public void onUpdate() {
		super.onUpdate();
		
		if (haunteeUUID != null && getAttackTarget() == null) {
			Entity entity = ((WorldServer)worldObj).getEntityFromUuid(haunteeUUID);
			if (entity != null) {
				setHauntee((EntityLivingBase)entity);
			}
		}
		
		deceasedEntity.onUpdate();
		
		//deceasedEntity.motionY *= 0.6D;
		//motionY *= .06D;
				
    	if (ticksExisted > ticksToExist)
    		setDead();    
    	
    	isJumping = false;
    	deceasedEntity.setJumping(false);
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

        if (getAttackTarget() != null) {
        	tagCompound.setString("TargetUUID", getAttackTarget().getUniqueID().toString());
	    } else {	
        	tagCompound.setString("TargetUUID", "");
	    }
        
        if (deceasedEntity != null) {
        	tagCompound.setString("LivingClass", deceasedEntity.getClass().getCanonicalName());        	
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompound)
    {
        super.readEntityFromNBT(tagCompound);
        
        haunteeUUID = UUID.fromString(tagCompound.getString("TargetUUID"));
        
        if (haunteeUUID == null) {
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
		/*
		if (getAttackTarget() != null) {
			additionalData.writeInt(getAttackTarget().getEntityId());
		} else {
			additionalData.writeInt(0);
		}
		*/
        ByteBuf tmpBuf = Unpooled.buffer();
        PacketBuffer pb = new PacketBuffer(tmpBuf);
        pb.writeString(deceasedEntity.getClass().getCanonicalName());
		additionalData.writeBytes(pb);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		/*
		EntityLivingBase target = (EntityLivingBase) worldObj.getEntityByID(additionalData.readInt());
        if (target == null) {
        	setDead();
        } else {
        }
		 */
        String className = new PacketBuffer(additionalData).readStringFromBuffer(additionalData.readableBytes());
    	try {
    		setDeceasedClass((Class<? extends EntityLiving>)Class.forName(className));
		} catch (Exception e) {
			FMLLog.info(className + " - failed to reload ghost class - " + e.getMessage());
		}
   	}
	
	@Override
    public boolean getCanSpawnHere() {
		return true;
	}
}
