package ebf.tim.gui;

import ebf.tim.entities.EntitySeat;
import ebf.tim.networking.PacketSeatUpdate;
import ebf.tim.utility.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import train.common.Traincraft;
import train.common.api.EntityRollingStock;

import javax.vecmath.Vector2f;
import java.util.*;


/**
 * <h1>Transport GUI</h1>
 * used to draw the GUI for choosing a seat while in a piece of rollingstock (get here from a button in the normal UI).
 * @author broscolotos
 */
public class GUISeatManager extends GuiScreen {
    public final EntityRollingStock entity;
    public final int seatCount;

    public int currentSeat;

    public int lastClickTick = 0;

    public static int guiTop;
    public static int guiLeft;

    public ArrayList<Vector2f> locations = new ArrayList<>();

    public GUISeatManager(EntityRollingStock transport) {
        entity=transport;
        seatCount = transport.seats.size();
    }
    public GUISeatManager(EntityPlayer player, EntityRollingStock transport) {
        this(transport);
    }

    @Override
    public void initGui() {}

    public static int percentTop(int value){return (int)(guiTop*(value*0.01f));}
    public static int percentLeft(int value){return (int)(guiLeft*(value*0.01f));}

    @Override
    public void drawScreen(int parWidth, int parHeight, float p_73863_3_) {
        drawDefaultBackground();
        super.drawScreen(parWidth, parHeight, p_73863_3_);
        locations.clear();
        guiLeft=new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight).getScaledWidth();
        guiTop=new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight).getScaledHeight();

        if (seatCount != 0) {
            for (int i = 0; i < seatCount; i++) {
                if (entity.seats.getPassengerAtIndex(i) == mc.thePlayer)
                    currentSeat = i;
            }

            defineButtons();
            guiSeatManager();

            for (Object guiButton : buttonList) {
                if (guiButton instanceof GUIButton) {
                    ((GUIButton) guiButton).drawText(parWidth - (int) guiLeft, parHeight - (int) guiTop);
                }
            }
        }
    }
    @Override
    public boolean doesGuiPauseGame() {return false;}

    public void defineButtons(){

        buttonList =new ArrayList();
        int column = 0;
        int activeRow = 0;
        for (int i = 0; i < seatCount; i++) {
            if (percentTop(25) + (28 * activeRow + 1) - 10 > percentTop(60)) {
                column++;
                activeRow = 0;
            }
            activeRow++;
        }
        int totalColumns = column;
        column = 0;
        activeRow =0;
        for (int i = 0; i < seatCount; i++) {
            if (percentTop(25)+(28*activeRow+1)-10>percentTop(60)) {
                column++;
                activeRow=0;
            }
            int width = fontRendererObj.getStringWidth("Empty Seat") + 6;

            Entity passenger = entity.seats.getPassengerAtIndex(i);
            if (passenger instanceof EntityPlayer) {

                if (fontRendererObj.getStringWidth(passenger.getCommandSenderName()) > width) {
                    width = fontRendererObj.getStringWidth(passenger.getCommandSenderName()) + 7;
                    if(width%2 !=0)
                        width++;
                }
                buttonList.add(
                        new GUIButton(percentLeft(50)-60-(totalColumns/2)+(column*120)-(width/2), percentTop(25)+(28*activeRow+1)-11, width, 20, passenger.getCommandSenderName()) {
                            @Override
                            public String getHoverText() {
                                return "Seat is currently occupied";
                            }
                            @Override
                            public void onClick() {}
                            @Override
                            public FontRenderer getFont(){return fontRendererObj;}
                        }
                );
            } else { //TODO: add an else if for things that aren't players. this becomes important for stuff like stock cars or putting villagers in player seats
                buttonList.add(
                        new GUIButton( percentLeft(50)-60-(totalColumns/2)+(column*120)-(width/2), percentTop(25)+(28*activeRow+1)-11, width,20,"Empty Seat") {
                            @Override
                            public String getHoverText() {
                                return "Seat is currently Empty";
                            }
                            @Override
                            public void onClick() {
                                if (lastClickTick+5 < entity.ticksExisted) {
                                    Traincraft.updateChannel.sendToServer(new PacketSeatUpdate(entity.getEntityId(), mc.thePlayer.getEntityId(), currentSeat, buttonList.indexOf(this), entity.dimension));
                                    lastClickTick = entity.ticksExisted;
                                }
                            }
                            @Override
                            public FontRenderer getFont(){return fontRendererObj;}
                        }
                );
            }
            locations.add(new Vector2f(percentLeft(50)-88-(totalColumns/2)+(column*120)-(width/2),percentTop(25)+(28*activeRow+1)-13));
            activeRow++;
        }
        buttonList.add(
                new GUIButton( percentLeft(50)-((fontRendererObj.getStringWidth("Close")+7)/2), percentTop(75)-10, fontRendererObj.getStringWidth("Close")+7,20,"Close") {
                    @Override
                    public String getHoverText() {
                        return "Close Inventory";
                    }
                    @Override
                    public void onClick() {
                        mc.displayGuiScreen(null);
                    }
                    @Override
                    public FontRenderer getFont(){return fontRendererObj;}
                }
        );

    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton instanceof GUIButton) {
            ((GUIButton) guibutton).onClick();
        }
    }


    public void guiSeatManager(){
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();
        mc.getTextureManager().bindTexture(ClientUtil.vanillaInventory);
        for(Vector2f pos : locations) {
            ClientUtil.drawTexturedRect(pos.x-3, pos.y-3, 54, 51, 30, 30, 20, 20);
        }

        for(int i = 0; i < seatCount; i++) {
            Entity passenger = entity.seats.getPassengerAtIndex(i);
            if (passenger instanceof AbstractClientPlayer) {
                mc.getTextureManager().bindTexture(((AbstractClientPlayer) passenger).getLocationSkin());
                ClientUtil.drawTexturedRect(locations.get(i).x, locations.get(i).y, 32, 64, 24, 24, 32, 64);
            } else if (passenger instanceof EntityPlayer) {
                mc.getTextureManager().bindTexture(Minecraft.getMinecraft().thePlayer.getLocationSkin());
                ClientUtil.drawTexturedRect(locations.get(i).x, locations.get(i).y, 32, 64, 24, 24, 32, 64);
            }
        }
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
