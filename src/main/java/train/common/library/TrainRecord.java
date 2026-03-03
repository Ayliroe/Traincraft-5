package train.common.library;

import com.google.gson.Gson;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.api.AbstractTrains;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TrainRecordJson {

    String entryName;
    String internalName;
    String entityClass;
    String item;
    String trainType;
    int MHP;
    int maxSpeed;
    float mass;
    int fuelConsumption;
    int waterConsumption;
    int heatingTime;
    double accelerationRate;
    double brakeRate;
    int tankCapacity;
    int cargoCapacity;
    int guiRenderScale;
    String additionnalTooltip;
    double bogieLocoPositions;
    String[] colors;
    String country;
    String year;
    boolean fictional;
    float optimalDistance;
    float[] hitboxSize;
    boolean shouldRiderSit;
    float[][] riderOffsets;
}

public final class TrainRecord {

    public static List<TrainRecord> initTrainRecords() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/TrainRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        TrainRecordJson[] recordsJson = new Gson().fromJson(reader, TrainRecordJson[].class);

        List<TrainRecord> trainRecords = new ArrayList<>();

        for (TrainRecordJson recordJson : recordsJson) {
            Class<AbstractTrains> entityClass;
            try {
                entityClass = (Class<AbstractTrains>) Class.forName(recordJson.entityClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            trainRecords.add(new TrainRecord(
                    recordJson.entryName,
                    recordJson.internalName,
                    entityClass,
                    ItemIDs.valueOf(recordJson.item).item,
                    recordJson.trainType,
                    recordJson.MHP,
                    recordJson.maxSpeed,
                    recordJson.mass,
                    recordJson.fuelConsumption,
                    recordJson.waterConsumption,
                    recordJson.heatingTime,
                    recordJson.accelerationRate,
                    recordJson.brakeRate,
                    recordJson.tankCapacity,
                    recordJson.cargoCapacity,
                    recordJson.guiRenderScale,
                    recordJson.additionnalTooltip,
                    recordJson.bogieLocoPositions,
                    recordJson.colors,
                    recordJson.country,
                    recordJson.year,
                    recordJson.fictional,
                    recordJson.optimalDistance,
                    recordJson.hitboxSize,
                    recordJson.shouldRiderSit,
                    recordJson.riderOffsets));
        }
        return trainRecords;
    }

    private final String entryName;
    private final String internalName;
    private final Class<AbstractTrains> entityClass;
    private final Item item;
    private final String trainType;
    private final int MHP;
    private final int maxSpeed;
    private final float mass;
    private final int fuelConsumption;
    private final int waterConsumption;
    private final int heatingTime;
    private final double accelerationRate;
    private final double brakeRate;
    private final int tankCapacity;
    private final int cargoCapacity;
    private final int guiRenderScale;
    private final String additionnalTooltip;
    private final double bogieLocoPositions;
    private final String[] colors;
    private final String country;
    private final String year;
    private final boolean fictional;
    private final float optimalDistance;
    private final float[] hitboxSize;
    private final boolean shouldRiderSit;
    private final float[][] riderOffsets;

    /**
     * @param entryName The stock's unique internal nname
     * @param internalName DEPRECATED
     * @param entityClass The broad entity class (ex. 'EntitySteamLocomotive', 'EntityTender')
     * @param item The item linked to this stock
     * @param trainType DEPRECATED, use TraincraftRegistry.findTrainType(this)
     * @param MHP Minecraft HorsePower, i.e. how much mass this stock can pull
     * @param maxSpeed The maximum speed under power (Locomotives)
     * @param mass The mass that is added to the whole train, slowing down pulling Locomotives
     * @param fuelConsumption How much fuel is consumed per tick (Locomotives)
     * @param waterConsumption How much water is consumed per tick (Steam Locomotives)
     * @param heatingTime CURRENTLY UNUSED
     * @param accelerationRate The maximum acceleration under power (Locomotives)
     * @param brakeRate The maximum break rate (Locomotives)
     * @param tankCapacity How much liquid is stored, where 1000 = 1 cubic meter (Tanks, Tenders, Non-electric Locomotives)
     * @param cargoCapacity How many freight slots are available (Freight)
     * @param guiRenderScale DEPRECATED?
     * @param additionnalTooltip Optional information (Item tooltip)
     * @param bogieLocoPositions The spacing between the stock's bogies
     * @param colors The available skins for this stock
     * @param country Country of origin (Item tooltip)
     * @param year Year of origin (Item tooltip)
     * @param fictional Is this stock fictional (Item tooltip)
     * @param optimalDistance TO BE DEPRECATED, use hitboxSize instead (Spacing when linked to another cart)
     * @param hitboxSize The size of this stock's proxies
     * @param shouldRiderSit Should the rider be in a sitting position
     * @param riderOffsets Position of the rider for each seat relative to the stock's center, the first being the driver
     */
    private TrainRecord(
            String entryName,
            String internalName,
            Class<AbstractTrains> entityClass,
            Item item,
            String trainType,
            int MHP,
            int maxSpeed,
            float mass,
            int fuelConsumption,
            int waterConsumption,
            int heatingTime,
            double accelerationRate,
            double brakeRate,
            int tankCapacity,
            int cargoCapacity,
            int guiRenderScale,
            String additionnalTooltip,
            double bogieLocoPositions,
            String[] colors,
            String country,
            String year,
            boolean fictional,
            float optimalDistance,
            float[] hitboxSize,
            boolean shouldRiderSit,
            float[][] riderOffsets) {
        this.entryName = entryName;
        this.internalName = internalName;
        this.entityClass = entityClass;
        this.item = item;
        this.trainType = trainType;
        this.MHP = MHP;
        this.maxSpeed = maxSpeed;
        this.mass = mass;
        this.fuelConsumption = fuelConsumption;
        this.waterConsumption = waterConsumption;
        this.heatingTime = heatingTime;
        this.accelerationRate = accelerationRate;
        this.brakeRate = brakeRate;
        this.tankCapacity = tankCapacity;
        this.cargoCapacity = cargoCapacity;
        this.guiRenderScale = guiRenderScale;
        this.additionnalTooltip = additionnalTooltip;
        this.bogieLocoPositions = bogieLocoPositions;
        this.colors = colors;
        this.country = country;
        this.year = year;
        this.fictional = fictional;
        this.optimalDistance = optimalDistance;
        this.hitboxSize = hitboxSize;
        this.shouldRiderSit = shouldRiderSit;
        this.riderOffsets = riderOffsets;
    }

    public String getName() { return entryName; }
    public String getInternalName() { return internalName; }
    public Class<AbstractTrains> getEntityClass()  { return entityClass; }
    public Item getItem() { return item; }
    public String getTrainType() { return trainType; }
    public int getMHP() { return MHP; }
    public int getMaxSpeed() { return maxSpeed; }
    public float getMass() { return mass; }
    public int getFuelConsumption() { return fuelConsumption; }
    public int getWaterConsumption() { return waterConsumption; }
    public int getHeatingTime() { return heatingTime; }
    public double getAccelerationRate() { return accelerationRate; }
    public double getBrakeRate() { return brakeRate; }
    public int getTankCapacity() { return tankCapacity; }
    public int getCargoCapacity() { return cargoCapacity; }
    public int getGuiRenderScale() { return guiRenderScale; }
    public String getAdditionnalTooltip() { return additionnalTooltip; }
    public double getBogieLocoPosition() { return bogieLocoPositions; }
    public List<String> getColors() {
        if (colors == null || colors.length==0) {
            return new ArrayList<>();
        } else {
            //this isnt redundant, Arrays.asList overrides and breaks the List.Add method,
            // so we have to dump content to a proper instance.
            return new ArrayList<String>(Arrays.asList(colors));
        }
    }
    public String getCountry() { return country; }
    public String getYear() { return year; }
    public boolean isFictional() { return fictional; }
    public float getOptimalDistance() { return optimalDistance; }
    public float[] getHitboxSize() { return hitboxSize; }
    public boolean getShouldRiderSit() { return shouldRiderSit; }
    public float[][] getRiderOffsets() { return riderOffsets; }


    public List<TransportSkin> getLiveries() {
        if (!SkinRegistry.liveryMap.containsKey(getName())) {
            for(String color:getColors()){
                SkinRegistry.addSkin(getName(),color);
            }
        }
        return (List<TransportSkin>) SkinRegistry.get(getName()).values();
    }

    public AbstractTrains getEntity(World world) {
        try {
            AbstractTrains train = (AbstractTrains) entityClass.getConstructor(World.class).newInstance(world);
            train.init(this);
            return train;
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
