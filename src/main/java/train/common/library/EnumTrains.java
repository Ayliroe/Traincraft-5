package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.api.TrainRecord;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class TrainRecordJson {

	String entryName;
	String internalName;
	String entityClass;
	String item;
	String trainType;
	int MHP;
	int maxSpeed;
	double mass;
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
}

public final class EnumTrains {

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

            trainRecords.add(TrainRecord.makeEntry(
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
					recordJson.colors));
		}
		return trainRecords;
	}
}
