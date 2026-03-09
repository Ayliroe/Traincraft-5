package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.core.EntityIds;
import train.common.items.ItemRollingStock;
import train.common.library.TraincraftRegistry.TrainRegister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>{@link #entryName} The stock's unique internal name
 * <p>{@link #entityClass} The broad entity class (ex. 'EntitySteamLocomotive', 'EntityTender')
 * <p>{@link #icon} The item icon for this this stock
 * <p>{@link #emeralds} How many emeralds this stock trades for with the trainstation merchant
 * <p>{@link #MHP} Minecraft HorsePower, i.e. how much mass this stock can pull
 * <p>{@link #maxSpeed} The maximum speed under power (Locomotives)
 * <p>{@link #mass} The mass that is added to the whole train, slowing down pulling Locomotives
 * <p>{@link #fuelConsumption} How much fuel is consumed per tick (Locomotives)
 * <p>{@link #waterConsumption} How much water is consumed per tick (Steam Locomotives)
 * <p>{@link #heatingTime} TODO CURRENTLY UNUSED
 * <p>{@link #accelerationRate} The maximum acceleration under power (Locomotives)
 * <p>{@link #brakeRate} The maximum break rate (Locomotives)
 * <p>{@link #tankCapacity} How much liquid is stored, where 1000 = 1 cubic meter (Tanks, Tenders, Non-electric Locomotives)
 * <p>{@link #cargoCapacity} How many freight slots are available (Freight)
 * <p>{@link #additionnalTooltip} Optional information (Item tooltip)
 * <p>{@link #bogieLocoPositions} The spacing between the stock's bogies
 * <p>{@link #country} Country of origin (Item tooltip)
 * <p>{@link #year} Year of origin (Item tooltip)
 * <p>{@link #fictional} Is this stock fictional (Item tooltip)
 * <p>{@link #optimalDistance} TODO TO BE DEPRECATED, use hitboxSize instead (Spacing when linked to another cart)
 * <p>{@link #hitboxSize} The size of this stock's proxies
 * <p>{@link #shouldRiderSit} Should the rider be in a sitting position
 * <p>{@link #riderOffsets} Position of the rider for each seat relative to the stock's center, the first being the driver
 */
public final class TrainRecord {

    public static void put(Map<String, TrainRegister> trains, String path) {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemDeserializer()).create();

        int trainID = 32;

        for (TrainRecord record : gson.fromJson(reader, TrainRecord[].class)) {

            if (!trains.containsKey(record.entryName))
                trains.put(record.entryName, new TrainRegister());

            trainID++;
            while (trainID == EntityIds.ZEPPELIN || trainID == EntityIds.LOCOMOTIVE_BOGIE || trainID == EntityIds.ZEPPELIN_BIG) {
                trainID++;
            }

            TrainRegister register = trains.get(record.entryName);

            register.type = record;
            EntityRegistry.registerModEntity(record.getEntityClass(), record.entryName, trainID, Traincraft.instance, 512, 1, true);

            register.item = new ItemRollingStock(trains.get(record.entryName), Info.modID.toLowerCase() + ":trains/" + record.icon, true);
            register.item.setUnlocalizedName(Info.modID + ":" + record.entryName);
            GameRegistry.registerItem(register.item, record.entryName);

            if (record.spawnInStation)
                TraincraftRegistry.stationTrains.add(trains.get(record.entryName));
        }
    }

    private String entryName;
    private String entityClass;
    private String icon;
    private int emeralds;
    private int MHP;
    private int maxSpeed;
    private float mass;
    private int fuelConsumption;
    private int waterConsumption;
    private int heatingTime;
    private double accelerationRate;
    private double brakeRate;
    private int tankCapacity;
    private int cargoCapacity;
    private String additionnalTooltip;
    private double bogieLocoPositions;
    private String[] skins;
    private String country;
    private String year;
    private boolean fictional;
    private float optimalDistance;
    private float[] hitboxSize;
    private boolean shouldRiderSit;
    private float[][] riderOffsets;
    private boolean spawnInStation;

    private Class<AbstractTrains> cachedEntityClass = null;   // We only store this when a stock actually needs it, else loading this for every possible class is wasted memory

    public String getName()                     { return entryName; }
    public Class<AbstractTrains> getEntityClass() {
        if (cachedEntityClass == null)          { cachedEntityClass = DeserializingUtils.trainClassForString(entityClass); }
                                                { return cachedEntityClass; }
    }
    public int getEmeralds()                    { return emeralds; }
    public int getMHP()                         { return MHP; }
    public int getMaxSpeed()                    { return maxSpeed; }
    public float getMass()                      { return mass; }
    public int getFuelConsumption()             { return fuelConsumption; }
    public int getWaterConsumption()            { return waterConsumption; }
    public int getHeatingTime()                 { return heatingTime; }
    public double getAccelerationRate()         { return accelerationRate; }
    public double getBrakeRate()                { return brakeRate; }
    public int getTankCapacity()                { return tankCapacity; }
    public int getCargoCapacity()               { return cargoCapacity; }
    public String getAdditionnalTooltip()       { return additionnalTooltip; }
    public double getBogieLocoPosition()        { return bogieLocoPositions; }
    public List<String> getSkins()              { return Arrays.asList(skins); }
    public String getCountry()                  { return country; }
    public String getYear()                     { return year; }
    public boolean isFictional()                { return fictional; }
    public float getOptimalDistance()           { return optimalDistance; }
    public float[] getHitboxSize()              { return hitboxSize; }
    public boolean getShouldRiderSit()          { return shouldRiderSit; }
    public float[][] getRiderOffsets()          { return riderOffsets; }
}