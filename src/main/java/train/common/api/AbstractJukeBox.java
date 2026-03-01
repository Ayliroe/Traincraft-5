package train.common.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.core.util.MP3Player;

public abstract class AbstractJukeBox extends EntityRollingStock {

    public boolean isPlaying = false;
    public boolean isInvalid = false;
    public String streamURL = "";
    private Side side;
    public float volume = 1.0f;
    public MP3Player player;

    public AbstractJukeBox(World world) {
        super(world);
        dataWatcher.addObject(22, streamURL);
        dataWatcher.addObject(23, 0);
        side = FMLCommonHandler.instance().getEffectiveSide();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote && ticksExisted % 10 == 0) {
            dataWatcher.updateObject(22, streamURL);
            dataWatcher.updateObject(23, isPlaying ? 1 : 0);
        }
        if (side == Side.CLIENT) {

            if (ticksExisted % 10 == 0 && !isPlaying() && dataWatcher.getWatchableObjectInt(23) != 0) {
                streamURL = dataWatcher.getWatchableObjectString(22);
                startStream();
            }
            if ((Minecraft.getMinecraft().thePlayer != null) && (player != null) && (!isInvalid)) {
                float vol = (float) getDistanceSq(Minecraft.getMinecraft().thePlayer.posX,
                        Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
                if (vol >= (volume * 1000.0F)) {
                    player.setVolume(0.0F);
                } else {
                    float v2 = 10000.0F / vol / 100.0F;
//					System.out.println(vol);
                    if (v2 > 1.0F) {
                        player.setVolume(volume);
                    } else {
                        float v1 = 1.0f - volume;
                        if (v2 - v1 > 0) {
                            v2 = v2 - v1;
                        } else {
                            v2 = 0.0f;
                        }
                        player.setVolume(v2);
                    }
                }
                if (vol == 0) {
                    invalidate();
                }
                if (isPlaying && rand.nextInt(5) == 0 && (player != null && player.isPlaying())) {
                    int random2 = rand.nextInt(24) + 1;
                    worldObj.spawnParticle("note", posX, posY + 1.2D, posZ, random2 / 24.0D, 0.0D, 0.0D);
                }
            }

        }
    }

    /**
     * server side
     *
     * @param url
     * @param playing
     */
    public void recievePacket(String url, boolean playing) {
        streamURL = url;
        isPlaying = playing;
    }

    @SideOnly(Side.CLIENT)
    public void invalidate() {
        isInvalid = true;
        stopStream();
    }

    @SuppressWarnings("static-access")
    public void startStream() {

        if (!isPlaying) {
            isPlaying = true;
            if (side == Side.CLIENT) {
                player = new MP3Player(streamURL, worldObj, getEntityId());
                player.setVolume(0);
                Traincraft.proxy.playerList.add(player);
            }
        }

    }

    @SuppressWarnings("static-access")
    public void stopStream() {

        if (isPlaying) {
            isPlaying = false;
            if (side == Side.CLIENT && player != null) {
                player.stop();
                Traincraft.proxy.playerList.remove(player);
            }
        }

    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setString("StreamUrl", streamURL);
        nbttagcompound.setBoolean("isPlaying", isPlaying());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        streamURL = nbttagcompound.getString("StreamUrl");
        isPlaying = nbttagcompound.getBoolean("isPlaying");
        dataWatcher.updateObject(22, streamURL);
        dataWatcher.updateObject(23, isPlaying ? 1 : 0);
    }
}
