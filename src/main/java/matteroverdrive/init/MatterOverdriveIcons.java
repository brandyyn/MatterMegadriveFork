package matteroverdrive.init;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import matteroverdrive.Reference;
import matteroverdrive.client.render.IconConnectedTexture;
import matteroverdrive.proxy.ClientProxy;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;

public class MatterOverdriveIcons {
    public static IIcon Base;
    public static IIcon Recycler;
    public static IIcon Vent;
    public static IIcon Vent2;
    public static IIcon matter_tank_full;
    public static IIcon matter_tank_empty;
    public static IIcon Transparent;
    public static IIcon Network_port_square;
    public static IconConnectedTexture Monitor_back;
    public static IIcon YellowStripes;
    public static IIcon Coil;
    public static IIcon charging_station;
    public static IIcon pattern_storage;
    public static IIcon replicator;
    public static IIcon matter_plasma_still;
    public static IIcon matter_plasma_flowing;
    public static IIcon packet_queue_active;
    public static IIcon molten_tritanium_still;
    public static IIcon molten_tritanium_flowing;
    public static IIcon tritanium_crate_base;
    public static IIcon tritanium_crate_overlay;
    public static IIcon inscriber;

    public static IIcon particle_steam;

    @SubscribeEvent
    public void registerTextures(TextureStitchEvent event) {
        switch (event.map.getTextureType()) {
            case 0:
                initBlockIcons(event.map);
                break;
            case 4:
                ClientProxy.holoIcons.registerIcons(event.map);
                break;
        }
    }

    private void initBlockIcons(IIconRegister r) {
        Vent = register(r, "vent");
        Base = register(r, "base");
        matter_tank_empty = register(r, "tank_empty");
        matter_tank_full = register(r, "tank_full");
        Transparent = register(r, "transperant");
        Network_port_square = register(r, "network_port");
        Vent2 = register(r, "vent2");
        Monitor_back = new IconConnectedTexture(register(r, "holo_monitor"));
        YellowStripes = register(r, "base_stripes");
        Recycler = register(r, "recycler_side");
        Coil = register(r, "base_coil");
        charging_station = register(r, "charging_station");
        pattern_storage = register(r, "pattern_storage");
        replicator = register(r, "replicator");
        matter_plasma_still = register(r, "matter_plasma_still");
        matter_plasma_flowing = register(r, "matter_plasma_flowing");
        packet_queue_active = register(r, "packet_queue_active");
        molten_tritanium_still = register(r, "molten_tritanium_still");
        molten_tritanium_flowing = register(r, "molten_tritanium_flowing");
        tritanium_crate_base = register(r, "tritanium_crate_base");
        tritanium_crate_overlay = register(r, "tritanium_crate_overlay");
        inscriber = register(r, "inscriber");
    }

    private void initParticleIcons(IIconRegister r) {
        particle_steam = register(r, "particle_steam");

    }

    private void initItems(IIconRegister r) {
        register(r, "vent");
        register(r, "base");
        register(r, "tank_empty");
        register(r, "tank_full");
    }

    public static IIcon register(IIconRegister register, String name) {
        return register.registerIcon(Reference.MOD_ID + ":" + name);
    }
}
