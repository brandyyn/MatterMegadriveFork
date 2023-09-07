package matteroverdrive.compat.modules.waila;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import matteroverdrive.blocks.*;
import matteroverdrive.compat.Compat;
import matteroverdrive.machines.fusionReactorController.TileEntityMachineFusionReactorController;
import matteroverdrive.machines.transporter.TileEntityMachineTransporter;
import matteroverdrive.tile.TileEntityMachineDecomposer;
import matteroverdrive.tile.TileEntityMachineReplicator;
import matteroverdrive.tile.TileEntityMachineStarMap;
import matteroverdrive.tile.TileEntityWeaponStation;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Compatibility for WAILA
 */
@Compat("Waila")
public class CompatWaila {

    @Compat.Init
    public static void init(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("Waila", "register", "matteroverdrive.compat.modules.waila.CompatWaila.registerCallback");
    }

    public static void registerCallback(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new TileEntityWeaponStation(), BlockWeaponStation.class);
        registrar.registerBodyProvider(new TileEntityMachineStarMap(), BlockStarMap.class);
        registrar.registerBodyProvider(new TileEntityMachineTransporter(), BlockTransporter.class);
        registrar.registerBodyProvider(new TileEntityMachineDecomposer(), BlockDecomposer.class);
        registrar.registerBodyProvider(new TileEntityMachineReplicator(), BlockReplicator.class);
        registrar.registerBodyProvider(new TileEntityMachineFusionReactorController(), BlockFusionReactorController.class);
    }
}
