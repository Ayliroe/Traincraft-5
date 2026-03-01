package train.common.entity.rollingStockOld.steam;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.LiquidManager;
import train.common.api.SteamTrain;
import train.common.api.TextureDescription;

public class EntityLocoSteamForneyRed extends SteamTrain {
	public EntityLocoSteamForneyRed(World world) {
		super(world, LiquidManager.WATER_FILTER);
		textureDescriptionMap.put("Default", new TextureDescription(null, "Forney locomotives are considered as a type of tank engine, small and powerful! The characteristics of this locomotive consisted of a pilot truck (if built with it), four drivers with the second set without flanges for tight turns, and a trailing truck/bogie of two sets of wheels. This little puppy was created to make tight turns conventional locomotives couldn’t. These mainly operated on commuter lines in New York, Chicago, & Boston. The most recognizable ones are from Disneyland No. 3 and the Maine Narrow Gauge Railroad Co. locomotives which the TC models are based off.\n"));
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1F;
	}

	@Override
	public float[][] getRiderOffsets(){return new float[][]{{0.4f,1.6f, 0f}};}
}