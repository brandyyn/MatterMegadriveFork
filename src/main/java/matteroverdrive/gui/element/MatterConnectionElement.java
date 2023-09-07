package matteroverdrive.gui.element;

import matteroverdrive.Reference;
import matteroverdrive.gui.MOGuiBase;
import matteroverdrive.util.RenderUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class MatterConnectionElement extends MOElementBase {
    public static final ResourceLocation texture = new ResourceLocation(Reference.PATH_ELEMENTS + "side_slot_bg.png");

    int id;
    int count;

    @Override
    public void addTooltip(List<String> list, int mouseX, int mouseY) {
        list.add(StatCollector.translateToLocal(Item.getItemById(id).getUnlocalizedName() + ".name") + " [" + count + "]");
    }

    public MatterConnectionElement(MOGuiBase gui, int id, int count) {
        this(gui, 22, 22, id, count);
    }

    public MatterConnectionElement(MOGuiBase gui, int width, int height, int id, int count) {
        super(gui, 0, 0, width, height);

        this.id = id;
        this.count = count;
    }

    @Override
    public void updateInfo() {

    }

    @Override
    public void init() {

    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        GL11.glColor3f(1, 1, 1);
        RenderUtils.bindTexture(texture);
        gui.drawSizedTexturedModalRect(posX, posY, 0, 0, 22, 22, 22, 22);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        RenderUtils.renderStack(posX + 2, posY + 2, new ItemStack(Item.getItemById(id)));
        gui.getFontRenderer().drawStringWithShadow(Integer.toString(count), posX + 8, posY + 24, 0xFFFFFF);
    }
}
