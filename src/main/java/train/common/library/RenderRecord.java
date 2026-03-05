package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.util.ResourceLocation;
import train.common.Traincraft;
import train.common.library.TraincraftRegistry.TrainRegister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public final class RenderRecord {

    public static void put(Map<String, TrainRegister> trains, String path) {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Gson gson = new GsonBuilder().registerTypeAdapter(ModelBase.class, new ModelDeserializer()).create();

        for (RenderRecord record : gson.fromJson(reader, RenderRecord[].class)) {
            trains.get(record.entryName).render = record;
        }
    }

    private String entryName;
    private ModelBase model;
    private String texture;
    private boolean multiTexture;
    private float[] trans;
    private float[] rotate;
    private float[] scale;
    private String smokeType;
    private int smokeIterations;
    private ArrayList<double[]> smokeFX;
    private String explosionType;
    private ArrayList<double[]> explosionFX;
    private int explosionFXIterations;
    private boolean hasSmokeOnSlopes;
    private String[] bogies;

    public String getEntryName()                    { return entryName; }
    public ModelBase getModel()                     { return model; }
    public ResourceLocation getTextureFile(String skin) {
        if (getIsMultiTextured())                   { return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + skin + ".png"); }
        else                                        { return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + ".png"); }
    }
    public boolean getIsMultiTextured()             { return multiTexture; }
    public float[] getTrans()                       { return trans.length != 0 ? trans : null; }
    public float[] getRotate()                      { return rotate.length != 0 ? rotate : null; }
    public float[] getScale()                       { return scale.length != 0 ? scale : null; }
    public String getSmokeType()                    { return smokeType; }
    public int getSmokeIterations()                 { return smokeIterations; }
    public ArrayList<double[]> getSmokeFX()         { return !smokeFX.isEmpty() ? smokeFX : null; }
    public String getExplosionType()                { return explosionType; }
    public ArrayList<double[]> getExplosionFX()     { return !explosionFX.isEmpty() ? explosionFX : null; }
    public int getExplosionFXIterations()           { return explosionFXIterations; }
    public boolean hasSmokeOnSlopes()               { return hasSmokeOnSlopes; }
    public String[] getBogies()                     { return bogies; }

    public boolean hasSmoke()                       { return !smokeType.isEmpty(); }
    public boolean hasExplosion()                   { return !explosionType.isEmpty(); }
}