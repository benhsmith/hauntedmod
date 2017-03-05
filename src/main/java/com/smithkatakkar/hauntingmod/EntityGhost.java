package com.smithkatakkar.hauntingmod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityGhost extends EntityMob implements IEntityAdditionalSpawnData {
    private class EntityAIFollow extends EntityAIBase {
        static final float maxDist = 40.0f;
        static final float minDist = 5.0f;
        static final float walkSpeed = 2.0f;
        EntityGhost ghost;
        EntityLivingBase follow;

        EntityAIFollow(EntityGhost ghost, EntityLivingBase follow) {
            this.ghost = ghost;
            this.follow = follow;
        }

        @Override
        public boolean shouldExecute() {
            double dist = ghost.getPositionVector().distanceTo(follow.getPositionVector());
            if (dist > maxDist && follow.onGround) {
                ghost.posX = follow.posX + follow.width + 2;
                ghost.posY = follow.posY;
                ghost.posZ = follow.posZ + follow.width + 2;
                ghost.setPosition(ghost.posX, ghost.posY, ghost.posZ);
            } else if (dist > minDist && dist <= maxDist) {
                return true;
            }

            return false;
        }

        public boolean continueExecuting()
        {
            return !ghost.getNavigator().noPath() && ghost.getDistanceSqToEntity(follow) > (double)(maxDist * maxDist);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting()
        {
        }

        /**
         * Resets the task
         */
        public void resetTask()
        {
            ghost.getNavigator().clearPathEntity();
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
        private Path entityPathEntity;

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
                     if (!entity.equals(ghost) && entity instanceof EntityGhost) {
                         otherGhost = ghost;
                         break;
                     }
                }
                Vec3d vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(ghost, 1, 1, new Vec3d(otherGhost.posX, otherGhost.posY, otherGhost.posZ));

                if (vec3 != null)
                {
                    entityPathEntity = ghost.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                    return entityPathEntity != null;
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

    private class EntityGhostAttack extends EntityAINearestAttackableTarget<EntityPlayer> {
        public EntityGhostAttack(EntityGhost ghost) {
            super(ghost, EntityPlayer.class, true);
        }

        public boolean shouldExecute()
        {
            if (taskOwner.getEntityWorld().isDaytime()) {
                return false;
            }

            return super.shouldExecute();
        }
    }

    public static final int ticksToExist = 20000;
    private EntityLiving deceasedEntity;
    private UUID haunteeUUID;

    public EntityGhost(World worldIn) {
        super(worldIn);
        setSize(0.9F, 0.9F);

        tasks.addTask(1, new EntityAvoidOtherGhosts(this));
        tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0f, 0.9f));
        tasks.addTask(4, new EntityAIScareTheLiving(this));
        tasks.addTask(5, new EntityGhostAttack(this));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    public void setHauntee(EntityPlayer hauntee) {
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
            Entity entity =  getEntityWorld().getPlayerEntityByUUID(haunteeUUID);
            if (entity != null) {
                setHauntee((EntityPlayer)entity);
            }
        }

        deceasedEntity.onUpdate();

        deceasedEntity.motionY *= 0.6D;
        motionY *= .06D;

        if (ticksExisted > ticksToExist)
            setDead();

        isJumping = false;
        deceasedEntity.setJumping(false);
    }

    protected void despawnEntity() {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x,y,z);
        deceasedEntity.setVelocity(x,y,z);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void setAngles(float yaw, float pitch) {
        super.setAngles(yaw, pitch);
        deceasedEntity.setAngles(yaw, pitch);
    }

    @Override
    public float getSwingProgress(float progress) {
        deceasedEntity.getSwingProgress(progress);
        return super.getSwingProgress(progress);
    }

    @Override
    public void setRotationYawHead(float rotation) {
        deceasedEntity.setRotationYawHead(rotation);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        if (getAttackTarget() != null) {
            tagCompound.setString("TargetUUID", getAttackTarget().getUniqueID().toString());
        } else {
            tagCompound.setString("TargetUUID", "");
        }
        
        if (deceasedEntity != null) {
            tagCompound.setString("LivingClass", deceasedEntity.getClass().getCanonicalName());
        }

        return super.writeToNBT(tagCompound);
    }

    public void readFromNBT(NBTTagCompound tagCompound)
    {
        haunteeUUID = UUID.fromString(tagCompound.getString("TargetUUID"));

        if (tagCompound.hasKey("LivingClass")) {
            String className = tagCompound.getString("LivingClass");
            try {
                setDeceasedClass((Class<? extends EntityLiving>)Class.forName(className));
            } catch (Exception e) {
                FMLLog.info(className + " - failed to reload ghost class - " + e.getMessage());
            }
        }

        super.readFromNBT(tagCompound);
    }

    @Override
    public void writeSpawnData(ByteBuf additionalData) {
        ByteBuf tmpBuf = Unpooled.buffer();
        PacketBuffer pb = new PacketBuffer(tmpBuf);
        pb.writeString(deceasedEntity.getClass().getCanonicalName());
        additionalData.writeBytes(pb);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
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
