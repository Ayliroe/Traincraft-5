package train.common.library;

import com.google.gson.Gson;
import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.api.TrainRecord;
import train.common.api.TrainSoundRecord;
import train.common.entity.rollingStockOld.diesel.*;
import train.common.entity.rollingStockOld.electric.*;
import train.common.entity.rollingStockOld.steam.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

public final class EnumSounds implements TrainSoundRecord {

    public static List<EnumSounds> initSoundRecords() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/SoundRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        SoundRecordJson[] recordsJson = new Gson().fromJson(reader, SoundRecordJson[].class);

        List<EnumSounds> soundRecords = new ArrayList<>();

        for (SoundRecordJson recordJson : recordsJson) {
            soundRecords.add(new EnumSounds(
                    recordJson.entryName,
                    recordJson.horn,
                    recordJson.hornVolume,
                    recordJson.run,
                    recordJson.runVolume,
                    recordJson.runSoundLength,
                    recordJson.idle,
                    recordJson.idleVolume,
                    recordJson.idleSoundLength,
                    recordJson.soundChangeWithSpeed));
        }
        return soundRecords;
    }


    private String entryName;
    private String horn;
    private float hornVolume;
    private String run;
    private String idle;
    private float runVolume;
    private float idleVolume;
    private int runSoundLength;
    private int idleSoundLength;
    private boolean soundChangeWithSpeed;


    private EnumSounds(String entryName, String horn, float hornVolume, String run, float runVolume, int runSoundLength, String idle, float idleVolume, int idleSoundLength, boolean soundChangeWithSpeed) {
        this.entryName = entryName;
        this.horn = horn;
        this.hornVolume = hornVolume;
        this.run = run;
        this.idle = idle;
        this.runVolume = runVolume;
        this.idleVolume = idleVolume;
        this.runSoundLength = runSoundLength;
        this.idleSoundLength = idleSoundLength;
        this.soundChangeWithSpeed = soundChangeWithSpeed;
    }

    @Override
    public String getEntryName() {
        return entryName;
    }

    @Override
    public String getHornString() {
        if (horn == null || horn.isEmpty()) return horn;
        return Info.resourceLocation + ":" + horn;
    }

    @Override
    public String getRunString() {
        if (run == null || run.isEmpty()) return run;
        return Info.resourceLocation + ":" + run;
    }

    @Override
    public String getIdleString() {
        if (idle == null || idle.isEmpty()) return idle;
        return Info.resourceLocation + ":" + idle;
    }

    @Override
    public Float getHornVolume() {
        return hornVolume;
    }

    @Override
    public Float getRunVolume() {
        return runVolume;
    }

    @Override
    public Float getIdleVolume() {
        return idleVolume;
    }

    @Override
    public int getRunSoundLength() {
        return runSoundLength;
    }

    @Override
    public int getIdleSoundLength() {
        return idleSoundLength;
    }

    @Override
    public boolean getSoundChangeWithSpeed() {
        return soundChangeWithSpeed;
    }
}
