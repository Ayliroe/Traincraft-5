package train.common.library;

import com.google.gson.Gson;
import train.common.Traincraft;
import train.common.library.TraincraftRegistry.TrainRegister;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class SoundRecordJson {

    String entryName;
    String horn;
    float hornVolume;
    String run;
    float runVolume;
    int runSoundLength;
    String idle;
    float idleVolume;
    int idleSoundLength;
    boolean soundChangeWithSpeed;
}

public final class SoundRecord {

    public static void put(Map<String, TrainRegister> trains, String path) {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        SoundRecordJson[] recordsJson = new Gson().fromJson(reader, SoundRecordJson[].class);

        for (SoundRecordJson recordJson : recordsJson) {

            trains.get(recordJson.entryName).sounds = new SoundRecord(
                    recordJson.entryName,
                    recordJson.horn,
                    recordJson.hornVolume,
                    recordJson.run,
                    recordJson.runVolume,
                    recordJson.runSoundLength,
                    recordJson.idle,
                    recordJson.idleVolume,
                    recordJson.idleSoundLength,
                    recordJson.soundChangeWithSpeed);
        }
    }

    private final String entryName;
    private final String horn;
    private final float hornVolume;
    private final String run;
    private final float runVolume;
    private final int runSoundLength;
    private final String idle;
    private final float idleVolume;
    private final int idleSoundLength;
    private final boolean soundChangeWithSpeed;

    private SoundRecord(String entryName, String horn, float hornVolume, String run, float runVolume, int runSoundLength, String idle, float idleVolume, int idleSoundLength, boolean soundChangeWithSpeed) {
        this.entryName = entryName;
        this.horn = horn;
        this.hornVolume = hornVolume;
        this.run = run;
        this.runVolume = runVolume;
        this.runSoundLength = runSoundLength;
        this.idle = idle;
        this.idleVolume = idleVolume;
        this.idleSoundLength = idleSoundLength;
        this.soundChangeWithSpeed = soundChangeWithSpeed;
    }

    public String getEntryName() { return entryName; }
    public String getHornString() {
        if (horn == null || horn.isEmpty()) return horn;
        return Info.resourceLocation + ":" + horn;
    }
    public Float getHornVolume() { return hornVolume; }
    public String getRunString() {
        if (run == null || run.isEmpty()) return run;
        return Info.resourceLocation + ":" + run;
    }
    public Float getRunVolume() { return runVolume; }
    public int getRunSoundLength() { return runSoundLength; }
    public String getIdleString() {
        if (idle == null || idle.isEmpty()) return idle;
        return Info.resourceLocation + ":" + idle;
    }
    public Float getIdleVolume() { return idleVolume; }
    public int getIdleSoundLength() { return idleSoundLength; }
    public boolean getSoundChangeWithSpeed() { return soundChangeWithSpeed; }
}
