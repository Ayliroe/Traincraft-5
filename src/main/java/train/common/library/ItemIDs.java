/*******************************************************************************
 * Copyright (c) 2012 mrbrutal. All rights reserved.
 *
 * @name TrainCraft
 * @author mrbrutal
 ******************************************************************************/

package train.common.library;

import net.minecraft.item.Item;
import train.common.api.IItemIDs;

public enum ItemIDs implements IItemIDs {
	diesel("ItemContainer", "item_diesel_filled", 20),
	stake("ItemStacked", "item_stake", 1),
	steeldust("ItemTrain", "item_steeldust", 0),
	coaldust("ItemTrain", "item_coaldust", 15),
	graphite("ItemTrain", "item_graphite", 10),
	boiler("ItemTrain", "item_boiler_steel", 5),
	firebox("ItemTrain", "item_firebox_steel", 5),
	bogie("ItemTrain", "item_bogie_steel", 4),
	steelframe("ItemTrain", "item_frame_steel", 6),
	steelcab("ItemTrain", "item_cab_steel", 5),
	steelchimney("ItemTrain", "item_chimney_steel", 5),
	airship("ItemZeppelin", "item_zeppelin", 1),
	balloon("ItemTrain", "item_balloon", 7),
	propeller("ItemTrain", "item_propeller", 6),
	steamengine("ItemTrain", "item_engine_steam", 2),
	dieselengine("ItemTrain", "item_engine_diesel", 1),
	piston("ItemTrain", "item_piston", 9),
	camshaft("ItemTrain", "item_camshaft", 8),
	cylinder("ItemTrain", "item_cylinder", 7),
	electmotor("ItemTrain", "item_engine_electric", 1),
	woodenFrame("ItemTrain", "item_frame_wood", 12),
	woodenBogie("ItemTrain", "item_bogie_wood", 15),
	woodenCab("ItemTrain", "item_cab_wood", 10),
	ironChimney("ItemTrain", "item_chimney_iron", 7),
	ironFirebox("ItemTrain", "item_firebox_iron", 4),
	ironBoiler("ItemTrain", "item_boiler_iron", 5),
	ironFrame("ItemTrain", "item_frame_iron", 6),
	ironBogie("ItemTrain", "item_bogie_iron", 8),
	ironCab("ItemTrain", "item_cab_iron", 5),
	steel("ItemTrain", "item_steel", 4),
	refinedFuel("ItemContainer", "item_fuel_filled", 15),
	signal("ItemSignal", "item_signal", 0),
	kSignal("ItemsignalSpanish", "signalK", 2),
	seats("ItemTrain", "item_seats", 15),
	transformer("ItemTrain", "item_transformer", 4),
	controls("ItemTrain", "item_controls", 7),
	transmition("ItemTrain", "item_transmition", 5),
	generator("ItemTrain", "item_generator", 5),
	overalls("ItemTCArmor", "armour_overalls", 1),
	jacket("ItemTCArmor", "armour_jacket", 1),
	hat("ItemTCArmor", "armour_hat", 1),
	rawPlastic("ItemTrain", "item_plastic", 20),
	ingotCopper("ItemTrain", "item_copper", 9),
	copperWireFine("ItemTrain", "item_wire", 4),
	electronicCircuit("ItemTrain", "item_circuit", 2),
	chunkLoaderActivator("ItemChunkLoaderActivator", "item_chunk_loader", 1),
	//refinedFuelLiquid("ItemTrain", "item_liquid_fuel"),
	//dieselLiquid("ItemTrain", "item_liquid_diesel"),
	emptyCanister("ItemContainer", "item_canister", 40),
	//copperRail("ItemRail", "item_rail_copper", 1),
	//steelRail("ItemRail", "item_rail_steel", 1),
	copperRail("ItemTrain", "item_rail_copper", 1),
	steelRail("ItemTrain", "item_rail_steel", 1),
	recipeBook("ItemRecipeBook", "item_book_blue", 1),
	adminBook("ItemAdminBook", "item_book_blue", 0),
	trackDebugger("ItemTrackDebugger", "item_composite_wrench", 0),
	wirelessTransmitter("ItemWirelessTransmitter", "wireless_transmitter", 3),
	paintbrushThing("ItemPaintbrushThing", "paintbrushThing", 1),
	whistle("ItemWhistle", "whistle", 1),
	padlock("ItemPadlock", "padlock", 1),
	bolt("ItemBolt", "bolt", 1),
	hat_ticketMan_paintable("ItemTCArmor", "armor_ticket_man_hat", 1),
	pants_ticketMan_paintable("ItemTCArmor", "armor_ticket_man_pants", 1),
	jacket_ticketMan_paintable("ItemTCArmor", "armor_ticket_man_jacket", 1),
	hat_driver_paintable("ItemTCArmor", "armor_driver_hat", 1),
	pants_driver_paintable("ItemTCArmor", "armor_driver_pants", 1),
	jacket_driver_paintable("ItemTCArmor", "armor_driver_jacket", 1),

