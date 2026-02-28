package train.common.entity.rollingStockOld.diesel;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.DieselTrain;
import train.common.api.LiquidManager;
import train.common.core.util.TraincraftUtil;
import train.common.library.GuiIDs;

public class EntityLocoDieselFOLM1B extends DieselTrain {
    public EntityLocoDieselFOLM1B(World world) {
        super(world, LiquidManager.dieselFilter());
    }

    @Override
    public void updateRiderPosition() {
        TraincraftUtil.updateRider(this, 4.0, 0.35f);
    }

    @Override
    public String getInventoryName() {
        return "FOL-M1B";
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return (1F);
    }
}