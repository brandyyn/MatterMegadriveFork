package matteroverdrive.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import matteroverdrive.entity.player.MOExtendedProperties;
import net.minecraftforge.event.world.BlockEvent;

public class BlockHandler {
    @SubscribeEvent
    public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent event) {
        if (event.harvester != null) {
            MOExtendedProperties extendedProperties = MOExtendedProperties.get(event.harvester);
            if (extendedProperties != null) {
                extendedProperties.onEvent(event);
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlaceEvent(BlockEvent.PlaceEvent event) {
        if (event.player != null) {
            MOExtendedProperties extendedProperties = MOExtendedProperties.get(event.player);
            if (extendedProperties != null) {
                extendedProperties.onEvent(event);
            }
        }
    }
}