	helmet_suit_paintable("ItemTCArmor", "armor_composite_helmet", 1),
	pants_suit_paintable("ItemTCArmor", "armor_composite_pants", 1),
	boots_suit_paintable("ItemTCArmor", "armor_composite_boots", 1),
	jacket_suit_paintable("ItemTCArmor", "armor_composite_chest", 1),
	reinforcedPlastic("ItemTrain", "item_fiberglass_plate", 10),
	reinforcedPlates("ItemTrain", "item_reinforced_plate", 10),
	composite_wrench("ItemWrench", "item_composite_wrench", 1),

	/**
	 * Normal Tracks
	 */
	tcRailSmallStraight("ItemTCRail", "item_rail_straight_small", 5),
	tcRailMediumStraight("ItemTCRail", "item_rail_straight_medium", 5),
	tcRailLongStraight("ItemTCRail", "item_rail_straight_large", 5),
	tcRailVeryLongStraight("ItemTCRail", "item_rail_straight_very_large", 5),

	tcRailSmallDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_small", 5),
	tcRailMediumDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_medium", 5),
	tcRailLongDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_long", 5),
	tcRailVeryLongDiagonalStraight("ItemTCRail", "item_rail_diagonal_straight_very_long", 5),

	tcRail1X1Turn("ItemTCRail", "item_rail_tc_turn_1", 5),
	tcRailMediumTurn("ItemTCRail", "item_rail_turn_medium", 5),
	tcRailLargeTurn("ItemTCRail", "item_rail_turn_large", 5),
	tcRailVeryLargeTurn("ItemTCRail", "item_rail_turn_veryLarge", 5),
	tcRailSuperLargeTurn("ItemTCRail", "item_rail_tc_super_large_turn", 5),
	tcRail29X29Turn("ItemTCRail", "item_rail_tc_turn_29", 5),
	tcRail32X32Turn("ItemTCRail", "item_rail_tc_turn_32", 5),

	tcRailMedium45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_medium", 5),
	tcRailLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_large", 5),
	tcRailVeryLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_very_large", 5),
	tcRailSuperLarge45DegreeTurn("ItemTCRail", "item_rail_45degree_turn_super_large", 5),
	tcRail45DegreeTurn9x20("ItemTCRail", "item_rail_45degree_turn_9x20", 5),
	tcRail45DegreeTurn10x22("ItemTCRail", "item_rail_45degree_turn_10x22", 5),

	tcRailSmallParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_small", 3),
	tcRailMediumParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_medium", 3),
	tcRailLargeParallelCurve("ItemTCRail", "item_rail_tc_parallel_curve_large", 3),
	tcRail20x2SCurve("ItemTCRail", "item_rail_tc_parallel_curve_2x20", 3),

	tcRailTwoWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),
	tcRailDiamondCrossing("ItemTCRail", "item_rail_diamond_crossing", 5),
	tcRailDoubleDiamondCrossing("ItemTCRail", "item_rail_tc_double_diamond_crossing", 5),
	tcRailDiagonalTwoWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),
	tcRailFourWaysCrossing("ItemTCRail", "item_rail_two_ways_crossing", 5),

	tcRailMediumSwitch("ItemTCRail", "item_rail_switch_medium", 5),
	tcRailLargeSwitch("ItemTCRail", "item_rail_switch_large", 5),
	tcRailVeryLargeSwitch("ItemTCRail", "item_rail_switch_very_large", 5),

	tcRailMediumParallelSwitch("ItemTCRail", "item_rail_switch_parallel_4x11", 5),
	tcRailLargeParallelSwitch("ItemTCRail", "item_rail_switch_parallel_4x17", 5),
	tcRailMedium45DegreeSwitch("ItemTCRail", "item_rail_switch_45degree_medium", 5),
	tcRailLarge45DegreeSwitch("ItemTCRail", "item_rail_switch_45degree_large", 5),

	tcRailSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),

	tcRailLargeSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailLargeSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailLargeSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailLargeSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailLargeSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),

	tcRailVeryLargeSlopeWood("ItemTCRail", "item_rail_straight_slope_wood", 3),
	tcRailVeryLargeSlopeGravel("ItemTCRail", "item_rail_straight_slope_gravel", 3),
	tcRailVeryLargeSlopeBallast("ItemTCRail", "item_rail_straight_slope_ballast", 3),
	tcRailVeryLargeSlopeSnowGravel("ItemTCRail", "item_rail_straight_slope_snow_gravel", 3),
	tcRailVeryLargeSlopeDynamic("ItemTCRail", "item_rail_straight_slope_dynamic", 3),

	tcRailLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),
	tcRailVeryLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),
	tcRailSuperLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_slope_curved_large_dynamic", 3),


	/**
	 * Embedded Tracks
	 */
	tcRailEmbeddedSmallStraight("ItemTCRail", "item_rail_straight_embedded_small_", 5),
	tcRailEmbeddedMediumStraight("ItemTCRail", "item_rail_straight_embedded_medium", 5),
	tcRailEmbeddedLongStraight("ItemTCRail", "item_rail_straight_embedded_large", 5),
	tcRailEmbeddedVeryLongStraight("ItemTCRail", "item_rail_straight_embedded_very_large", 5),

	tcRailEmbeddedSmallDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_small", 5),
	tcRailEmbeddedMediumDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_medium", 5),
	tcRailEmbeddedLongDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_long", 5),
	tcRailEmbeddedVeryLongDiagonalStraight("ItemTCRail", "item_rail_embedded_straight_diagonal_very_long", 5),

	tcRailEmbedded1X1Turn("ItemTCRail", "item_rail_tc_embedded_turn_1", 5),
	tcRailEmbeddedMediumTurn("ItemTCRail", "item_rail_tc_embedded_medium_turn", 5),
	tcRailEmbeddedLargeTurn("ItemTCRail", "item_rail_tc_embedded_large_turn", 5),
	tcRailEmbeddedVeryLargeTurn("ItemTCRail", "item_rail_tc_embedded_very_large_turn", 5),
	tcRailEmbeddedSuperLargeTurn("ItemTCRail", "item_rail_tc_embedded_super_large_turn", 5),
	tcRailEmbedded29X29Turn("ItemTCRail", "item_rail_tc_embedded_turn_29", 5),
	tcRailEmbedded32X32Turn("ItemTCRail", "item_rail_tc_embedded_turn_32", 5),

	tcRailEmbeddedMedium45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_medium", 5),
	tcRailEmbeddedLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_large", 5),
	tcRailEmbeddedVeryLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_very_large", 5),
	tcRailEmbeddedSuperLarge45DegreeTurn("ItemTCRail", "item_rail_embedded_45degree_turn_super_large", 5),
	tcRailEmbedded45DegreeTurn9x20("ItemTCRail", "item_rail_embedded_45degree_turn_9x20", 5),
	tcRailEmbedded45DegreeTurn10x22("ItemTCRail", "item_rail_embedded_45degree_turn_10x22", 5),

	tcRailEmbeddedSmallParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_small", 3),
	tcRailEmbeddedMediumParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_medium", 3),
	tcRailEmbeddedLargeParallelCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_large", 3),
	tcRailEmbedded20x2SCurve("ItemTCRail", "item_rail_tc_embedded_parallel_curve_2x20", 3),

	tcRailEmbeddedTwoWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),
	tcRailEmbeddedDiamondCrossing("ItemTCRail", "item_rail_embedded_diamond_crossing", 5),
	tcRailEmbeddedDoubleDiamondCrossing("ItemTCRail", "item_rail_tc_embedded_double_diamond_crossing", 5),
	tcRailEmbeddedDiagonalTwoWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),
	tcRailEmbeddedFourWaysCrossing("ItemTCRail", "item_rail_embedded_two_ways_crossing", 5),

	tcRailEmbeddedMediumSwitch("ItemTCRail", "item_rail_embedded_switch_medium", 5),
	tcRailEmbeddedLargeSwitch("ItemTCRail", "item_rail_embedded_switch_large", 5),
	tcRailEmbeddedVeryLargeSwitch("ItemTCRail", "item_rail_embedded_switch_large", 5),
	tcRailEmbeddedMediumParallelSwitch("ItemTCRail", "item_rail_embedded_switch_parallel_4x11", 5),
	tcRailEmbeddedLargeParallelSwitch("ItemTCRail", "item_rail_embedded_switch_parallel_4x17", 5),
	tcRailEmbeddedMedium45DegreeSwitch("ItemTCRail", "item_rail_embedded_switch_45degree_medium", 5),
	tcRailEmbeddedLarge45DegreeSwitch("ItemTCRail", "item_rail_embedded_switch_45degree_large", 5),

	tcRailEmbeddedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),
	tcRailEmbeddedLargeSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),
	tcRailEmbeddedVeryLargeSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_dynamic", 5),

	tcRailEmbeddedLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),
	tcRailEmbeddedVeryLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),
	tcRailEmbeddedSuperLargeCurvedSlopeDynamic("ItemTCRail", "item_rail_embedded_slope_curved_large_dynamic", 3),

	tcRailSmallRoadCrossing("ItemTCRail", "item_rail_small_road_crossing", 5),
	tcRailSmallRoadCrossing1("ItemTCRail", "item_rail_small_road_crossing_1", 5),
	tcRailSmallRoadCrossing2("ItemTCRail", "item_rail_small_road_crossing_2", 5),
	tcRailSmallRoadCrossingDynamic("ItemTCRail", "item_rail_small_road_crossing_dynamic", 5),

	/**
	 * Zeppelin
	 */

	zeppelin("ItemZeppelin", "item_zeppelin_one_balloon", 1),

	;

	public Item item;
	public String className;
	public String iconName;

	/**
	 * amount for one emerald. For ItemRollingStock, it is the price for one train
	 */
	public int amountForEmerald;



	/**
	 * @param classMethodName
	 * @param iconName
	 * @param amountForEmerald for one emerald. For ItemRollingStock, it is the price for one train
	 */
	ItemIDs(String classMethodName, String iconName, int amountForEmerald) {
		this.className = classMethodName;
		this.iconName = iconName;
		this.amountForEmerald = amountForEmerald;
	}


	@Override
	public Item getItem() {
		return this.item;
	}

	public String getItemName() {
		return this.item.getUnlocalizedName().replace("tc:", "");
	}
}

