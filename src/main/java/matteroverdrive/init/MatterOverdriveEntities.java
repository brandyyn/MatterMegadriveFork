package matteroverdrive.init;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.*;
import matteroverdrive.entity.monster.EntityMeleeRogueAndroidMob;
import matteroverdrive.entity.monster.EntityMutantScientist;
import matteroverdrive.entity.monster.EntityRangedRogueAndroidMob;
import matteroverdrive.entity.monster.EntityRogueAndroid;
import matteroverdrive.entity.weapon.PlasmaBolt;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.handler.village.TradeHandlerMadScientist;
import matteroverdrive.handler.village.VillageCreatationMadScientist;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

public class MatterOverdriveEntities {
    public static final int ENTITY_STARTING_ID = 171;
    public static EntityRogueAndroid rogueandroid;

    public static void init(FMLPreInitializationEvent event, ConfigurationHandler configurationHandler) {
        rogueandroid = new EntityRogueAndroid();
        configurationHandler.subscribe(rogueandroid);
    }

    public static void register(FMLPostInitializationEvent event) {
        MatterOverdrive.configHandler.config.load();
        addEntity(EntityFailedPig.class, "failed_pig", 15771042, 0x33CC33, 171);
        addEntity(EntityFailedCow.class, "failed_cow", 4470310, 0x33CC33, 172);
        addEntity(EntityFailedChicken.class, "failed_chicken", 10592673, 0x33CC33, 173);
        addEntity(EntityFailedSheep.class, "failed_sheep", 15198183, 0x33CC33, 174);
        addEntity(EntityVillagerMadScientist.class, "mad_scientist", 0xFFFFFF, 0, 175);
        addEntity(EntityMutantScientist.class, "mutant_scientist", 0xFFFFFF, 0x00FF00, 176);
        addEntity(EntityMeleeRogueAndroidMob.class, "rogue_android", 0xFFFFF, 0, 177);
        addEntity(EntityRangedRogueAndroidMob.class, "ranged_rogue_android", 0xFFFFF, 0, 178);

        VillagerRegistry.instance().registerVillageTradeHandler(666, new TradeHandlerMadScientist());
        VillageCreatationMadScientist creatationMadScientist = new VillageCreatationMadScientist();
        VillagerRegistry.instance().registerVillageCreationHandler(creatationMadScientist);
        EntityRogueAndroid.addAsBiomeGen(EntityMeleeRogueAndroidMob.class);
        EntityRogueAndroid.addAsBiomeGen(EntityRangedRogueAndroidMob.class);

        int phaserFireID = loadIDFromConfig(PlasmaBolt.class, "phaser_fire", 170);
        EntityRegistry.registerGlobalEntityID(PlasmaBolt.class, "phaser_fire", phaserFireID);
        MatterOverdrive.configHandler.save();
    }

    public static int addEntity(Class<? extends Entity> enityClass, String name, int mainColor, int spotsColor, int id) {
        id = loadIDFromConfig(enityClass, name, id);
        EntityRegistry.registerGlobalEntityID(enityClass, name, id);
        EntityRegistry.registerModEntity(enityClass, name, id, MatterOverdrive.instance, 64, 1, true);
        createEgg(id, mainColor, spotsColor);
        return id;
    }

    public static void createEgg(int id, int solidColor, int spotColor) {
        EntityList.entityEggs.put(Integer.valueOf(id), new EntityList.EntityEggInfo(id, solidColor, spotColor));
    }

    public static int loadIDFromConfig(Class<? extends Entity> entityClass, String name, int id) {
        return MatterOverdrive.configHandler.getInt(getEntityConfigKey(name), ConfigurationHandler.CATEGORY_ENTITIES, id);
    }

    private static String getEntityConfigKey(String name) {
        return "entity." + name + ".id";
    }
}
