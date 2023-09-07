package matteroverdrive.client.render;

import matteroverdrive.Reference;
import matteroverdrive.api.IScannable;
import matteroverdrive.api.inventory.IBlockScanner;
import matteroverdrive.client.RenderHandler;
import matteroverdrive.client.render.tileentity.TileEntityRendererPatternMonitor;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.items.MatterScanner;
import matteroverdrive.util.MatterDatabaseHelper;
import matteroverdrive.util.MatterHelper;
import matteroverdrive.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class RenderMatterScannerInfoHandler implements IWorldLastRenderer {
    private DecimalFormat healthFormater = new DecimalFormat("#.##");
    public ResourceLocation spinnerTexture = new ResourceLocation(Reference.PATH_ELEMENTS + "spinner.png");

    public void onRenderWorldLast(RenderHandler renderHandler, RenderWorldLastEvent event) {
        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();

        if (isScanning(heldItem)) {
            glPushMatrix();
            renderInfo(Minecraft.getMinecraft().thePlayer, heldItem, event.partialTicks);
            glPopMatrix();
        } else if (AndroidPlayer.get(Minecraft.getMinecraft().thePlayer).isAndroid()) {
            renderInfo(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().objectMouseOver, null, event.partialTicks);
        }
    }

    private boolean isScanning(ItemStack heldItemStack) {
        if (heldItemStack != null) {
            Item heldItem = heldItemStack.getItem();

            if (heldItem instanceof MatterScanner && !MatterScanner.isLinked(heldItemStack)) {
                // Item is a matter scanner, but is not linked to a pattern storage.
                return false;
            }

            //noinspection RedundantIfStatement
            if (heldItem instanceof IBlockScanner && Minecraft.getMinecraft().thePlayer.isUsingItem()) {
                // Item is some kind of scanner and the player is using it.
                return true;
            }
        }
        return false;
    }

    private void renderInfo(EntityPlayer player, ItemStack scanner, float ticks) {
        IBlockScanner scannerItem = (IBlockScanner) scanner.getItem();
        MovingObjectPosition position = scannerItem.getScanningPos(scanner, player);
        renderInfo(player, position, scanner, ticks);
    }

    private void renderInfo(EntityPlayer player, MovingObjectPosition position, ItemStack scanner, float ticks) {
        Vec3 playerPos = player.getPosition(ticks);

        glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);

        if (position != null) {

            if (position.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {

                Block block = player.worldObj.getBlock(position.blockX, position.blockY, position.blockZ);
                if (block != null) {
                    renderBlockInfo(block, position, player, playerPos, scanner, scanner == null);
                }
            } else if (position.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                if (position.entityHit != null) {
                    renderEntityInfo(position.entityHit, position, player, playerPos, ticks);
                }
            }
        }
        glPopAttrib();
    }

    private void renderBlockInfo(Block block, MovingObjectPosition position, EntityPlayer player, Vec3 playerPos, ItemStack scanner, boolean infoOnly) {
        double offset = 0.1;
        ForgeDirection side = ForgeDirection.getOrientation(position.sideHit);
        List<String> infos = new ArrayList<>();
        if (block instanceof IScannable) {
            ((IScannable) block).addInfo(player.worldObj, position.blockX, position.blockY, position.blockZ, infos);

        } else if (player.worldObj.getTileEntity(position.blockX, position.blockY, position.blockZ) instanceof IScannable) {
            ((IScannable) player.worldObj.getTileEntity(position.blockX, position.blockY, position.blockZ)).addInfo(player.worldObj, position.blockX, position.blockY, position.blockZ, infos);
        } else if (infoOnly) {
            return;
        }

        glPushMatrix();
        glTranslated(side.offsetX * 0.5 + (position.blockX + 0.5) - playerPos.xCoord, side.offsetY * 0.5 + (position.blockY + 0.5) - playerPos.yCoord, side.offsetZ * 0.5 + (position.blockZ + 0.5) - playerPos.zCoord);
        rotateFromSide(side, player.rotationYaw);
        glTranslated(-0.5, -0.5, -offset);
        drawInfoPlane(0);

        ItemStack blockItemStack = MatterDatabaseHelper.GetItemStackFromWorld(player.worldObj, position.blockX, position.blockY, position.blockZ);
        int matter = MatterHelper.getMatterAmountFromItem(blockItemStack);
        if (matter > 0) {
            infos.add("Matter: " + MatterHelper.formatMatter(matter));
        }
        String blockName = "Unknown";

        try {
            if (blockItemStack != null) {
                blockName = blockItemStack.getDisplayName();
            } else {
                blockName = player.worldObj.getBlock(position.blockX, position.blockY, position.blockZ).getLocalizedName();
            }
        } catch (Exception e) {
            blockName = player.worldObj.getBlock(position.blockX, position.blockY, position.blockZ).getLocalizedName();
        } finally {
            drawInfoList(blockName, infos);
        }

        if (!(block instanceof IScannable) && scanner != null) {
            drawProgressBar(scanner, player);
        }
        glPopMatrix();
    }

    private void drawProgressBar(ItemStack scanner, EntityPlayer player) {
        glPushMatrix();
        renderer().bindTexture(spinnerTexture);

        int count = player.getItemInUseCount();
        int maxCount = scanner.getMaxItemUseDuration();

        glAlphaFunc(GL_GREATER, (float) count / (float) maxCount);
        RenderUtils.applyColorWithMultipy(Reference.COLOR_HOLO, 0.5f);
        RenderUtils.drawPlane(0.35, 0.45, -0.1, 0.3, 0.3);
        glPopMatrix();
    }

    private void renderEntityInfo(Entity entity, MovingObjectPosition position, EntityPlayer player, Vec3 playerPos, float ticks) {
        if (!entity.isInvisibleToPlayer(player)) {
            double offset = 0.1;
            Vec3 entityPos;
            String name = entity.getCommandSenderName();
            List<String> infos = new ArrayList<String>();
            if (entity instanceof EntityLivingBase) {
                entityPos = ((EntityLivingBase) entity).getPosition(ticks);
                entityPos.yCoord += +entity.getEyeHeight();
                infos.add("Health: " + (healthFormater.format(((EntityLivingBase) entity).getHealth()) + " / " + ((EntityLivingBase) entity).getMaxHealth()));

                glPushMatrix();
                glTranslated(entityPos.xCoord - playerPos.xCoord, entityPos.yCoord - playerPos.yCoord, entityPos.zCoord - playerPos.zCoord);
                glRotated(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * ticks, 0, -1, 0);
                glRotated(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * ticks, 1, 0, 0);
                glTranslated(1, 0, 0);
                glRotated(180, 0, 0, 1);
                glTranslated(-0.5, -0.5, -offset);
                drawInfoPlane(0.5);
                drawInfoList(name, infos);
                glPopMatrix();
            }
        }
    }

    private void drawInfoPlane(double opacity) {
        if (opacity > 0) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_TEXTURE_2D);
            glColor4d(0, 0, 0, opacity);
            RenderUtils.drawPlane(1);
            glEnable(GL_TEXTURE_2D);
        }

        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
        RenderUtils.applyColorWithMultipy(Reference.COLOR_HOLO, 0.05f);
        renderer().bindTexture(TileEntityRendererPatternMonitor.screenTextureBack);
        RenderUtils.drawPlane(0, 0, -0.01, 1, 1);
    }

    private void drawInfoList(String name, List<String> infos) {
        glPushMatrix();
        int width = fontRenderer().getStringWidth(name);
        glTranslated(0.5, 0.5, -0.05);
        glScaled(0.01, 0.01, 0.01);
        fontRenderer().drawString(name, -width / 2, -40, Reference.COLOR_HOLO.getColor());

        for (int i = 0; i < infos.size(); i++) {
            width = fontRenderer().getStringWidth(infos.get(i));
            fontRenderer().drawString(infos.get(i), -width / 2, -24 + 16 * i, Reference.COLOR_HOLO.getColor());
        }
        glPopMatrix();
    }

    public static void rotateFromSide(ForgeDirection side, float yaw) {
        if (side == ForgeDirection.UP) {
            glRotatef(Math.round(yaw / 90) * 90 - 180, 0, -1, 0);
            glRotated(90, 1, 0, 0);
        } else if (side == ForgeDirection.DOWN) {
            glRotatef(Math.round(yaw / 90) * 90 - 180, 0, -1, 0);
            glRotated(-90, 1, 0, 0);
        } else if (side == ForgeDirection.WEST) {
            glRotated(90, 0, 1, 0);
            glRotated(180, 0, 0, 1);
        } else if (side == ForgeDirection.EAST) {
            glRotated(-90, 0, 1, 0);
            glRotated(180, 0, 0, 1);
        } else if (side == ForgeDirection.NORTH) {
            glRotated(180, 0, 0, 1);
        } else if (side == ForgeDirection.SOUTH) {
            glRotated(180, 0, 1, 0);
            glRotated(180, 0, 0, 1);
        }
    }

    private FontRenderer fontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    private TextureManager renderer() {
        return Minecraft.getMinecraft().renderEngine;
    }
}
