package train.common.api;

public interface TrainSoundRecord {

    String getEntryName();

    String getHornString();

    String getRunString();

    String getIdleString();

    Float getHornVolume();

    Float getRunVolume();

    Float getIdleVolume();

    int getRunSoundLength();

    int getIdleSoundLength();

    boolean getSoundChangeWithSpeed();
}
