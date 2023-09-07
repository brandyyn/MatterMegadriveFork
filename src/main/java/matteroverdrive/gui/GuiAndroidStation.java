package matteroverdrive.gui;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.api.android.IBionicStat;
import matteroverdrive.container.ContainerAndroidStation;
import matteroverdrive.container.slot.MOSlot;
import matteroverdrive.data.inventory.BionicSlot;
import matteroverdrive.entity.monster.EntityMeleeRogueAndroidMob;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.gui.element.*;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.init.MatterOverdriveBioticStats;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.tile.TileEntityAndroidStation;
import matteroverdrive.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class GuiAndroidStation extends MOGuiMachine<TileEntityAndroidStation> {
    private EntityMeleeRogueAndroidMob mob;
    private MOElementButtonScaled hudConfigs;
    ElementSlot[] parts_slots = new ElementSlot[Reference.BIONIC_BATTERY + 1];
    List<ElementBioStat> stats = new ArrayList<>(MatterOverdrive.statRegistry.getStats().size());

    public GuiAndroidStation(InventoryPlayer inventoryPlayer, TileEntityAndroidStation machine) {
        super(new ContainerAndroidStation(inventoryPlayer, machine), machine, 364, 250);
        texW = 255;
        texH = 237;
        AndroidPlayer androidPlayer = AndroidPlayer.get(inventoryPlayer.player);

        background = GuiWeaponStation.BG;

        for (int i = 0; i < parts_slots.length; i++) {
            parts_slots[i] = new ElementInventorySlot(this, (MOSlot) inventorySlots.getSlot(i), 20, 20, "holo", androidPlayer.getInventory().getSlot(i).getHoloIcon());
            parts_slots[i].setColor(Reference.COLOR_MATTER.getIntR(), Reference.COLOR_MATTER.getIntG(), Reference.COLOR_MATTER.getIntB(), 78);
            parts_slots[i].setInfo("biopart." + BionicSlot.names[i] + ".name");
        }

        parts_slots[Reference.BIONIC_HEAD].setPosition(220, ySize - 110);
        parts_slots[Reference.BIONIC_ARMS].setPosition(220, ySize - 80);
        parts_slots[Reference.BIONIC_LEGS].setPosition(220, ySize - 50);

        parts_slots[Reference.BIONIC_CHEST].setPosition(320, ySize - 110);
        parts_slots[Reference.BIONIC_OTHER].setPosition(320, ySize - 80);
        parts_slots[Reference.BIONIC_BATTERY].setPosition(320, ySize - 50);
        parts_slots[Reference.BIONIC_BATTERY].setIcon(ClientProxy.holoIcons.getIcon("battery"));

        addStat(androidPlayer, MatterOverdriveBioticStats.teleport, 0, 0, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.nanobots, 1, 1, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.nanoArmor, 0, 1, ForgeDirection.EAST);
        addStat(androidPlayer, MatterOverdriveBioticStats.flotation, 2, 0, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.speed, 3, 0, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.highJump, 3, 1, ForgeDirection.UP);
        addStat(androidPlayer, MatterOverdriveBioticStats.equalizer, 3, 2, ForgeDirection.UP);
        addStat(androidPlayer, MatterOverdriveBioticStats.shield, 0, 2, ForgeDirection.UP);
        addStat(androidPlayer, MatterOverdriveBioticStats.attack, 2, 1, ForgeDirection.WEST);
        addStat(androidPlayer, MatterOverdriveBioticStats.cloak, 0, 3, ForgeDirection.UP);
        addStat(androidPlayer, MatterOverdriveBioticStats.nightvision, 1, 0, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.minimap, 1, 2, ForgeDirection.UNKNOWN);
        addStat(androidPlayer, MatterOverdriveBioticStats.flashCooling, 2, 2, ForgeDirection.UP);
        addStat(androidPlayer, MatterOverdriveBioticStats.shockwave, 2, 3, ForgeDirection.UP);

        mob = new EntityMeleeRogueAndroidMob(Minecraft.getMinecraft().theWorld);
        mob.getEntityData().setBoolean("Hologram", true);

        hudConfigs = new MOElementButtonScaled(this, this, 48, 64, "hud_configs", 128, 24);
        hudConfigs.setText("HUD Options");
    }

    public void addStat(AndroidPlayer androidPlayer, IBionicStat stat, int x, int y, ForgeDirection direction) {
        ElementBioStat elemStat = new ElementBioStat(this, 0, 0, stat, androidPlayer.getUnlockedLevel(stat), androidPlayer, direction);
        elemStat.setPosition(54 + x * 30, 36 + y * 30);
        stats.add(elemStat);
    }

    @Override
    public void initGui() {
        super.initGui();

        for (ElementSlot elementSlot : parts_slots) {
            pages.get(0).addElement(elementSlot);
        }

        for (ElementBioStat stat : stats) {
            pages.get(0).addElement(stat);
        }

        pages.get(1).addElement(hudConfigs);

        AddMainPlayerSlots(inventorySlots, this);
        AddHotbarPlayerSlots(inventorySlots, this);
    }

    @Override
    public void drawTooltip(List<String> tooltips) {

        for (ElementBioStat stat : stats) {
            if (stat.intersectsWith(mouseX, mouseY)) {
                int itemCount = 0;
                for (ItemStack stack : stat.getStat().getRequiredItems()) {
                    int x = guiLeft + mouseX + 12 + 22 * itemCount;
                    int y = guiTop + mouseY - 36;
                    RenderUtils.renderStack(x, y, stack);
                    glPushMatrix();
                    glTranslated(0, 0, 100);
                    fontRendererObj.drawString(Integer.toString(stack.stackSize), x + 13, y + 8, 0xFFFFFF);
                    glPopMatrix();
                    itemCount++;
                }
            }
        }
        super.drawTooltip(tooltips);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        if (pages.get(0).isVisible()) {
            glPushMatrix();
            glTranslatef(0, 0, 100);
            drawEntityOnScreen(280, ySize - 25, 50, -this.mouseX + 280, -this.mouseY + ySize - 100, mc.thePlayer);
            glPopMatrix();

            String info = Minecraft.getMinecraft().thePlayer.experienceLevel + " XP";
            glDisable(GL_LIGHTING);
            int width = fontRendererObj.getStringWidth(info);
            fontRendererObj.drawString(EnumChatFormatting.GREEN + info, 280 - width / 2, ySize - 20, 0xFFFFFF);
        }
    }

    public void handleElementButtonClick(MOElementBase element, String elementName, int mouseButton) {
        super.handleElementButtonClick(element, elementName, mouseButton);
        if (element.equals(hudConfigs)) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiConfig(this, ConfigurationHandler.CATEGORY_ANDROID_HUD));
        }
    }

    /**
     * Draws an entity on the screen
     * Copied from {@link net.minecraft.client.gui.inventory.GuiInventory}
     *
     * @param x
     * @param y
     * @param scale
     * @param mouseX
     * @param mouseY
     * @param entity
     */
    private void drawEntityOnScreen(int x, int y, int scale, float mouseX, float mouseY, EntityPlayer entity) {
        glPushAttrib(GL_LIGHTING_BIT);
        glEnable(GL_COLOR_MATERIAL);
        glPushMatrix();
        glTranslatef((float) x, (float) y, 50.0F);
        glScalef((float) (-scale), (float) scale, (float) scale);
        glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        entity.renderYawOffset = mc.theWorld.getWorldTime();
        entity.rotationYaw = mc.theWorld.getWorldTime();
        entity.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;

        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);

        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        glDisable(GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        glPopAttrib();
    }
}
