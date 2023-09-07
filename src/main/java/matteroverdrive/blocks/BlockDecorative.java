package matteroverdrive.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.blocks.includes.IImageGenBlock;
import matteroverdrive.blocks.includes.MOBlock;
import matteroverdrive.client.render.block.RendererBlockDecorativeVertical;
import matteroverdrive.items.DecorativeBlockItem;
import matteroverdrive.world.MOImageGen;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.4.0
 */
public class BlockDecorative extends MOBlock implements IImageGenBlock {

    public static List<BlockDecorative> decorativeBlocks = new ArrayList<>();
    public String iconNames[];
    int mapColor;
    boolean rotated;
    boolean colored;
    IIcon[] icons;

    public BlockDecorative(Material material, String name, float hardness, int harvestLevel, float resistance, int mapColor, String[] iconNames) {
        super(material, name);
        setHardness(hardness);
        setHarvestLevel("pickaxe", harvestLevel);
        setResistance(resistance);
        setCreativeTab(MatterOverdrive.tabMatterOverdrive_decorative);
        this.mapColor = mapColor;
        decorativeBlocks.add(this);
        MOImageGen.worldGenerationBlockColors.put(this, getBlockColor(0));
        this.iconNames = iconNames;
        setRotationType(-1);
    }

    @Override
    protected void registerBlock() {
        GameRegistry.registerBlock(this, DecorativeBlockItem.class, this.getUnlocalizedName().substring(5));
    }

    public BlockDecorative(Material material, String name, String iconName, float hardness, int harvestLevel, float resistance, int mapColor) {
        this(material, name, hardness, harvestLevel, resistance, mapColor, new String[]{iconName, iconName, iconName, iconName, iconName, iconName});
    }

    @Override
    public void onBlockPlacedBy(World World, int x, int y, int z, EntityLivingBase player, ItemStack item) {
        World.setBlockMetadataWithNotify(x, y, z, item.getItemDamage(), 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        if (rotated) {
            p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
            p_149666_3_.add(new ItemStack(p_149666_1_, 1, 1));
        } else if (colored) {
            for (int i = 0; i < 16; i++) {
                p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
            }
        }
    }

    public BlockDecorative setRotated(boolean rotated) {
        this.rotated = rotated;
        return this;
    }

    public BlockDecorative setColored(boolean colored) {
        this.colored = colored;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[iconNames.length];
        for (int i = 0; i < iconNames.length; i++) {
            boolean iconRepeat = false;
            for (int j = 0; j < iconNames.length; j++) {
                if (iconNames[j].equals(iconNames[i]) && icons[j] != null) {
                    icons[i] = icons[j];
                    iconRepeat = true;
                    break;
                }
            }
            if (!iconRepeat)
                icons[i] = iconRegister.registerIcon(Reference.MOD_ID + ":" + iconNames[i]);
        }
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        if (colored) {
            return ItemDye.field_150922_c[MathHelper.clamp_int(meta, 0, ItemDye.field_150922_c.length - 1)];
        }
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        if (colored) {
            int meta = world.getBlockMetadata(x, y, z);
            return ItemDye.field_150922_c[MathHelper.clamp_int(meta, 0, ItemDye.field_150922_c.length - 1)];
        }
        return 16777215;
    }

    @Override
    public int damageDropped(int meta) {
        return rotated || colored ? meta : 0;
    }

    @Override
    public int getRenderType() {
        return RendererBlockDecorativeVertical.renderID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[side];
    }

    @Override
    public int getBlockColor(int meta) {
        return mapColor;
    }

    public boolean canBeRotated() {
        return rotated;
    }

    public boolean isColored() {
        return colored;
    }
}
