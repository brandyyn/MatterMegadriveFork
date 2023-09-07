package matteroverdrive.gui.element;

import cofh.api.energy.IEnergyStorage;
import matteroverdrive.Reference;
import matteroverdrive.gui.MOGuiBase;
import matteroverdrive.util.MOEnergyHelper;
import matteroverdrive.util.RenderUtils;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class MOElementEnergy extends MOElementBase {
    protected int energyRequired;
    protected int energyRequiredPerTick;
    protected IEnergyStorage storage;
    protected boolean alwaysShowMinimum = false;

    public MOElementEnergy(MOGuiBase gui, int posX, int posY, IEnergyStorage storage) {
        super(gui, posX, posY);
        this.storage = storage;
        setTexture(Reference.TEXTURE_ENERGY_METER, 32, 64);
        this.sizeX = 16;
        this.sizeY = 42;
        this.texW = 32;
        this.texH = 64;
    }

    @Override
    public void updateInfo() {

    }

    @Override
    public void init() {

    }

    @Override
    public void addTooltip(List<String> list, int mouseX, int mouseY) {
        if (this.storage.getMaxEnergyStored() < 0) {
            list.add("Infinite RF");
        } else {
            list.add(this.storage.getEnergyStored() + " / " + this.storage.getMaxEnergyStored() + " RF");
        }

        if (energyRequired > 0) {
            list.add(EnumChatFormatting.GREEN + "+" + String.valueOf(energyRequired) + MOEnergyHelper.ENERGY_UNIT + EnumChatFormatting.RESET);
        } else if (energyRequired < 0) {
            list.add(EnumChatFormatting.RED + String.valueOf(energyRequired) + MOEnergyHelper.ENERGY_UNIT + EnumChatFormatting.RESET);
        }
        if (energyRequiredPerTick > 0) {
            list.add(EnumChatFormatting.GREEN + "+" + String.valueOf(energyRequiredPerTick) + MOEnergyHelper.ENERGY_UNIT + "/t" + EnumChatFormatting.RESET);
        } else if (energyRequiredPerTick < 0) {
            list.add(EnumChatFormatting.RED + String.valueOf(energyRequiredPerTick) + MOEnergyHelper.ENERGY_UNIT + "/t" + EnumChatFormatting.RESET);
        }
    }

    public void drawBackground(int var1, int var2, float var3) {
        int var4 = this.getScaled();
        RenderUtils.bindTexture(this.texture);
        this.drawTexturedModalRect(this.posX, this.posY, 0, 0, this.sizeX, this.sizeY);
        this.drawTexturedModalRect(this.posX, this.posY + 42 - var4, 16, 42 - var4, this.sizeX, var4);
    }

    public void drawForeground(int var1, int var2) {
    }

    protected int getScaled() {
        if (this.storage.getMaxEnergyStored() <= 0) {
            return this.sizeY;
        } else {
            long var1 = (long) this.storage.getEnergyStored() * (long) this.sizeY / (long) this.storage.getMaxEnergyStored();
            return this.alwaysShowMinimum && this.storage.getEnergyStored() > 0 ? (int) Math.max(1, Math.round((double) var1)) : (int) Math.round((double) var1);
        }
    }

    public int getEnergyRequired() {
        return energyRequired;
    }

    public int getEnergyRequiredPerTick() {
        return energyRequiredPerTick;
    }

    public void setEnergyRequired(int energyRequired) {
        this.energyRequired = energyRequired;
    }

    public void setEnergyRequiredPerTick(int energyRequired) {
        this.energyRequiredPerTick = energyRequired;
    }
}
