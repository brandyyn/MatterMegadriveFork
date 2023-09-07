package matteroverdrive.data.biostats;

import com.google.common.collect.Multimap;
import matteroverdrive.Reference;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.util.IConfigSubscriber;
import matteroverdrive.util.MOEnergyHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.EnumSet;

public class BioticStatCloak extends AbstractBioticStat implements IConfigSubscriber {
    public static int ENERGY_PER_TICK = 128;

    public BioticStatCloak(String name, int xp) {
        super(name, xp);
        setShowOnHud(true);
        setShowOnWheel(true);
    }

    @Override
    public String getDetails(int level) {
        return String.format(super.getDetails(level), EnumChatFormatting.YELLOW.toString() + ENERGY_PER_TICK + MOEnergyHelper.ENERGY_UNIT + EnumChatFormatting.GRAY);
    }

    @Override
    public void onAndroidUpdate(AndroidPlayer android, int level) {
        if (!android.getPlayer().worldObj.isRemote) {
            if (isActive(android, level)) {
                if (!android.getPlayer().isInvisible()) {
                    android.getPlayer().worldObj.playSoundAtEntity(android.getPlayer(), Reference.MOD_ID + ":cloak_on", 1, 1);
                }
                android.getPlayer().setInvisible(true);
                android.extractEnergyScaled(ENERGY_PER_TICK);
            } else {
                if (android.getPlayer().isInvisible()) {
                    android.getPlayer().worldObj.playSoundAtEntity(android.getPlayer(), Reference.MOD_ID + ":cloak_off", 1, 1);
                }
                android.getPlayer().setInvisible(false);
            }
        }
    }

    @Override
    public void onActionKeyPress(AndroidPlayer android, int level, boolean server) {
        if (this.equals(android.getActiveStat()) && server) {
            setActive(android, level, !android.getEffects().getBoolean("Cloaked"));
        }
    }

    public void setActive(AndroidPlayer android, int level, boolean active) {
        android.getEffects().setBoolean("Cloaked", active);
        android.sync(EnumSet.of(AndroidPlayer.DataType.EFFECTS), true);
    }

    @Override
    public void onKeyPress(AndroidPlayer androidPlayer, int level, int keycode, boolean down) {

    }

    @Override
    public void onLivingEvent(AndroidPlayer androidPlayer, int level, LivingEvent event) {

    }

    @Override
    public void changeAndroidStats(AndroidPlayer androidPlayer, int level, boolean enabled) {
        if (!isEnabled(androidPlayer, level) && isActive(androidPlayer, level)) {
            setActive(androidPlayer, level, false);
        }
    }

    @Override
    public Multimap attributes(AndroidPlayer androidPlayer, int level) {
        return null;
    }

    @Override
    public boolean isActive(AndroidPlayer androidPlayer, int level) {
        return androidPlayer.getEffects().getBoolean("Cloaked") && !androidPlayer.getPlayer().isUsingItem();
    }

    @Override
    public int getDelay(AndroidPlayer androidPlayer, int level) {
        return 0;
    }

    @Override
    public boolean isEnabled(AndroidPlayer androidPlayer, int level) {
        return super.isEnabled(androidPlayer, level) && androidPlayer.hasEnoughEnergyScaled(ENERGY_PER_TICK);
    }

    @Override
    public void onConfigChanged(ConfigurationHandler config) {
        ENERGY_PER_TICK = config.getInt("cloak_energy_per_tick", ConfigurationHandler.CATEGORY_ABILITIES, 128, "The energy cost of the Cloak");
    }
}
