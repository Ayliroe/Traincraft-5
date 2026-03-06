package train.common.library;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.render.CustomItemModel;
import ebf.tim.utility.DebugUtil;
import ebf.tim.utility.OreGen;
import fexcraft.tmt.slim.ModelBase;
import mods.railcraft.api.fuel.FuelManager;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import train.client.core.ClientProxy;
import train.common.Traincraft;
import train.common.api.*;
import train.common.api.blocks.BlockDynamic;
import train.common.api.blocks.TileRenderFacing;
import train.common.blocks.BlockTraincraftFluid;
import train.common.generation.ComponentVillageTrainstation;
import train.common.items.ItemRollingStock;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TraincraftRegistry {

    public static class TrainRegister {
        public int ID;
        public TrainRecord type;
        public RenderRecord render;
        public SoundRecord sounds;

        public AbstractTrains getEntity(World world) {
            try {
                AbstractTrains train = (AbstractTrains) type.getEntityClass().getConstructor(World.class).newInstance(world);
                train.init(this);
                return train;
            } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static final Map<String,TrainRegister> trains = new HashMap<>();
    public static final Map<Item,TrainRegister> trainsByItem = new HashMap<>(); //TODO: nuke this (the item should know its own register)
    public static final List<TrainRegister> stationTrains = new ArrayList<>();
    public static final Map<String,TrackRecord> tracks = new HashMap<>();

    public TraincraftRegistry() {}

    public static void registerTrains() {
        TrainRecord.put(trains, "assets/tc/data/TrainRecords.json");
        for (TrainRegister train : trains.values()) {
            trainsByItem.put(train.type.getItem(), train);
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            RenderRecord.put(trains, "assets/tc/data/RenderRecords.json");
            SoundRecord.put(trains, "assets/tc/data/SoundRecords.json");
        }
        ComponentVillageTrainstation.put(stationTrains, "assets/tc/data/TrainstationRecords.json");

        for (TrackRecord track : EnumTracks.values()) {
            tracks.put(track.getLabel(), track);
        }

        for(TrainRegister train : trains.values()){
            EntityRegistry.registerModEntity(train.type.getEntityClass(), train.type.getInternalName(), train.ID, Traincraft.instance, 512, 1, true);
        }
    }

    // Only used for ComputerCraft peripherals, TODO refactor
    public static String findTrainType(AbstractTrains t){
        if(t instanceof SteamTrain){
            return "steam";
        }
        if(t instanceof DieselTrain){
            return "diesel";
        }
        if(t instanceof ElectricTrain){
            return "electric";
        }
        if(t instanceof Tender){
            return "tender";
        }
        if(t instanceof AbstractWorkCart){
            return "work";
        }
        if(t instanceof Freight){
            return "freight";
        }
        if(t instanceof IPassenger){
            return "passenger";
        }
        if(t instanceof LiquidTank){
            return "tank";
        }
        return "decorative";
    }

    //todo:purge redundancy checks on postinit
    private static List<String> usedNames = new ArrayList<>();
    private static List<String> redundantTiles = new ArrayList<>();
    private static List<String> redundantBlocks = new ArrayList<>();
    public static int registryPosition = 18;

    public static Map<Block, Item> fluidMap = new HashMap<>();

    /**
     * @param block             the block in question
     * @param tab               the creative tab to put the block's item into, leave null for no creative tab entry.
     * @param MODID             modid of the host add-on, used for translations, texture names, and generic identification.
     * @param unlocalizedName   unlocalized name, used for translations, texture names, and generic identification.
     * @param oreDictionaryName ore directory name, used for other mods to identify type, mainly used for ingots, ores, and wood.
     * @param render            a ModelBase instance for rendering the tile entity.
     *                          Can instead be a TESR instance for more rendering control.
     *                          Null will fallback to a normal textured block render.
     * @return
     */
    public static Block registerBlock(Block block, CreativeTabs tab, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object render) {
        if (render instanceof ModelBase) {
            return registerBlock(block, tab, MODID, unlocalizedName, oreDictionaryName, Traincraft.proxy.getTESR(), (ModelBase) render, unlocalizedName);
        } else {
            return registerBlock(block, tab, MODID, unlocalizedName, oreDictionaryName, render, null, unlocalizedName);
        }
    }

    public static Block registerBlock(Block block, CreativeTabs tab, String modid, String unlocalizedName) {
        return registerBlock(block, tab, modid, unlocalizedName, null, Traincraft.proxy.getTESR());
    }

    public static Block registerBlock(Block block, CreativeTabs tab, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object TESR, @Nullable ModelBase model, String textureName) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Block with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (tab != null) {
            block.setCreativeTab(tab);
        }
        if (unlocalizedName.length() > 0) {
            block.setBlockName(unlocalizedName);
            GameRegistry.registerBlock(block, null, unlocalizedName);
            if(model!=null || (block instanceof ITileEntityProvider && ((ITileEntityProvider) block).createNewTileEntity(null,0) instanceof TileRenderFacing)) {
                RegisterItem(new ItemBlock(block), MODID, unlocalizedName, oreDictionaryName + ".item", tab, null, ebf.tim.render.CustomItemModel.instance, textureName);
                if(Traincraft.proxy.isClient() && TESR==null){
                    TESR=new ebf.tim.render.BlockTESR();
                }
            } else {
                RegisterItem(new ItemBlock(block), MODID, unlocalizedName, oreDictionaryName + ".item", tab, null, null, textureName);
            }
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Block with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }

        if (Traincraft.proxy.isClient() && MODID != null) {
            block.setBlockTextureName(MODID + ":" + textureName);
        }
        if (oreDictionaryName != null) {
            OreDictionary.registerOre(oreDictionaryName, block);
        }
        if (DebugUtil.dev && Traincraft.proxy.isClient() && block.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName() +".name"))) {
            DebugUtil.println("Block missing lang entry: " + block.getUnlocalizedName());
        }
        if (block instanceof ITileEntityProvider) {
            Class<? extends TileEntity> tile = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
            if (!redundantTiles.contains(tile.getName())) {
                GameRegistry.registerTileEntity(tile, unlocalizedName + "tile");
                redundantTiles.add(tile.getName());
                redundantTiles.add(unlocalizedName + "tile");
                if (Traincraft.proxy.isClient() && TESR!=null) {
                    regTileRender(MODID,unlocalizedName,block,tile,model,TESR);
                }
            } else if(!redundantTiles.contains(unlocalizedName + "tile")) {
                if (Traincraft.proxy.isClient() && TESR != null) {
                    regTileRender(MODID,unlocalizedName,block, tile, model, TESR);
                }
            } else {
                DebugUtil.println("redundant tile name found",tile.getName(), unlocalizedName + "tile");
                DebugUtil.printStackTrace();
            }
        }
        return block;
    }

    public static Item RegisterItem(Item itm, String MODID, String unlocalizedName, CreativeTabs tab) {
        return RegisterItem(itm, MODID, unlocalizedName, null, tab, null, null);
    }

    public static Item RegisterItem(Item itm, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable CreativeTabs tab, @Nullable Item container, @Nullable Object itemRender) {
        return RegisterItem(itm, MODID, unlocalizedName, oreDictionaryName, tab, container, itemRender, unlocalizedName.replace("item.", ""));
    }

    public static Item RegisterItem(Item itm, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable CreativeTabs tab, @Nullable Item container, @Nullable Object itemRender, String textureName) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Item with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (tab != null) {
            itm.setCreativeTab(tab);
        }
        if (container != null) {
            itm.setContainerItem(container);
        }
        if (!unlocalizedName.equals("")) {
            itm.setUnlocalizedName(unlocalizedName);
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Item with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }
        GameRegistry.registerItem(itm, unlocalizedName);
        if (oreDictionaryName != null) {
            OreDictionary.registerOre(oreDictionaryName, itm);
        }
        if (DebugUtil.dev && Traincraft.proxy != null && Traincraft.proxy.isClient() && itm.getUnlocalizedName().equals(StatCollector.translateToLocal(itm.getUnlocalizedName()+".name"))) {
            DebugUtil.println("Item missing lang entry: " + itm.getUnlocalizedName());
        }
        if (Traincraft.proxy.isClient() && itemRender != null) {
            MinecraftForgeClient.registerItemRenderer(itm, (IItemRenderer) itemRender);
            if (ClientProxy.preRenderModels) {
                ebf.tim.render.CustomItemModel.instance.renderItem(IItemRenderer.ItemRenderType.INVENTORY, new ItemStack(itm));
            }
        } else if (Traincraft.proxy.isClient() && itm instanceof ItemRollingStock) {
            MinecraftForgeClient.registerItemRenderer(itm, ebf.tim.render.CustomItemModel.instance);
            if (ClientProxy.preRenderModels) {
                ebf.tim.render.CustomItemModel.instance.renderItem(IItemRenderer.ItemRenderType.INVENTORY, new ItemStack(itm));
            }
        } else if(Traincraft.proxy.isClient()){
            itm.setTextureName(MODID+ ":" + textureName);
        }
        return itm;
    }


    public static Item RegisterFluid(Fluid fluid, String MODID, String unlocalizedName, boolean isGaseous, int density, MapColor color, @Nullable CreativeTabs tab) {
        if (usedNames.contains(unlocalizedName)) {
            DebugUtil.println("ERROR: ", "attempted to register Fluid with a used unlocalizedName", unlocalizedName);
            DebugUtil.throwStackTrace();
        }
        if (!unlocalizedName.equals("")) {
            fluid.setUnlocalizedName(unlocalizedName);
            usedNames.add(unlocalizedName);
        } else {
            DebugUtil.println("ERROR: ", "attempted to register Fluid with no unlocalizedName");
            DebugUtil.throwStackTrace();
        }
        fluid.setGaseous(isGaseous).setDensity(density);
        FluidRegistry.registerFluid(fluid);

        Block block = new BlockTraincraftFluid(fluid, Material.water).setBlockName("block." + unlocalizedName.replace(".item", "")).setBlockTextureName(MODID + ":block_" + unlocalizedName);
        ((BlockTraincraftFluid) block).setModID(MODID);
        GameRegistry.registerBlock(block, "block." + unlocalizedName);
        if (Traincraft.proxy.isClient()) {
            block.setBlockTextureName(MODID + ":" + unlocalizedName);
        }
        fluid.setBlock(block);


        Item bucket = new ItemBucket(block).setCreativeTab(tab).setContainerItem(Items.bucket);
        if (Traincraft.proxy.isClient()) {
            bucket.setTextureName(MODID + ":bucket_" + unlocalizedName);
        }
        bucket.setUnlocalizedName(unlocalizedName + ".bucket");
        GameRegistry.registerItem(bucket, "fluid." + unlocalizedName + ".bucket");
        FluidContainerRegistry.registerFluidContainer(fluid, new ItemStack(bucket), new ItemStack(Items.bucket));

        fluidMap.put(block, bucket);

        if (DebugUtil.dev && Traincraft.proxy.isClient()) {
            if (fluid.getUnlocalizedName().equals(StatCollector.translateToLocal(fluid.getUnlocalizedName()))) {
                DebugUtil.println("Fluid missing lang entry: " + fluid.getUnlocalizedName());
            }
            if (bucket.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName()))) {
                DebugUtil.println("Item missing lang entry: " + bucket.getUnlocalizedName());
            }
            if (block.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName()))) {
                DebugUtil.println("Block missing lang entry: " + block.getUnlocalizedName());
            }

        }
        return bucket;
    }

    @SideOnly(Side.CLIENT)
    private static void regTileRender(String MODID, String unlocalizedName, Block block, Class<? extends TileEntity> tile, ModelBase model, Object TESR) {

        if (block instanceof BlockDynamic) {
            if (model != null) {
                ((BlockDynamic) block).setModel(model);
            } else if (TESR != null) {
                ((BlockDynamic) block).setTESR(TESR);
            }
        }
        if (TESR != null) {
            cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(tile, (TileEntitySpecialRenderer) TESR);
            MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), CustomItemModel.instance);
            CustomItemModel.registerBlockTextures(Item.getItemFromBlock(block), ((ITileEntityProvider) block).createNewTileEntity(null, 0));
        } else {
            cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(tile, (TileEntitySpecialRenderer) Traincraft.proxy.getTESR());
            MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), CustomItemModel.instance);
            CustomItemModel.registerBlockTextures(Item.getItemFromBlock(block), ((ITileEntityProvider) block).createNewTileEntity(null, 0));
        }
    }


    /**
     * @param priority the priority to generate, higher numbers tend to generate after other mods.
     */
    public static void registerOreGen(int priority, OreGen veinConfig) {
        GameRegistry.registerWorldGenerator(veinConfig, priority);
    }


    public static void endRegistration() {
        usedNames = null;
        registryPosition = -1;
        redundantTiles = null;
    }


    //todo:add support for buildcraft/railcraft burnable fluids

    @Optional.Method(modid = "BuildCraft|Energy")
    static void registerBCFluid(Fluid f, int powerPerCycle, int totalBurningTime) {
        BuildcraftFuelRegistry.fuel.addFuel(f, powerPerCycle, totalBurningTime);
    }

    @Optional.Method(modid = "Railcraft")
    static void registerRCFluid(Fluid f, int totalBurningTime) {
        FuelManager.addBoilerFuel(f, totalBurningTime);
    }

}
