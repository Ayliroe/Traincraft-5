package train.common.generation;

import com.google.gson.Gson;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import train.common.Traincraft;
import train.common.api.EntityRollingStock;
import train.common.api.TrainRecord;
import train.common.blocks.TCBlocks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static train.common.Traincraft.tcLog;

class TrainstationRecordJson {
	String entryName;
}

public class ComponentVillageTrainstation extends StructureVillagePieces.Village {

	private int averageGroundLevel = -1;

	public ComponentVillageTrainstation() {}
	
	public ComponentVillageTrainstation(StructureVillagePieces.Start par1ComponentVillageStartPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
		super(par1ComponentVillageStartPiece, par2);
		this.coordBaseMode = par5;
		this.boundingBox = par4StructureBoundingBox;
	}

	public static ComponentVillageTrainstation buildComponent(StructureVillagePieces.Start par0ComponentVillageStartPiece, List par1List, Random par2Random, int par3, int par4, int par5, int par6, int par7) {
		StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 9, 9, 10, par6);
		return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par1List, structureboundingbox) == null ? new ComponentVillageTrainstation(par0ComponentVillageStartPiece, par7, par2Random, structureboundingbox, par6) : null;
	}

	/*
	 * Stocks being defined in a json file, it is not guaranteed that they are valid objects to be spawned.
	 * This catches at game start that the strings are valid, but also that the entity will readily instantiate, so players don't encounter a crash on a village generating
	 * (Probably overkill, but at least it's there)
	 **/
	public static List<TrainRecord> initTrainstationRecords() {

		InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/TrainstationRecords.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		TrainstationRecordJson[] recordsJson = new Gson().fromJson(reader, TrainstationRecordJson[].class);

		List<TrainRecord> validRecords = new ArrayList<>();

		for (TrainRecord record : Traincraft.instance.trainRecords) {
			//TODO: nested loops = BAD
			for (TrainstationRecordJson recordJson : recordsJson){
				if (record.getName().equals(recordJson.entryName)) {

					// Attempt to instantiate the stock. This should also cause a stack trace if it fails.
					if ((EntityRollingStock) record.getEntity((World) null) != null) {
						validRecords.add(record);
					} else {
						tcLog.error("Invalid trainstation stock: " + record.getName());
					}
				}
			}
		}
		return validRecords;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox) {
		if (averageGroundLevel < 0) {
			averageGroundLevel = getAverageGroundLevel(world, structureboundingbox);

			if (averageGroundLevel < 0) {
				return true;
			}

			boundingBox.offset(0, averageGroundLevel - boundingBox.maxY + 9 - 1, 0);
		}
		fillWithBlocks(world, structureboundingbox, 1, 1, 1, 7, 5, 4, Blocks.air, Blocks.air, false);
		fillWithBlocks(world, structureboundingbox, 0, 0, 0, 8, 0, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 5, 0, 8, 5, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 6, 1, 8, 6, 4, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 7, 2, 8, 7, 3, Blocks.brick_block, Blocks.brick_block, false);
		int i = getMetadataWithOffset(Blocks.oak_stairs, 3);
		int j = getMetadataWithOffset(Blocks.oak_stairs, 2);
		int k;
		int l;

		for (k = -1; k <= 2; ++k) {
			for (l = 0; l <= 8; ++l) {
				placeBlockAtCurrentPosition(world, Blocks.oak_stairs, i, l, 6 + k, k, structureboundingbox);
				placeBlockAtCurrentPosition(world, Blocks.oak_stairs, j, l, 6 + k, 5 - k, structureboundingbox);
			}
		}

		fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 1, 1, 5, 8, 1, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 8, 1, 0, 8, 1, 4, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 2, 1, 0, 7, 1, 0, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 4, 0, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 2, 5, 0, 4, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 8, 2, 5, 8, 4, 5, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 8, 2, 0, 8, 4, 0, Blocks.brick_block, Blocks.brick_block, false);
		fillWithBlocks(world, structureboundingbox, 0, 2, 1, 0, 4, 4, Blocks.planks, Blocks.planks, false);
		fillWithBlocks(world, structureboundingbox, 1, 2, 5, 7, 4, 5, Blocks.planks, Blocks.planks, false);
		fillWithBlocks(world, structureboundingbox, 8, 2, 1, 8, 4, 4, Blocks.planks, Blocks.planks, false);
		fillWithBlocks(world, structureboundingbox, 1, 2, 0, 7, 4, 0, Blocks.planks, Blocks.planks, false);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 4, 2, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 2, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 2, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 4, 3, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 3, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 3, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 2, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 3, 2, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 3, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 2, 2, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 2, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 3, 2, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 3, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 2, 3, 5, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 2, 2, 5, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 3, 2, 5, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 3, 3, 5, structureboundingbox);

		for (int z = 0; z < 9; z++) {
			placeBlockAtCurrentPosition(world, Blocks.brick_block, 0, z, 0, 6, structureboundingbox);
			placeBlockAtCurrentPosition(world, Blocks.stone_slab, 4, z, 0, 7, structureboundingbox);
			placeBlockAtCurrentPosition(world, Blocks.rail, 0, z, 0, 8, structureboundingbox);
			placeBlockAtCurrentPosition(world, Blocks.stonebrick, 0, z, -1, 8, structureboundingbox);
		}
		placeBlockAtCurrentPosition(world, Blocks.brick_block, 0, 2, 0, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 2, 1, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 2, 2, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 2, 3, 7, structureboundingbox);

		placeBlockAtCurrentPosition(world, Blocks.brick_block, 0, 6, 0, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 6, 1, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 6, 2, 7, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 6, 3, 7, structureboundingbox);
		fillWithBlocks(world, structureboundingbox, 1, 4, 7, 7, 4, 7, Blocks.wooden_slab, Blocks.wooden_slab, false);
		fillWithBlocks(world, structureboundingbox, 1, 4, 6, 7, 4, 6, Blocks.wooden_slab, Blocks.wooden_slab, false);
		fillWithBlocks(world, structureboundingbox, 1, 4, 8, 7, 4, 8, Blocks.wooden_slab, Blocks.wooden_slab, false);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 0, 1, 6, structureboundingbox);
		placeBlockAtCurrentPosition(world, TCBlocks.lantern, 0, 0, 2, 6, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 8, 1, 6, structureboundingbox);
		placeBlockAtCurrentPosition(world, TCBlocks.lantern, 0, 8, 2, 6, structureboundingbox);

		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 2, 5, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 2, 5, structureboundingbox);
		fillWithBlocks(world, structureboundingbox, 1, 4, 1, 7, 4, 1, Blocks.planks, Blocks.planks, false);
		fillWithBlocks(world, structureboundingbox, 1, 4, 4, 7, 4, 4, Blocks.planks, Blocks.planks, false);
		fillWithBlocks(world, structureboundingbox, 4, 3, 4, 7, 3, 4, Blocks.bookshelf, Blocks.bookshelf, false);
		fillWithBlocks(world, structureboundingbox, 5, 3, 5, 6, 3, 5, Blocks.glass_pane, Blocks.glass_pane, false);
		fillWithBlocks(world, structureboundingbox, 4, 3, 3, 7, 3, 3, Blocks.iron_bars, Blocks.iron_bars, false);
		placeBlockAtCurrentPosition(world, TCBlocks.lantern, 0, 7, 4, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, TCBlocks.lantern, 0, 4, 4, 3, structureboundingbox);

		placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 4, 2, 3, structureboundingbox);
		fillWithBlocks(world, structureboundingbox, 4, 1, 3, 7, 1, 3, Blocks.iron_bars, Blocks.iron_bars, false);
		placeBlockAtCurrentPosition(world, Blocks.planks, 0, 7, 1, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.planks, 0, 7, 1, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.planks, 0, 4, 1, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.planks, 0, 4, 1, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 4, 2, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 4, 2, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 7, 2, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 7, 2, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.planks, 0, 1, 1, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.oak_stairs, getMetadataWithOffset(Blocks.oak_stairs, 1), 1, 1, 3, structureboundingbox);
		k = getMetadataWithOffset(Blocks.oak_stairs, 3);
		placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 3, 1, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 2, 1, 4, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.fence, 0, 2, 1, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.wooden_pressure_plate, 0, 2, 2, 3, structureboundingbox);
		placeBlockAtCurrentPosition(world, TCBlocks.trainWorkbench, 0, 7, 1, 1, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.air, 0, 1, 1, 0, structureboundingbox);
		placeBlockAtCurrentPosition(world, Blocks.air, 0, 1, 2, 0, structureboundingbox);
		placeDoorAtCurrentPosition(world, structureboundingbox, random, 1, 1, 0, getMetadataWithOffset(Blocks.wooden_door, 1));

		if (getBlockAtCurrentPosition(world, 1, 0, -1, structureboundingbox) == Blocks.air && getBlockAtCurrentPosition(world, 1, -1, -1, structureboundingbox) != Blocks.air) {
			placeBlockAtCurrentPosition(world, Blocks.stone_slab, 4, 1, 0, -1, structureboundingbox);
		}

		for (l = 0; l < 6; ++l) {
			for (int i1 = 0; i1 < 9; ++i1) {
				clearCurrentPositionBlocksUpwards(world, i1, 9, l, structureboundingbox);
				func_151554_b(world, Blocks.brick_block, 0, i1, -1, l, structureboundingbox);
			}
		}

		spawnVillagers(world, structureboundingbox, 6, 1, 4, 1);
		spawnTrainstationCart(world, random, structureboundingbox, getXWithOffset(6, 8), getYWithOffset(1), getZWithOffset(6, 8));
		spawnTrainstationCart(world, random, structureboundingbox, getXWithOffset(3, 8), getYWithOffset(1), getZWithOffset(3, 8));

		return true;
	}

	private void spawnTrainstationCart(World world, Random random, StructureBoundingBox structureboundingbox, int j, int k, int l) {
		if (structureboundingbox.isVecInside(j, k, l) && !Traincraft.instance.trainstationRecords.isEmpty()) {
			TrainRecord record = Traincraft.instance.trainstationRecords.get(random.nextInt(Traincraft.instance.trainstationRecords.size()-1));
			EntityRollingStock cart = (EntityRollingStock)record.getEntity(world);

			if (cart != null) {
				cart.setLocationAndAngles(j + 0.5D, k, l + 0.5D, 90.0F, 0.0F);
				cart.shouldChunkLoad = false;
				List<TransportSkin> skins = new LinkedList<>(SkinRegistry.get(cart).values());
				if (skins != null && !skins.isEmpty()) {
					cart.setColor(skins.get(new Random().nextInt((skins.size() - 1))).addr);
				}
				world.spawnEntityInWorld(cart);
				cart.setInformation("VillagerJoe", "VillagerJoe", cart.getCartItem().getItem().getItemStackDisplayName(cart.getCartItem()), -1);
			}
		}
	}

	/**
	 * Returns the villager type to spawn in this component, based on the number of villagers already spawned.
	 */
	@Override
	protected int getVillagerType(int par1) {
		return 86;
	}
}