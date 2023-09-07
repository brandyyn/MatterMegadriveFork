package matteroverdrive.data.biostats;

import com.google.common.collect.Multimap;
import matteroverdrive.Reference;
import matteroverdrive.api.events.weapon.MOEventEnergyWeapon;
import matteroverdrive.client.render.HoloIcons;
import matteroverdrive.entity.player.AndroidPlayer;
import matteroverdrive.proxy.ClientProxy;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.text.DecimalFormat;
import java.util.Random;

public class BioticStatFlashCooling extends AbstractBioticStat {
    private Random random;
    public static final float COOLDOWN_CHANGE = 0.45f;

    public BioticStatFlashCooling(String name, int xp) {
        super(name, xp);
        random = new Random();
    }

    @Override
    public String getDetails(int level) {
        return String.format(super.getDetails(level), EnumChatFormatting.GREEN + DecimalFormat.getPercentInstance().format(COOLDOWN_CHANGE) + EnumChatFormatting.GRAY);
    }

    @Override
    public void registerIcons(HoloIcons holoIcons) {
        this.icon = ClientProxy.holoIcons.getIcon("temperature");
    }

    @Override
    public void onAndroidUpdate(AndroidPlayer android, int level) {

    }

    @Override
    public void onActionKeyPress(AndroidPlayer androidPlayer, int level, boolean server) {

    }

    @Override
    public void onKeyPress(AndroidPlayer androidPlayer, int level, int keycode, boolean down) {

    }

    @Override
    public void onLivingEvent(AndroidPlayer androidPlayer, int level, LivingEvent event) {
        if (event instanceof MOEventEnergyWeapon.Overheat && random.nextFloat() < COOLDOWN_CHANGE) {
            event.setCanceled(true);
            ((MOEventEnergyWeapon.Overheat) event).energyWeapon.setHeat(((MOEventEnergyWeapon.Overheat) event).weaponStack, 0);
            ((MOEventEnergyWeapon.Overheat) event).entity.worldObj.playSoundAtEntity(((MOEventEnergyWeapon.Overheat) event).entity, Reference.MOD_ID + ":" + "overheat", 1F, 1f);
        }
    }

    @Override
    public void changeAndroidStats(AndroidPlayer androidPlayer, int level, boolean enabled) {

    }

    @Override
    public Multimap attributes(AndroidPlayer androidPlayer, int level) {
        return null;
    }

    @Override
    public boolean isActive(AndroidPlayer androidPlayer, int level) {
        return false;
    }

    @Override
    public int getDelay(AndroidPlayer androidPlayer, int level) {
        return 0;
    }
}
