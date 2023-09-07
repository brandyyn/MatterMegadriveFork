package matteroverdrive.gui;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.api.network.MatterNetworkTaskState;
import matteroverdrive.container.ContainerMachine;
import matteroverdrive.container.ContainerReplicator;
import matteroverdrive.container.MOBaseContainer;
import matteroverdrive.data.ItemPattern;
import matteroverdrive.gui.element.*;
import matteroverdrive.gui.pages.PageTasks;
import matteroverdrive.matter_network.tasks.MatterNetworkTaskReplicatePattern;
import matteroverdrive.network.packet.server.PacketRemoveTask;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.tile.TileEntityMachineReplicator;
import matteroverdrive.util.MOStringHelper;
import matteroverdrive.util.MatterHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class GuiReplicator extends MOGuiNetworkMachine<TileEntityMachineReplicator> {
    MOElementEnergy energyElement;
    ElementMatterStored matterElement;
    ElementDualScaled replicate_progress;
    ElementSlot outputSlot;
    ElementSlot seccoundOutputSlot;
    PageTasks pagePackets;
    ElementItemPattern itemPattern;

    public GuiReplicator(InventoryPlayer inventoryPlayer, TileEntityMachineReplicator entity) {
        super(new ContainerReplicator(inventoryPlayer, entity), entity);
        name = "replicator";
        matterElement = new ElementMatterStored(this, 141, 39, machine.getMatterStorage());
        energyElement = new MOElementEnergy(this, 167, 39, machine.getEnergyStorage());
        replicate_progress = new ElementDualScaled(this, 32, 52);
        outputSlot = new ElementInventorySlot(this, this.getContainer().getSlotAt(machine.OUTPUT_SLOT_ID), 70, 52, 22, 22, "big");
        seccoundOutputSlot = new ElementInventorySlot(this, this.getContainer().getSlotAt(machine.SECOND_OUTPUT_SLOT_ID), 96, 52, 22, 22, "big");

        itemPattern = new ElementItemPattern(this, entity.getInternalPatternStorage(), "big_main", 37, 22);
        slotsList.setPosition(5, 49);
        slotsList.addElementAt(0, itemPattern);

        replicate_progress.setMode(1);
        replicate_progress.setSize(24, 16);
        replicate_progress.setTexture(Reference.TEXTURE_ARROW_PROGRESS, 48, 16);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addElement(replicate_progress);
        pages.get(0).addElement(outputSlot);
        pages.get(0).addElement(seccoundOutputSlot);
        pages.get(0).addElement(matterElement);
        pages.get(0).addElement(energyElement);
        AddHotbarPlayerSlots(inventorySlots, this);
        AddMainPlayerSlots(inventorySlots, pages.get(0));
    }

    @Override
    public void registerPages(MOBaseContainer container, TileEntityMachineReplicator machine) {
        super.registerPages(container, machine);

        pagePackets = new PageTasks(this, 10, 0, xSize, ySize, machine.getTaskQueue((byte) 0));
        pagePackets.setName("Tasks");
        AddPage(pagePackets, ClientProxy.holoIcons.getIcon("page_icon_tasks"), MOStringHelper.translateToLocal("gui.tooltip.page.tasks")).setIconColor(Reference.COLOR_MATTER);
    }

    @Override
    protected void renderToolTip(ItemStack stack, int x, int y) {
        List list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

        for (int k = 0; k < list.size(); ++k) {
            String info = (String) list.get(k);

            if (k == 0) {
                list.set(k, stack.getRarity().rarityColor + info);
            } else {
                list.set(k, EnumChatFormatting.GRAY + info);
            }
        }

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {

        super.drawGuiContainerBackgroundLayer(partialTick, x, y);

        replicate_progress.setQuantity(Math.round(((ContainerMachine) getContainer()).getProgress() * 24));
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        ManageReqiremnetsTooltips();
    }

    void ManageReqiremnetsTooltips() {
        ItemPattern itemPattern = machine.getInternalPatternStorage();

        if (itemPattern != null) {
            ItemStack item = itemPattern.toItemStack(false);

            int matterAmount = MatterHelper.getMatterAmountFromItem(item);
            matterElement.setDrain(-matterAmount);
            energyElement.setEnergyRequired(-(machine.getEnergyDrainMax()));
            energyElement.setEnergyRequiredPerTick(-machine.getEnergyDrainPerTick());
        }
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        MatterNetworkTaskReplicatePattern task = machine.getTaskQueue((byte) 0).peek();
        if (task != null && machine.getInternalPatternStorage() != null) {
            itemPattern.setAmount(((ContainerReplicator) inventorySlots).getPatternReplicateCount());
        } else
            itemPattern.setAmount(0);
    }

    @Override
    public void handleElementButtonClick(MOElementBase element, String buttonName, int mouseButton) {
        super.handleElementButtonClick(element, buttonName, mouseButton);
        if (buttonName.equals("DropTask")) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("TaskID", mouseButton);
            MatterOverdrive.packetPipeline.sendToServer(new PacketRemoveTask(machine, mouseButton, (byte) 0, MatterNetworkTaskState.INVALID));
        }
    }

}
