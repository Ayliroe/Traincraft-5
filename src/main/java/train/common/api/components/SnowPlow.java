package train.common.api.components;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import train.common.api.Freight;
import train.common.api.Locomotive;
import train.common.core.FakePlayer;
import train.common.core.util.TraincraftUtil;

import java.util.Random;

public class SnowPlow {

    private static final float radianF = (float) Math.PI / 180.0f;
    private static final double[][]	blockpos	= { { 4, 0, 1 }, { 4, 0, -1 }, { 4, 0, 0 }};

    private FakePlayer fakePlayer = null;
    private final Freight train;

    public SnowPlow(Freight train) {
        this.train = train;
    }

    public void updatePlow() {
        if (train.getWorld().isRemote) {
            return;
        }

        if (fakePlayer == null)
            fakePlayer = new FakePlayer(train.getWorld());

        int rotation = MathHelper.floor_float(train.bogies.yaw() + 180f);

        double[] point1 = rotateVec3(blockpos[0], train.rotationPitch, rotation);
        point1[0] += train.posX;
        point1[1] += train.posY;
        point1[2] += train.posZ;
        mineSnow(point1);
        point1[1]++;
        mineSnow(point1);
        point1[1]++;
        mineSnow(point1);

        point1 = rotateVec3(blockpos[1], train.rotationPitch, rotation);
        point1[0] += train.posX;
        point1[1] += train.posY;
        point1[2] += train.posZ;
        mineSnow(point1);
        point1[1]++;
        mineSnow(point1);
        point1[1]++;
        mineSnow(point1);

        point1 = rotateVec3(blockpos[2], train.rotationPitch, rotation);
        point1[0] += train.posX;
        point1[1] += train.posY+1;
        point1[2] += train.posZ;
        mineSnow(point1);
        point1[1]++;
        mineSnow(point1);
    }

    private void mineSnow(double[] point){
        ItemStack[] cargoItems = train.cargoItems;
        Block b = train.getWorld().getBlock(MathHelper.floor_double(point[0]),MathHelper.floor_double(point[1]),MathHelper.floor_double(point[2]));


        int blockMeta = train.getWorld().getBlockMetadata(MathHelper.floor_double(point[0]), MathHelper.floor_double(point[1]),
                MathHelper.floor_double(point[2]));

        if((b == Blocks.snow || b == Blocks.snow_layer) && b.canHarvestBlock(fakePlayer, blockMeta)){
            train.getWorld().setBlockToAir(MathHelper.floor_double(point[0]),MathHelper.floor_double(point[1]),MathHelper.floor_double(point[2]));
            int snowballs = new Random().nextInt(9);
            for(int i=2; i<cargoItems.length && snowballs>0; i++){
                if (cargoItems[i] == null){
                    cargoItems[i] = new ItemStack(Items.snowball, snowballs);
                    snowballs--;
                } else if (cargoItems[i].getItem() == Items.snowball && cargoItems[i].stackSize < Items.snowball.getItemStackLimit()){
                    while (cargoItems[i].stackSize < cargoItems[i].getMaxStackSize() && snowballs >0){
                        cargoItems[i].stackSize++;
                        snowballs--;
                    }
                }
                if (snowballs ==0){
                    break;
                }
            }
            if (snowballs >0){
                EntityItem entityitem = new EntityItem(train.getWorld(), point[0], point[1] + 1, point[2], new ItemStack(Items.snowball, snowballs));
                entityitem.delayBeforeCanPickup = 10;
                train.getWorld().spawnEntityInWorld(entityitem);

            }
        }
    }

    private double[] rotateVec3(double[] offset, float pitch, float yaw) {
        double[] xyz = new double[]{offset[0],offset[1],offset[2]};
        //rotate pitch
        if (pitch != 0.0F) {
            pitch *= radianF;

            xyz[0] = (offset[0] * Math.cos(pitch));
            xyz[1] = (offset[0] * Math.sin(pitch));
        }
        //rotate yaw
        if (yaw != 0.0F) {
            yaw *= radianF;
            double cos = MathHelper.cos(yaw);
            double sin = MathHelper.sin(yaw);

            xyz[0] = (offset[0] * cos) - (offset[2] * sin);
            xyz[2] = (offset[0] * sin) + (offset[2] * cos);
        }
        return xyz;
    }
}
