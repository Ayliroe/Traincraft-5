package train.common.core;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import ebf.tim.entities.EntitySeat;
import ebf.tim.gui.GUISeatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import train.common.Traincraft;
import train.common.api.*;
import train.common.containers.*;
import train.common.core.handlers.ChunkEvents;
import train.common.core.handlers.WorldEvents;
import train.common.core.util.MP3Player;
import train.common.entity.digger.EntityRotativeDigger;
import train.common.entity.zeppelin.AbstractZeppelin;
import train.common.inventory.*;
import train.common.library.GuiIDs;
import train.common.mtc.*;
import train.common.tile.*;
import train.common.tile.switchStand.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommonProxy implements IGuiHandler {
    public static List<MP3Player> playerList = new ArrayList<MP3Player>();
    public static boolean debug = false;

    public static String configDirectory = "";
    public void throwAlphaException() {
        throw new IllegalStateException("You're trying to use a Traincraft alpha-version past its expiry date. Download a release-build at https://minecraft.curseforge.com/projects/traincraft.");
    }

    public boolean isClient(){
		return false;
	}
	public void setKeyBinding(String name, int value) {
    }

    public void registerPlayerScaler(){}

    public void registerRenderInformation() {
    }

    public void registerEvents(FMLPreInitializationEvent event) {
        WorldEvents worldEvents = new WorldEvents();
        ChunkEvents chunkEvents = new ChunkEvents();

        registerEvent(worldEvents);
        registerEvent(chunkEvents);
        ForgeChunkManager.setForcedChunkLoadingCallback(Traincraft.instance, chunkEvents);

    }

    public void registerEvent(Object o) {
        FMLCommonHandler.instance().bus().register(o);
        MinecraftForge.EVENT_BUS.register(o);
    }

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileCrafterTierI.class, "TileCrafterTierI");
        GameRegistry.registerTileEntity(TileCrafterTierII.class, "TileCrafterTierII");
        GameRegistry.registerTileEntity(TileCrafterTierIII.class, "TileCrafterTierIII");
        GameRegistry.registerTileEntity(TileTrainWbench.class, "TileTrainWbench");
        GameRegistry.registerTileEntity(TileEntityDistil.class, "Tile Distil");
        GameRegistry.registerTileEntity(TileEntityOpenHearthFurnace.class, "Tile OpenHearthFurnace");
        GameRegistry.registerTileEntity(TileStopper.class, "TileStopper");
        GameRegistry.registerTileEntity(TileEmbeddedStopper.class, "TileEmbeddedStopper");
        GameRegistry.registerTileEntity(TileAmericanStopper.class, "TileAmericanStopper");
        GameRegistry.registerTileEntity(TileSignal.class, "TileTrainSignal");
        GameRegistry.registerTileEntity(TileLantern.class, "tileLantern");
        GameRegistry.registerTileEntity(TileMFPBWigWag.class, "tileMFPBWigWag");
        GameRegistry.registerTileEntity(TileWaterWheel.class, "tileWaterWheel");
        GameRegistry.registerTileEntity(TileWindMill.class, "tileWindMill");
        GameRegistry.registerTileEntity(TileGeneratorDiesel.class, "tileGeneratorDiesel");
        GameRegistry.registerTileEntity(TileBook.class, "tileBook");
        GameRegistry.registerTileEntity(TileTCRailGag.class, "tileTCRailGag");
        GameRegistry.registerTileEntity(TileTCRail.class, "tileTCRail");
        GameRegistry.registerTileEntity(TileBridgePillar.class, "tileTCBridgePillar");
        GameRegistry.registerTileEntity(TileMetroMadridPole.class, "tileMetroMadridPole");

        /**SwitchStands*/
        GameRegistry.registerTileEntity(TileSwitchStand.class, "tileSwitchStand");

        GameRegistry.registerTileEntity(TileOWOSwitchStand.class, "tileowoswitchstand");
        GameRegistry.registerTileEntity(TileCircleSwitchStand.class, "tilecircleswitchstand");
        GameRegistry.registerTileEntity(TileMILWSwitchStand.class, "tileMILWSwitchStand");
        GameRegistry.registerTileEntity(TileAutoSwitchStand.class, "tileautoSwitchStand");
        GameRegistry.registerTileEntity(TileOWOYardSwitchStand.class, "tileowoYardSwitchStand");

        GameRegistry.registerTileEntity(TileoverheadWire.class, "tileoverheadwire");
        GameRegistry.registerTileEntity(TileoverheadWireDouble.class, "tileoverheadwiredouble");
        GameRegistry.registerTileEntity(TilesignalSpanish.class, "tilesignalspanish");
        GameRegistry.registerTileEntity(TilekSignal.class, "tileksignal");
        GameRegistry.registerTileEntity(TileSpeedSign.class, "tilespeedsign");

        if (Traincraft.hasComputerCraft()) {
            GameRegistry.registerTileEntity(TileInfoTransmitterSpeed.class, "tileInfoTransmitterSpeed");
            GameRegistry.registerTileEntity(TileInfoTransmitterMTC.class, "tileInfoTransmitterMTC");
            GameRegistry.registerTileEntity(TileInfoGrabberMTC.class, "tileInfoReceiverMTC");
            GameRegistry.registerTileEntity(TileInfoGrabberDestination.class, "tileInfoReceiverDestination");
            GameRegistry.registerTileEntity(TileATOTransmitterStopPoint.class, "tileATOTransmitterStopPoint");
            GameRegistry.registerTileEntity(TilePDMInstructionRadio.class, "tilePDMInstructionRadio");
        }

        //GameRegistry.registerTileEntity(TileFortyFootContainer.class, "tileFortyFootContainer");
    }

    public void registerComputerCraftPeripherals() throws ClassNotFoundException {
        Class<?> computerCraft = Class.forName("dan200.computercraft.ComputerCraft");
        try {
            Method computerCraft_registerPeripheralProvider = computerCraft.getMethod("registerPeripheralProvider", Class.forName("dan200.computercraft.api.peripheral.IPeripheralProvider"));

            //Register all CC required blocks
            computerCraft_registerPeripheralProvider.invoke(null, BlockInfoTransmitterSpeed.instance);
            computerCraft_registerPeripheralProvider.invoke(null, BlockInfoGrabberMTC.instance);
            computerCraft_registerPeripheralProvider.invoke(null, BlockInfoTransmitterMTC.instance);
            computerCraft_registerPeripheralProvider.invoke(null, BlockATOTransmitterStopPoint.instance);

        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        Object entity;
        if (y == -1 && world instanceof WorldServer) // External entity
            entity = world.getEntityByID(x);
        else if (player.ridingEntity instanceof EntitySeat) // Riding entity (stock)
            entity = ((EntitySeat) player.ridingEntity).getHost();
        else if (player.ridingEntity instanceof AbstractZeppelin || player.ridingEntity instanceof EntityRotativeDigger) // Riding entity (other)
            entity = player.ridingEntity;
        else
            entity = world.getTileEntity(x, y, z); // Tile entity

        assert entity != null;

        switch (ID) {
            // --- TILE ENTITIES ---
            case (GuiIDs.CRAFTER_TIER_I):
                return new ContainerTier(player.inventory, (TileCrafterTierI) entity);
            case (GuiIDs.CRAFTER_TIER_II):
                return new ContainerTier(player.inventory, (TileCrafterTierII) entity);
            case (GuiIDs.CRAFTER_TIER_III):
                return new ContainerTier(player.inventory, (TileCrafterTierIII) entity);
            case (GuiIDs.DISTIL):
                return new ContainerDistil(player.inventory, (TileEntityDistil) entity);
            case (GuiIDs.GENERATOR_DIESEL):
                return new ContainerGeneratorDiesel(player.inventory, (TileGeneratorDiesel) entity);
            case (GuiIDs.OPEN_HEARTH_FURNACE):
                return new ContainerOpenHearthFurnace(player.inventory, (TileEntityOpenHearthFurnace) entity);
            case (GuiIDs.TRAIN_WORKBENCH):
                return new ContainerTrainWorkbench(player.inventory, player.worldObj, (TileTrainWbench) entity);
        /*case (GuiIDs.FORTY_FOOT_CONTAINER):
            return new ContainerStorage((TileFortyFootContainer)te, player);*/
            // --- INTERNAL STOCK GUIs ---
            case (GuiIDs.LOCO):
                return new InventoryLoco(player.inventory, (EntityRollingStock) entity);
            case (GuiIDs.CRAFTING_CART):
                return new ContainerWorkbenchCart(player.inventory, player.worldObj);
            case (GuiIDs.FURNACE_CART):
                return new InventoryWorkCart(player.inventory, (Entity) entity);
            case (GuiIDs.ZEPPELIN):
                return new InventoryZepp(player.inventory, (AbstractZeppelin) entity);
            case (GuiIDs.DIGGER):
                return new InventoryRotativeDigger(player.inventory, (EntityRotativeDigger) entity);
            case (GuiIDs.SEAT_GUI):
                return null; // No inventory for the Seat GUI
            // --- EXTERNAL STOCK GUIs ---
            case (GuiIDs.FREIGHT):
                return new InventoryFreight(player.inventory, (Freight) entity);
            case (GuiIDs.JUKEBOX):
                return new InventoryJukeBoxCart(player.inventory, (AbstractJukeBox) entity);
            case (GuiIDs.TENDER):
                return new InventoryTender(player.inventory, (Tender) entity);
            case (GuiIDs.BUILDER):
                return new InventoryBuilder(player.inventory, (AbstractTracksBuilder) entity);
            case (GuiIDs.LIQUID):
                return new InventoryLiquid(player.inventory, (LiquidTank) entity);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public int addArmor(String armor) {
        return 0;
    }

    public Minecraft getClientInstance() {
        return FMLClientHandler.instance().getClient();
    }

    public GuiScreen getCurrentScreen() {
        return null;
    }

    public void registerTextureFX() {
    }

    public void registerSounds() {
    }

    public void registerBookHandler() {
    }

    public Minecraft getMinecraft() {
        return null;
    }

    public void registerVillagerSkin(int villagerId, String textureName) {
    }

    public static void killAllStreams() {
        for (MP3Player p : playerList) {
            p.stop();
        }
    }

    public void openadmingui(String data) {
    }

    public static boolean checkJukeboxEntity(World world, int id) {
        return world.getEntityByID(id) != null;
    }

    public void doNEICheck(ItemStack stack) {
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public float getJukeboxVolume() {
        return 0;
    }

    public void registerKeyBindingHandler() {
    }

    public void setHook() {
    }


    public void seatGUI(EntityPlayer player, EntityRollingStock transport) {    }


    public Object getTESR(){return null;}
    public Object getEntityRender(){return null;}
    public Object getNullRender(){return null;}

}