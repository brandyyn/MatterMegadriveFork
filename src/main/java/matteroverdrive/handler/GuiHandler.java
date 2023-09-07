package matteroverdrive.handler;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.container.*;
import matteroverdrive.gui.*;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.machines.analyzer.TileEntityMachineMatterAnalyzer;
import matteroverdrive.machines.fusionReactorController.TileEntityMachineFusionReactorController;
import matteroverdrive.machines.transporter.TileEntityMachineTransporter;
import matteroverdrive.tile.*;
import matteroverdrive.util.MOLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class GuiHandler implements IGuiHandler {
    private Map<Class<? extends MOTileEntity>, Class<? extends MOGuiBase>> tileEntityGuiList;
    private Map<Class<? extends MOTileEntity>, Class<? extends MOBaseContainer>> tileEntityContainerList;

    public GuiHandler() {
        tileEntityGuiList = new HashMap<>();
        tileEntityContainerList = new HashMap<>();
    }

    public void register(Side side) {
        if (side == Side.SERVER) {
            //Container Registration
            registerContainer(TileEntityMachineSolarPanel.class, ContainerSolarPanel.class);
            registerContainer(TileEntityWeaponStation.class, ContainerWeaponStation.class);
            registerContainer(TileEntityMachineFusionReactorController.class, ContainerFusionReactor.class);
            registerContainer(TileEntityAndroidStation.class, ContainerAndroidStation.class);
            registerContainer(TileEntityMachineStarMap.class, ContainerStarMap.class);
            registerContainer(TileEntityInscriber.class, ContainerInscriber.class);
            registerContainer(TileEntityAndroidSpawner.class, ContainerAndroidSpawner.class);
            registerContainer(TileEntityMachineReplicator.class, ContainerReplicator.class);
            registerContainer(TileEntityMachinePatternMonitor.class, ContainerPatternMonitor.class);
        } else {
            //Gui Registration
            registerGuiAndContainer(TileEntityMachineReplicator.class, GuiReplicator.class, ContainerReplicator.class);
            registerGui(TileEntityMachineDecomposer.class, GuiDecomposer.class);
            registerGui(TileEntityMachineNetworkRouter.class, GuiNetworkRouter.class);
            registerGui(TileEntityMachineMatterAnalyzer.class, GuiMatterAnalyzer.class);
            registerGui(TileEntityMachinePatternStorage.class, GuiPatternStorage.class);
            registerGuiAndContainer(TileEntityMachineSolarPanel.class, GuiSolarPanel.class, ContainerSolarPanel.class);
            registerGuiAndContainer(TileEntityWeaponStation.class, GuiWeaponStation.class, ContainerWeaponStation.class);
            registerGuiAndContainer(TileEntityMachinePatternMonitor.class, GuiPatternMonitor.class, ContainerPatternMonitor.class);
            registerGui(TileEntityMachineNetworkSwitch.class, GuiNetworkSwitch.class);
            registerGui(TileEntityMachineTransporter.class, GuiTransporter.class);
            registerGui(TileEntityMachineMatterRecycler.class, GuiRecycler.class);
            registerGuiAndContainer(TileEntityMachineFusionReactorController.class, GuiFusionReactor.class, ContainerFusionReactor.class);
            registerGuiAndContainer(TileEntityAndroidStation.class, GuiAndroidStation.class, ContainerAndroidStation.class);
            registerGuiAndContainer(TileEntityMachineStarMap.class, GuiStarMap.class, ContainerStarMap.class);
            registerGui(TileEntityHoloSign.class, GuiHoloSign.class);
            registerGui(TileEntityMachineChargingStation.class, GuiChargingStation.class);
            registerGuiAndContainer(TileEntityInscriber.class, GuiInscriber.class, ContainerInscriber.class);
            registerGui(TileEntityMachineContractMarket.class, GuiContractMarket.class);
            registerGuiAndContainer(TileEntityAndroidSpawner.class, GuiAndroidSpawner.class, ContainerAndroidSpawner.class);
        }
    }

    public void registerContainer(Class<? extends MOTileEntity> tileEntity, Class<? extends MOBaseContainer> container) {
        tileEntityContainerList.put(tileEntity, container);
    }

    public void registerGuiAndContainer(Class<? extends MOTileEntity> tileEntity, Class<? extends MOGuiBase> gui, Class<? extends MOBaseContainer> container) {
        tileEntityContainerList.put(tileEntity, container);
        tileEntityGuiList.put(tileEntity, gui);
    }

    public void registerGui(Class<? extends MOTileEntity> tileEntity, Class<? extends MOGuiBase> gui) {
        tileEntityGuiList.put(tileEntity, gui);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
                                      int x, int y, int z) {

        TileEntity entity = world.getTileEntity(x, y, z);

        switch (ID) {
            default:
                if (entity != null && tileEntityContainerList.containsKey(entity.getClass())) {
                    try {
                        Class<? extends MOBaseContainer> containerClass = tileEntityContainerList.get(entity.getClass());
                        Constructor[] constructors = containerClass.getDeclaredConstructors();
                        for (Constructor constructor : constructors) {
                            Class[] parameterTypes = constructor.getParameterTypes();
                            if (parameterTypes.length == 2) {
                                if (parameterTypes[0].isInstance(player.inventory) && parameterTypes[1].isInstance(entity)) {
                                    onContainerOpen(entity, Side.SERVER);
                                    return constructor.newInstance(player.inventory, entity);
                                }
                            }
                        }
                    } catch (InvocationTargetException e) {
                        MOLog.warn("Could not call TileEntity constructor in server GUI handler", e);
                    } catch (InstantiationException e) {
                        MOLog.warn("Could not instantiate TileEntity in server GUI handler", e);
                    } catch (IllegalAccessException e) {
                        MOLog.warn("Could not access TileEntity constructor in server GUI handler", e);
                    }
                } else if (entity instanceof MOTileEntityMachine) {
                    return ContainerFactory.createMachineContainer((MOTileEntityMachine) entity, player.inventory);
                }
        }
        return null;
    }

    private void onContainerOpen(TileEntity entity, Side side) {
        if (entity instanceof MOTileEntityMachine) {
            ((MOTileEntityMachine) entity).onContainerOpen(side);
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
                                      int x, int y, int z) {

        TileEntity entity = world.getTileEntity(x, y, z);

        switch (ID) {
            default:
                if (tileEntityGuiList.containsKey(entity.getClass())) {
                    try {

                        Class<? extends MOGuiBase> containerClass = tileEntityGuiList.get(entity.getClass());
                        Constructor[] constructors = containerClass.getDeclaredConstructors();
                        for (Constructor constructor : constructors) {
                            Class[] parameterTypes = constructor.getParameterTypes();
                            if (parameterTypes.length == 2) {
                                if (parameterTypes[0].isInstance(player.inventory) && parameterTypes[1].isInstance(entity)) {
                                    onContainerOpen(entity, Side.CLIENT);
                                    return constructor.newInstance(player.inventory, entity);
                                }
                            }
                        }
                    } catch (InvocationTargetException e) {
                        MOLog.warn("Could not call TileEntity constructor in client GUI handler", e);
                    } catch (InstantiationException e) {
                        MOLog.warn("Could not instantiate the TileEntity in client GUI handler", e);
                    } catch (IllegalAccessException e) {
                        MOLog.warn("Could not access TileEntity constructor in client GUI handler");
                    }
                } else if (entity instanceof MOTileEntityMachine) {
                    return new MOGuiMachine(ContainerFactory.createMachineContainer((MOTileEntityMachine) entity, player.inventory), (MOTileEntityMachine) entity);
                }
        }
        return null;
    }

}
