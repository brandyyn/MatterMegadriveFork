package matteroverdrive.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.util.math.MOMathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class MOPhysicsHelper {
    public static boolean insideBounds(Vec3 pos, AxisAlignedBB bounds) {
        return bounds.minX <= pos.xCoord && bounds.minY <= pos.yCoord && bounds.minZ <= pos.zCoord && bounds.maxX >= pos.xCoord && bounds.maxY >= pos.yCoord && bounds.maxZ >= pos.zCoord;
    }

    public static MovingObjectPosition rayTrace(EntityLivingBase viewer, World world, double distance, float ticks, Vec3 offset) {
        return rayTrace(viewer, world, distance, ticks, offset, false, false);
    }

    public static MovingObjectPosition rayTrace(EntityLivingBase viewer, World world, double distance, float ticks, Vec3 offset, boolean checkBlockCollision, boolean onlySolid) {
        return rayTrace(viewer, world, distance, ticks, offset, checkBlockCollision, onlySolid, null);
    }

    public static MovingObjectPosition rayTrace(EntityLivingBase viewer, World world, double distance, float ticks, Vec3 offset, boolean checkBlockCollision, boolean onlySolid, Vec3 dir) {
        return rayTrace(getPosition(viewer, ticks), world, distance, ticks, offset, checkBlockCollision, onlySolid, dir, viewer);
    }

    public static MovingObjectPosition rayTrace(Vec3 fromPos, World world, double distance, float ticks, Vec3 offset, boolean checkBlockCollision, boolean onlySolid, Vec3 dir, EntityLivingBase viewer) {
        MovingObjectPosition objectMouseOver = null;
        Entity pointedEntity = null;

        if (world != null) {
            if (dir == null)
                dir = viewer.getLook(ticks);

            double d0 = distance;
            objectMouseOver = MOPhysicsHelper.rayTraceForBlocks(fromPos, world, d0, ticks, offset, checkBlockCollision, onlySolid, dir);
            double d1 = d0;
            Vec3 vec3 = fromPos;
            if (offset != null)
                vec3 = vec3.addVector(offset.xCoord, offset.yCoord, offset.zCoord);

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec32 = vec3.addVector(dir.xCoord * d0, dir.yCoord * d0, dir.zCoord * d0);
            Vec3 vec33 = null;
            float f1 = 1.0F;
            List list = world.getEntitiesWithinAABBExcludingEntity(viewer, viewer.boundingBox.addCoord(dir.xCoord * d0, dir.yCoord * d0, dir.zCoord * d0).expand((double) f1, (double) f1, (double) f1));
            double d2 = d1;

            for (int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);

                if (entity.canBeCollidedWith()) {
                    float f2 = entity.getCollisionBorderSize();
                    AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double) f2, (double) f2, (double) f2);
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                    if (axisalignedbb.isVecInside(vec3)) {
                        if (0.0D < d2 || d2 == 0.0D) {
                            pointedEntity = entity;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0.0D;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                        if (d3 < d2 || d2 == 0.0D) {
                            pointedEntity = entity;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (pointedEntity != null && d2 < d1) {
                if (objectMouseOver != null) {
                    objectMouseOver.typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
                    objectMouseOver.entityHit = pointedEntity;
                    objectMouseOver.hitVec = vec33;
                } else {
                    objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
                    objectMouseOver.blockX = (int) vec33.xCoord;
                    objectMouseOver.blockY = (int) vec33.yCoord;
                    objectMouseOver.blockZ = (int) vec33.zCoord;
                }

            }
        }
        return objectMouseOver;
    }


    public static MovingObjectPosition rayTraceForBlocks(EntityLivingBase viewer, World world, double distance, float ticks, Vec3 offset, boolean collisionCheck, boolean onlySolid) {
        return rayTraceForBlocks(viewer, world, distance, ticks, offset, collisionCheck, onlySolid, null);
    }

    public static MovingObjectPosition rayTraceForBlocks(EntityLivingBase viewer, World world, double distance, float ticks, Vec3 offset, boolean collisionCheck, boolean onlySolid, Vec3 dir) {
        return rayTraceForBlocks(getPosition(viewer, ticks), world, distance, ticks, offset, collisionCheck, onlySolid, dir == null ? viewer.getLook(ticks) : dir);
    }

    public static MovingObjectPosition rayTraceForBlocks(Vec3 fromPosition, World world, double distance, float ticks, Vec3 offset, boolean collisionCheck, boolean onlySolid, Vec3 dir) {
        Vec3 vec3 = Vec3.createVectorHelper(fromPosition.xCoord, fromPosition.yCoord, fromPosition.zCoord);
        if (offset != null) {
            vec3 = vec3.addVector(offset.xCoord, offset.yCoord, offset.zCoord);
        }
        Vec3 vec31 = dir;
        Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
        return world.func_147447_a(vec3, vec32, collisionCheck, onlySolid, true);
    }

    @SideOnly(Side.CLIENT)
    public static MovingObjectPosition mouseRaytraceForBlocks(int mouseX, int mouseY, int width, int height, EntityLivingBase viewer, World world, boolean collisionCheck, boolean onlySolid) {
        Vec3 dir = MOMathHelper.mouseToWorldRay(mouseX, mouseY, width, height);
        Vec3 vec3 = viewer.getPosition(1);
        Vec3 vec32 = vec3.addVector(dir.xCoord * 32, dir.yCoord * 32, dir.zCoord * 32);
        return world.func_147447_a(vec3, vec32, false, false, true);
    }

    public static Vec3 getPosition(EntityLivingBase entity, float p_70666_1_) {
        if (p_70666_1_ == 1.0F) {
            return Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
        } else {
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) p_70666_1_;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) p_70666_1_;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) p_70666_1_;
            return Vec3.createVectorHelper(d0, d1, d2);
        }
    }
}
