package train.common.library;

import com.google.gson.Gson;
import fexcraft.fvtm.BEOModelLoader;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.util.ResourceLocation;
import train.common.Traincraft;
import train.common.library.TraincraftRegistry.TrainRegister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class RenderRecordJson {

    String entryName;
    String model;
    String texture;
    boolean multiTexture;
    float[] trans;
    float[] rotate;
    float[] scale;
    String smokeType;
    int smokeIterations;
    ArrayList<double[]> smokeFX;
    String explosionType;
    ArrayList<double[]> explosionFX;
    int explosionFXIterations;
    boolean hasSmokeOnSlopes;
    String[] bogies;
}

public final class RenderRecord {

    public static void put(Map<String, TrainRegister> trains, String path) {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        RenderRecordJson[] recordsJson = new Gson().fromJson(reader, RenderRecordJson[].class);

        for (RenderRecordJson recordJson : recordsJson) {
            ModelBase model;
            try {
                model = ((Class<ModelBase>) Class.forName(recordJson.model)).newInstance();
            } catch (ClassNotFoundException e) {
                model = BEOModelLoader.load(recordJson.model);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            trains.get(recordJson.entryName).render = new RenderRecord(
                    recordJson.entryName,
                    model,
                    recordJson.texture,
                    recordJson.multiTexture,
                    recordJson.trans.length != 0 ? recordJson.trans : null,
                    recordJson.rotate.length != 0 ? recordJson.rotate : null,
                    recordJson.scale.length != 0 ? recordJson.scale : null,
                    recordJson.smokeType,
                    recordJson.smokeIterations,
                    !recordJson.smokeFX.isEmpty() ? recordJson.smokeFX : null,
                    recordJson.explosionType,
                    !recordJson.explosionFX.isEmpty() ? recordJson.explosionFX : null,
                    recordJson.explosionFXIterations,
                    recordJson.hasSmokeOnSlopes,
                    recordJson.bogies);
        }
    }

    private final String entryName;
    private final ModelBase model;
    private final String texture;
    private final boolean multiTexture;
    private final float[] trans;
    private final float[] rotate;
    private final float[] scale;
    private final String smokeType;
    private final int smokeIterations;
    private final ArrayList<double[]> smokeFX;
    private final String explosionType;
    private final ArrayList<double[]> explosionFX;
    private final int explosionFXIterations;
    private final boolean hasSmokeOnSlopes;
    private final String[] bogies;

    private RenderRecord(String entryName, ModelBase model, String texture, boolean multiTexture, float[] trans, float[] rotate, float[] scale, String smokeType, int smokeIterations, ArrayList<double[]> smokeFX, String explosionType, ArrayList<double[]> explosionFX, int explosionFXIterations, boolean hasSmokeOnSlopes, String[] bogies) {
        this.entryName = entryName;
        this.model = model;
        this.texture = texture;
        this.multiTexture = multiTexture;
        this.trans = trans;
        this.rotate = rotate;
        this.scale = scale;
        this.smokeType = smokeType;
        this.smokeIterations = smokeIterations;
        this.smokeFX = smokeFX;
        this.explosionType = explosionType;
        this.explosionFX = explosionFX;
        this.explosionFXIterations = explosionFXIterations;
        this.hasSmokeOnSlopes = hasSmokeOnSlopes;
        this.bogies = bogies;
    }

    public String getEntryName() { return entryName; }
    public ModelBase getModel() { return model; }
    public ResourceLocation getTextureFile(String colorString) {
        if (getIsMultiTextured()) { return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + colorString + ".png"); }
        else {                      return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + ".png"); }
    }
    public boolean getIsMultiTextured() { return multiTexture; }
    public float[] getTrans() { return trans; }
    public float[] getRotate() { return rotate; }
    public float[] getScale() { return scale; }
    public String getSmokeType() { return smokeType; }
    public int getSmokeIterations() { return smokeIterations; }
    public ArrayList<double[]> getSmokeFX() { return smokeFX; }
    public String getExplosionType() { return explosionType; }
    public ArrayList<double[]> getExplosionFX() { return explosionFX; }
    public int getExplosionFXIterations() { return explosionFXIterations; }
    public boolean hasSmokeOnSlopes() { return hasSmokeOnSlopes; }
    public String[] getBogies() { return bogies; }


    public boolean hasSmoke() { return !smokeType.isEmpty(); }
    public boolean hasExplosion() { return !explosionType.isEmpty(); }
}