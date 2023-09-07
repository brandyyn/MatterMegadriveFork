package matteroverdrive.tile;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.api.network.IMatterNetworkBroadcaster;
import matteroverdrive.api.network.IMatterNetworkClient;
import matteroverdrive.api.network.IMatterNetworkDispatcher;
import matteroverdrive.data.BlockPos;
import matteroverdrive.data.ItemPattern;
import matteroverdrive.machines.MOTileEntityMachine;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.machines.components.ComponentMatterNetworkConfigs;
import matteroverdrive.matter_network.MatterNetworkPacket;
import matteroverdrive.matter_network.MatterNetworkPacketQueue;
import matteroverdrive.matter_network.MatterNetworkTaskQueue;
import matteroverdrive.matter_network.components.MatterNetworkComponentPatternMonitor;
import matteroverdrive.matter_network.tasks.MatterNetworkTaskReplicatePattern;
import matteroverdrive.network.packet.client.PacketPatternMonitorSync;
import matteroverdrive.util.TimeTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import static matteroverdrive.util.MOBlockHelper.getOppositeSide;

public class TileEntityMachinePatternMonitor extends MOTileEntityMachine implements IMatterNetworkDispatcher, IMatterNetworkClient, IMatterNetworkBroadcaster {
    public static final int BROADCAST_WEATING_DELAY = 80;
    public static final int SEARCH_DELAY = 20;
    public static final int VALIDATE_DELAY = 120;
    public static final int TASK_QUEUE_SIZE = 16;
    List<ItemPattern> guiPatterns = new ArrayList<>();
    HashSet<BlockPos> databases;
    MatterNetworkTaskQueue<MatterNetworkTaskReplicatePattern> taskQueue;
    TimeTracker searchDelayTracker;
    private MatterNetworkComponentPatternMonitor networkComponent;
    private ComponentMatterNetworkConfigs componentMatterNetworkConfigs;

    public TileEntityMachinePatternMonitor() {
        super(4);
        taskQueue = new MatterNetworkTaskQueue<>(this, TASK_QUEUE_SIZE);
        databases = new HashSet<>();
        searchDelayTracker = new TimeTracker();
        playerSlotsHotbar = true;

    }

    @Override
    protected void registerComponents() {
        super.registerComponents();
        componentMatterNetworkConfigs = new ComponentMatterNetworkConfigs(this);
        networkComponent = new MatterNetworkComponentPatternMonitor(this);
        addComponent(componentMatterNetworkConfigs);
        addComponent(networkComponent);
    }

    @Override
    public String getSound() {
        return null;
    }

    @Override
    public boolean hasSound() {
        return false;
    }

    @Override
    public boolean getServerActive() {
        return false;
    }

    @Override
    public float soundVolume() {
        return 0;
    }

    public boolean isAffectedByUpgrade(UpgradeTypes type) {
        return false;
    }

    //region Matter Network Functions

    @Override
    public int onNetworkTick(World world, TickEvent.Phase phase) {
        return networkComponent.onNetworkTick(world, phase);
    }

    @Override
    public MatterNetworkTaskQueue<MatterNetworkTaskReplicatePattern> getTaskQueue(int id) {
        return taskQueue;
    }

    @Override
    public int getTaskQueueCount() {
        return 1;
    }

    public void SyncDatabasesWithClient() {
        MatterOverdrive.packetPipeline.sendToAllAround(new PacketPatternMonitorSync(this), this, 64);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
        super.writeCustomNBT(nbt, categories, toDisk);
        if (categories.contains(MachineNBTCategory.DATA) && toDisk) {
            taskQueue.writeToNBT(nbt);
        }
    }

    @Override
    protected void onActiveChange() {

    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
        super.readCustomNBT(nbt, categories);
        if (categories.contains(MachineNBTCategory.DATA)) {
            taskQueue.readFromNBT(nbt);
        }
    }

    @Override
    protected void onAwake(Side side) {

    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean canConnectFromSide(ForgeDirection side) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        return getOppositeSide(meta) == side.ordinal();
    }

    //endregion

    public void queuePatternRequest(List<ItemPattern> request) {
        networkComponent.queuePatternRequest(request);
    }

    public HashSet<BlockPos> getDatabases() {
        return databases;
    }

    public void setDatabases(HashSet<BlockPos> blockPositions) {
        databases = blockPositions;
    }

    public void forceSearch(boolean refresh) {
        networkComponent.setNeedsSearchRefresh(refresh);
    }

    public void queueSearch() {
        if (searchDelayTracker.hasDelayPassed(worldObj, SEARCH_DELAY)) {
            forceSearch(true);
        }
    }

    public boolean needsRefresh() {
        return networkComponent.getNeedsSearchRefresh();
    }

    @Override
    public boolean canPreform(MatterNetworkPacket packet) {
        return networkComponent.canPreform(packet);
    }

    @Override
    public void queuePacket(MatterNetworkPacket packet, ForgeDirection from) {
        networkComponent.queuePacket(packet, from);
    }

    @Override
    public MatterNetworkPacketQueue getPacketQueue(int queueID) {
        return networkComponent.getPacketQueue(queueID);
    }

    @Override
    public int getPacketQueueCount() {
        return networkComponent.getPacketQueueCount();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public void onAdded(World world, int x, int y, int z) {

    }

    @Override
    public void onPlaced(World world, EntityLivingBase entityLiving) {

    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public NBTTagCompound getFilter() {
        return componentMatterNetworkConfigs.getFilter();
    }

    public ComponentMatterNetworkConfigs getComponentMatterNetworkConfigs() {
        return componentMatterNetworkConfigs;
    }

    @SideOnly(Side.CLIENT)
    public List<ItemPattern> getGuiPatterns() {
        return guiPatterns;
    }

    public void setGuiPatterns(List<ItemPattern> patterns) {
        this.guiPatterns = patterns;
    }
}
