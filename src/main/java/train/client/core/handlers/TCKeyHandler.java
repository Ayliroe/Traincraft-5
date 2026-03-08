package train.client.core.handlers;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import ebf.tim.entities.EntitySeat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
import train.client.gui.GuiMTCInfo;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.api.Locomotive;
import train.common.api.SteamTrain;
import train.common.core.handlers.ConfigHandler;
import train.common.core.network.PacketKeyPress;
import train.common.entity.zeppelin.AbstractZeppelin;

public class TCKeyHandler {
    public static KeyBinding horn;
    public static KeyBinding bell;
    public static KeyBinding inventory;
    public static KeyBinding up;
    public static KeyBinding down;
    public static KeyBinding idle;
    public static KeyBinding furnace;
    public static KeyBinding MTCScreen;
    public static KeyBinding toggleATO;
    public static KeyBinding mtcOverride;
    public static KeyBinding overspeedOverride;

    public TCKeyHandler() {
        horn = new KeyBinding("key.traincraft.horn", Keyboard.KEY_H, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(horn);
        inventory = new KeyBinding("key.traincraft.inventory", Keyboard.KEY_R, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(inventory);
        up = new KeyBinding("key.traincraft.up", Keyboard.KEY_Y, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(up);
        down = new KeyBinding("key.traincraft.down", Keyboard.KEY_X, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(down);
        idle = new KeyBinding("key.traincraft.idle", Keyboard.KEY_C, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(idle);
        furnace = new KeyBinding("key.traincraft.furnace", Keyboard.KEY_F, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(furnace);
        bell = new KeyBinding("key.traincraft.bell", Keyboard.KEY_B, "key.categories.traincraft");
        ClientRegistry.registerKeyBinding(bell);

        if (Traincraft.hasComputerCraft()) {
            MTCScreen = new KeyBinding("key.traincraft.showMTCScreen", Keyboard.KEY_M, "key.categories.traincraft");
            ClientRegistry.registerKeyBinding(MTCScreen);
            toggleATO = new KeyBinding("key.traincraft.toggleATO", Keyboard.KEY_O, "key.categories.traincraft");
            ClientRegistry.registerKeyBinding(toggleATO);
            mtcOverride = new KeyBinding("key.traincraft.mtcOverride", Keyboard.KEY_O, "key.categories.traincraft");
            ClientRegistry.registerKeyBinding(mtcOverride);
            overspeedOverride = new KeyBinding("key.traincraft.overspeedOverride", Keyboard.KEY_L, "key.categories.traincraft");
            ClientRegistry.registerKeyBinding(overspeedOverride);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()){
            return;
        }

        Entity riding =Minecraft.getMinecraft().thePlayer.ridingEntity;
        if(riding instanceof EntitySeat){
            riding=((EntitySeat) riding).parent;
        }
        if (riding instanceof AbstractTrains || riding instanceof AbstractZeppelin){
            if (up.getIsKeyPressed()) {
                sendKeyControlsPacket(0);
            }

            if (down.getIsKeyPressed()) {
                sendKeyControlsPacket(2);
            }

            if (idle.getIsKeyPressed()) {
                sendKeyControlsPacket(6);
            }

            if (inventory.getIsKeyPressed()) {
                sendKeyControlsPacket(7);
            }

            if (furnace.getIsKeyPressed()) {
                sendKeyControlsPacket(9);
            }

            if (FMLClientHandler.instance().getClient().gameSettings.keyBindSneak.getIsKeyPressed() && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                sendKeyControlsPacket(404);
            }
        }

        if (riding instanceof Locomotive) {

            if (horn.getIsKeyPressed()) {
                sendKeyControlsPacket(8);
            }

            if (bell.getIsKeyPressed()) {
                sendKeyControlsPacket(10);
            }



            if (Keyboard.isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindForward.getKeyCode())
                    && !((Locomotive) riding).forwardPressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(4));
                ((Locomotive) riding).forwardPressed = true;
            } else if (!Keyboard
                    .isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindForward.getKeyCode())
                    && ((Locomotive) riding).forwardPressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(13));
                ((Locomotive) riding).forwardPressed = false;
            }
            if (Keyboard.isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindBack.getKeyCode())
                    && !((Locomotive) riding).backwardPressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(5));
                ((Locomotive) riding).backwardPressed = true;
            } else if (!Keyboard
                    .isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindBack.getKeyCode())
                    && ((Locomotive) riding).backwardPressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(14));
                ((Locomotive) riding).backwardPressed = false;
            }
            if (Keyboard.isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindJump.getKeyCode())
                    && !((Locomotive) riding).brakePressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(12));
                ((Locomotive) riding).brakePressed = true;
            } else if (!Keyboard
                    .isKeyDown(FMLClientHandler.instance().getClient().gameSettings.keyBindJump.getKeyCode())
                    && ((Locomotive) riding).brakePressed) {
                Traincraft.keyChannel.sendToServer(new PacketKeyPress(15));
                ((Locomotive) riding).brakePressed = false;
            }
            if (Traincraft.hasComputerCraft()) {
                if (MTCScreen.getIsKeyPressed() && !FMLClientHandler.instance().isGUIOpen(GuiMTCInfo.class)) {
                    if (Minecraft.getMinecraft().thePlayer.ridingEntity != null) {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiMTCInfo(Minecraft.getMinecraft().thePlayer.ridingEntity));
                    }
                }

                if (toggleATO.getIsKeyPressed() && Minecraft.getMinecraft().thePlayer.ridingEntity instanceof Locomotive) {
                    sendKeyControlsPacket(16);

                    Locomotive train = (Locomotive) Minecraft.getMinecraft().thePlayer.ridingEntity;
                    train.MTC.toggleATO();
                }

                if (mtcOverride.getIsKeyPressed() && Minecraft.getMinecraft().thePlayer.ridingEntity instanceof Locomotive) {
                    sendKeyControlsPacket(17);

                    Locomotive train = (Locomotive) Minecraft.getMinecraft().thePlayer.ridingEntity;
                    train.MTC.toggleMTCOverride();
                }

                if (overspeedOverride.getIsKeyPressed() && Minecraft.getMinecraft().thePlayer.ridingEntity instanceof Locomotive) {
                    sendKeyControlsPacket(18);

                    Locomotive train = (Locomotive) Minecraft.getMinecraft().thePlayer.ridingEntity;
                    train.MTC.toggleOverspeedOverride();
                }
            }
        }
    }


    private static void sendKeyControlsPacket(int key) {
        Traincraft.keyChannel.sendToServer(new PacketKeyPress(key));
    }
}