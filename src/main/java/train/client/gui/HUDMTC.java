package train.client.gui;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import train.common.api.Locomotive;
import train.common.api.MTC;
import train.common.library.Info;

public class HUDMTC extends GuiScreen {

    private Minecraft game;
    private int windowWidth, windowHeight;
    public boolean mtcIconOnOff = true;

    //test15
    @SubscribeEvent
    public void onGameRender(RenderGameOverlayEvent.Text event) {
        if (game != null
				&& game.thePlayer != null
				&& game.thePlayer.ridingEntity != null
				&& game.thePlayer.ridingEntity instanceof Locomotive && Minecraft.isGuiEnabled()
				&& game.currentScreen == null) {
            renderSkillHUD(event, (Locomotive) game.thePlayer.ridingEntity);
        } else {
            this.game = this.mc = Minecraft.getMinecraft();
            this.fontRendererObj = this.game.fontRenderer;
        }
    }

    public void renderSkillHUD(RenderGameOverlayEvent event, Locomotive rcCar) {
        windowWidth = event.resolution.getScaledWidth();
        windowHeight = event.resolution.getScaledHeight() - 100;

        MTC MTC = rcCar.MTC;

        if (MTC.mtcStatus == 1 || MTC.mtcStatus == 2) {
			// Steam Trains have water.
            int width = this.game.fontRenderer.getStringWidth("Speed Limit: " + MTC.speedLimit + " km/h");

            int height = this.game.fontRenderer.FONT_HEIGHT;
            int padding = 2;
            int margin = 4;

            Gui.drawRect(margin, margin, margin + width + padding * 2 + 70, margin + height + padding + padding + 35, 0xAA000000);

			this.drawString(this.game.fontRenderer, "Speed Limit: " + MTC.speedLimit + " km/h" + (MTC.atoStatus == 1 ? ", ATO on" : ""), margin + 4, margin, 14737632);
            this.drawString(this.game.fontRenderer, "Next Speed Limit: " + MTC.nextSpeedLimit + " km/h", margin + 4, margin + 10, 14737632);

            MTC.distanceFromStopPoint = rcCar.getDistance(MTC.xFromStopPoint, MTC.yFromStopPoint, MTC.zFromStopPoint);
            MTC.distanceFromSpeedChange = rcCar.getDistance(MTC.xSpeedLimitChange, MTC.ySpeedLimitChange, MTC.zSpeedLimitChange);
            MTC.distanceFromStationStop = rcCar.getDistance(MTC.xStationStop, MTC.yStationStop, MTC.zStationStop);

            if (MTC.xFromStopPoint != 0 && MTC.yFromStopPoint != 0 && MTC.zFromStopPoint != 0) {
                this.drawString(this.game.fontRenderer, "Stop in " + Math.round(MTC.distanceFromStopPoint) + " blocks.", margin + 4, margin + 19, 14737632);
            }

            if (MTC.xFromStopPoint == 0 && MTC.yFromStopPoint == 0 && MTC.zFromStopPoint == 0 && MTC.xStationStop != 0 && MTC.yStationStop != 0) {
                this.drawString(this.game.fontRenderer, "Station stop in " + Math.round(MTC.distanceFromStationStop) + " blocks.", margin + 4, margin + 19, 14737632);
            }

            if (MTC.xSpeedLimitChange != 0 && MTC.ySpeedLimitChange != 0 && MTC.zSpeedLimitChange != 0) {
                this.drawString(this.game.fontRenderer, "Next speed limit in " + Math.round(MTC.distanceFromSpeedChange) + " blocks.", margin + 4, margin + 28, 14737632);
            }

            if (MTC.speedLimit < rcCar.getSpeed() && !MTC.overspeedOveridePressed) {
                drawTexturedRect(new ResourceLocation(Info.resourceLocation, Info.guiPrefix + "mtcspeeding.png"), 30, 45, 0, 0, 64, 64, 64, 64, 0.25);
            } else if (MTC.overspeedOveridePressed) {
                drawTexturedRect(new ResourceLocation(Info.resourceLocation, Info.guiPrefix + "mtcspeedingoverride.png"), 30, 45, 0, 0, 64, 64, 64, 64, 0.25);
            }
        }

        if (rcCar.ticksExisted % 21 == 0 && MTC.mtcStatus == 2) {
            mtcIconOnOff = !mtcIconOnOff;
        } else if (MTC.mtcStatus == 1) {
            mtcIconOnOff = true;
        }

        if (MTC.mtcOverridePressed) {
            drawTexturedRect(new ResourceLocation(Info.resourceLocation, Info.guiPrefix + "mtcdisable.png"), 12, 45, 0, 0, 64, 64, 64, 64, 0.25);
        } else if (MTC.mtcStatus == 1 || MTC.mtcStatus == 2) {
            if (mtcIconOnOff) {
                if (MTC.mtcType == 1 || MTC.mtcType == 0) {
                    drawTexturedRect(new ResourceLocation(Info.resourceLocation, Info.guiPrefix + "mtcicon.png"), 12, 45, 0, 0, 64, 64, 64, 64, 0.25);
                } else {
                    drawTexturedRect(new ResourceLocation(Info.resourceLocation, Info.guiPrefix + "mtcicon2.png"), 12, 45, 0, 0, 64, 64, 64, 64, 0.25);
                }
            }
        }
    }

    public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        double minU = (double) u / (double) imageWidth;
        double maxU = (double) (u + width) / (double) imageWidth;
        double minV = (double) v / (double) imageHeight;
        double maxV = (double) (v + height) / (double) imageHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + scale * (double) width, y + scale * (double) height, 0, maxU, maxV);
        tessellator.addVertexWithUV(x + scale * (double) width, y, 0, maxU, minV);
        tessellator.addVertexWithUV(x, y, 0, minU, minV);
        tessellator.addVertexWithUV(x, y + scale * (double) height, 0, minU, maxV);
        tessellator.draw();
    }
}
