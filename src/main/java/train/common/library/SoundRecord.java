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

public final class SoundRecord {

    public static void put(Map<String, TrainRegister> trains, String path) {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        for (SoundRecord record : new Gson().fromJson(reader, SoundRecord[].class)) {
            trains.get(record.entryName).sounds = record;
        }
    }

    private String entryName;
    private String horn;
    private float hornVolume;
    private String run;
    private float runVolume;
    private int runSoundLength;
    private String idle;
    private float idleVolume;
    private int idleSoundLength;
    private boolean soundChangeWithSpeed;

    public String getEntryName()                        { return entryName; }
    public String getHornString() {
        if (horn == null || horn.isEmpty())             { return horn; }
        else                                            { return Info.resourceLocation + ":" + horn; }
    }
    public Float getHornVolume()                        { return hornVolume; }
    public String getRunString()                        {
        if (run == null || run.isEmpty())               { return run; }
        else                                            { return Info.resourceLocation + ":" + run; }
    }
    public Float getRunVolume()                         { return runVolume; }
    public int getRunSoundLength()                      { return runSoundLength; }
    public String getIdleString() {
        if (idle == null || idle.isEmpty())             { return idle; }
        else                                            { return Info.resourceLocation + ":" + idle; }
    }
    public Float getIdleVolume()                        { return idleVolume; }
    public int getIdleSoundLength()                     { return idleSoundLength; }
    public boolean getSoundChangeWithSpeed()            { return soundChangeWithSpeed; }
}