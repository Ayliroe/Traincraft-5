package train.common.api;

import net.minecraft.world.World;
import train.common.api.components.SnowPlow;

public abstract class AbstractSteamSnowPlow extends SteamTrain {

    SnowPlow snowPlow = new SnowPlow(this);

    public AbstractSteamSnowPlow(World world) {
        super(world);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        checkInvent(cargoItems[0], cargoItems[1], this);

        snowPlow.updatePlow();
    }

}
