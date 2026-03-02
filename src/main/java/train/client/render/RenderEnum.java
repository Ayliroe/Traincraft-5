package train.client.render;

import com.google.gson.Gson;
import fexcraft.fvtm.BEOModelLoader;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.util.ResourceLocation;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.api.TrainRenderRecord;
import train.common.library.Info;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
}

public class RenderEnum implements TrainRenderRecord {


    private final String entryName;
    private final ModelBase model;
    private final String texture;
    private final boolean multiTexture;
    private final float[] trans;
    private final float[] rotate;
    private final float[] scale;
    private final String smokeType;
    private final ArrayList<double[]> smokeFX;
    private final String explosionType;
    private final ArrayList<double[]> explosionFX;
    private final int smokeIterations;
    private final int explosionFXIterations;
    private final boolean hasSmokeOnSlopes;

    public static List<RenderEnum> initRenderRecords() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/RenderRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        RenderRecordJson[] recordsJson = new Gson().fromJson(reader, RenderRecordJson[].class);

        List<RenderEnum> renderRecords = new ArrayList<>();

        for (RenderRecordJson recordJson : recordsJson) {
            ModelBase model;
            try {
                model = ((Class<ModelBase>) Class.forName(recordJson.model)).newInstance();
            } catch (ClassNotFoundException e) {
                model = BEOModelLoader.load(recordJson.model);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            renderRecords.add(new RenderEnum(
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
                    recordJson.hasSmokeOnSlopes)
                    );
        }
        return renderRecords;
    }

    /**
     * Defines the render @param for a RollingStock
     *
     * @param model         ModelBase
     * @param texture       String
     * @param multiTexture  boolean
     * @param trans         float[]
     * @param rotate        float[]
     * @param scale         float[]
     * @param smokeType     String
     * @param smokeFX       ArrayList
     * @param explosionType String
     * @param explosionFX   ArrayList
     * @see RenderRollingStock
     */
    RenderEnum(String entryName, ModelBase model, String texture, boolean multiTexture, float[] trans, float[] rotate, float[] scale, String smokeType, int smokeIterations, ArrayList<double[]> smokeFX, String explosionType, ArrayList<double[]> explosionFX, int explosionFXIterations, boolean hasSmokeOnSlopes) {
        this.entryName = entryName;
        this.model = model;
        this.texture = texture;
        this.multiTexture = multiTexture;
        this.trans = trans;
        this.rotate = rotate;
        this.scale = scale;
        this.smokeType = smokeType;
        this.smokeFX = smokeFX;
        this.explosionType = explosionType;
        this.explosionFX = explosionFX;
        this.smokeIterations = smokeIterations;
        this.explosionFXIterations = explosionFXIterations;
        this.hasSmokeOnSlopes = hasSmokeOnSlopes;
    }

    @Override
    public String getEntryName() {
        return entryName;
    }

    @Override
    public ModelBase getModel() {
        return model;
    }

    public boolean getIsMultiTextured() {
        return multiTexture;
    }

    @Override
    public boolean hasSmoke() {
        return !smokeType.isEmpty();
    }

    @Override
    public boolean hasSmokeOnSlopes() {
        return hasSmokeOnSlopes;
    }

    @Override
    public String getSmokeType() {
        return smokeType;
    }

    @Override
    public ArrayList<double[]> getSmokeFX() {
        return smokeFX;
    }

    @Override
    public String getExplosionType() {
        return explosionType;
    }

    @Override
    public boolean hasExplosion() {
        return !explosionType.isEmpty();
    }

    @Override
    public ArrayList<double[]> getExplosionFX() {
        return explosionFX;
    }

    @Override
    public float[] getTrans() {
        return trans;
    }

    @Override
    public float[] getRotate() {
        return rotate;
    }

    @Override
    public float[] getScale() {
        return scale;
    }

    @Override
    public ResourceLocation getTextureFile(String colorString) {
        if (multiTexture) {
            return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + colorString + ".png");
        } else {
            return new ResourceLocation(Info.resourceLocation, Info.trainsPrefix + texture + ".png");
        }
    }

    @Override
    public int getSmokeIterations() {
        return smokeIterations;
    }

    @Override
    public int getExplosionFXIterations() {
        return explosionFXIterations;
    }
}

