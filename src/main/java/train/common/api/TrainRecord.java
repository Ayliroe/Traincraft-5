package train.common.api;

import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TrainRecord {

    public abstract String getName();

    public abstract String getInternalName();

    public abstract Item getItem();

    public abstract String getTrainType();

    public abstract int getMHP();

    public abstract int getMaxSpeed();

    public abstract double getMass();

    public abstract int getFuelConsumption();

    public abstract int getWaterConsumption();

    public abstract int getHeatingTime();

    public abstract double getAccelerationRate();

    public abstract double getBrakeRate();

    @Deprecated
    public abstract int getTankCapacity();

    public abstract List<String> getColors();

    public List<TransportSkin> getLiveries() {
        if (!SkinRegistry.liveryMap.containsKey(getName()) && getColors() != null) {
            for(String color:getColors()){
                SkinRegistry.addSkin(getName(),color);
            }
        }
        return (List<TransportSkin>) SkinRegistry.get(getName()).values();
    }

    public abstract double getBogieLocoPosition();

    public abstract Class<AbstractTrains> getEntityClass();

    @Deprecated
    public abstract String getAdditionnalTooltip();

    @Deprecated
    public abstract int getCargoCapacity();

    public abstract AbstractTrains getEntity(World world);

    public abstract AbstractTrains getEntity(World world, double x, double y, double z);

    /**
     * #param entryName is depreciated
     * #param additionalTooltip is depreciated.
     */
    public static TrainRecord makeEntry(final String entryName, final String internalName, final Class<AbstractTrains> entityClass, final Item item, final String trainType, final int MHP, final int maxSpeed, final double mass, final int fuelConsumption, final int waterConsumption, final int heatingTime, final double accelerationRate, final double brakeRate, final int tankCapacity, final int cargoCapacity, int guiRenderScale, final String additionnalTooltip, final double bogieLocoPositions, final String[] colors) {
        return new TrainRecord() {
            @Override
            public String getName() {
                return entryName;
            }

            @Override
            public String getInternalName() {
                return internalName;
            }

            @Override
            public Item getItem() {
                return item;
            }

            @Override
            public String getTrainType() {
                return trainType;
            }

            @Override
            public int getMHP() {
                return MHP;
            }

            @Override
            public int getMaxSpeed() {
                return maxSpeed;
            }

            @Override
            public double getMass() {
                return mass;
            }

            @Override
            public int getFuelConsumption() {
                return fuelConsumption;
            }

            @Override
            public int getWaterConsumption() {
                return waterConsumption;
            }

            @Override
            public int getHeatingTime() {
                return heatingTime;
            }

            @Override
            public double getAccelerationRate() {
                return accelerationRate;
            }

            @Override
            public double getBrakeRate() {
                return brakeRate;
            }

            @Override
            public int getTankCapacity() {
                return tankCapacity;
            }

            @Override
            public List<String> getColors() {
                if (colors == null || colors.length==0) {
                    return new ArrayList<>();
                } else {
                    //this isnt redundant, Arrays.asList overrides and breaks the List.Add method,
                    // so we have to dump content to a proper instance.
                    return new ArrayList<String>(Arrays.asList(colors));
                }
            }

            @Override
            public double getBogieLocoPosition() {
                return bogieLocoPositions;
            }

            @Override
            public Class<AbstractTrains> getEntityClass() {
                return entityClass;
            }

            @Override
            public String getAdditionnalTooltip() {
                return additionnalTooltip;
            }

            @Override
            public int getCargoCapacity() {
                return cargoCapacity;
            }

            @Override
            public AbstractTrains getEntity(World world) {
                try {
                    return (AbstractTrains) entityClass.getConstructor(World.class).newInstance(world);
                } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public AbstractTrains getEntity(World world, double x, double y, double z) {
                try {
                    //if (world.isRemote) {
                        //entityClass.getConstructor(World.class).newInstance(world);
                    //} else {
                        return (AbstractTrains) entityClass.getConstructor(World.class, double.class, double.class, double.class).newInstance(world, x, y, z);
                    //}
                } catch (IllegalArgumentException | SecurityException | InstantiationException |
                         IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
