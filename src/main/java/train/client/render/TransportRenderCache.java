package train.client.render;

import fexcraft.tmt.slim.ModelBase;
import train.common.library.RenderRecord;

import java.util.ArrayList;

public class TransportRenderCache {
    public ArrayList<double[]> smokePosition = null;
    public ModelBase[] models = null;
    public Bogie[] bogies = null;
    public boolean needs_model_update = true;
    public String color="";
    public RenderRecord rend;
    public String[] bogieSkins;
}