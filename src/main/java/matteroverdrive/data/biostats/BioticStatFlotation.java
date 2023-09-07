package matteroverdrive.data.biostats;

import com.google.common.collect.Multimap;
import matteroverdrive.entity.player.AndroidPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

public class BioticStatFlotation extends AbstractBioticStat {
    public BioticStatFlotation(String name, int xp) {
        super(name, xp);
        setShowOnHud(true);
    }

    @Override
    public void onAndroidUpdate(AndroidPlayer android, int level) {
        if (android.getPlayer().isInWater()) {
            android.getPlayer().motionY = android.getPlayer().motionY + 0.007;
        }
    }

    @Override
    public void onActionKeyPress(AndroidPlayer androidPlayer, int level, boolean server) {

    }

    @Override
    public void onKeyPress(AndroidPlayer androidPlayer, int level, int keycode, boolean down) {

    }

    @Override
    public void onLivingEvent(AndroidPlayer androidPlayer, int level, LivingEvent event) {

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
