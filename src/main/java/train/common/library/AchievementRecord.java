package train.common.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import train.common.Traincraft;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class AchievementRecord {

    public static Map<String,AchievementRecord> achievements = new HashMap<>();
    public static AchievementPage page;

    public static void init() {

        InputStream stream = Traincraft.instance.getClass().getClassLoader().getResourceAsStream("assets/tc/data/AchievementRecords.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Gson gson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemDeserializer()).registerTypeAdapter(ItemStack.class, new ItemStackDeserializer()).create();

        Map<String,AchievementRecord> achievementsToBuild = new HashMap<>();
        List<Achievement> achievementsForPage = new ArrayList<>();

        for (AchievementRecord record : gson.fromJson(reader, AchievementRecord[].class)) {
            achievementsToBuild.put(record.name, record);
            achievements.put(record.name, record);
        }

        // Keep iterating through achievements to find parent dependencies (sadly unavoidable as the parent field is final, so we can't pre-construct achievements in a first pass and assign their parents later)
        while (!achievementsToBuild.isEmpty()) {
            Set<String> toRemove = new HashSet<>();
            for (AchievementRecord record : achievementsToBuild.values()) {

                Achievement parent = record.parent.isEmpty() ? null : achievements.get(record.parent).achievement;

                if (record.parent.isEmpty() || parent != null) {
                    record.achievement = new Achievement("achievement.tc." + record.name, "tc:" + record.name, record.column, record.row, record.stack, parent); // Empty parent string = we intentionally set null here
                    if (record.stack == null)
                        record.achievement.initIndependentStat();
                    if (record.special)
                        record.achievement.setSpecial();
                    record.achievement.registerStat();

                    achievementsForPage.add(record.achievement);
                    toRemove.add(record.name);
                }
            }
            achievementsToBuild.keySet().removeAll(toRemove);
        }

        page = new AchievementPage("TrainCraft", achievementsForPage.toArray(new Achievement[0]));
        AchievementPage.registerAchievementPage(AchievementRecord.page);
    }

    private Achievement achievement = null;

    private String name;
    private int column;
    private int row;
    private ItemStack stack;
    private String parent;
    private boolean special;
    private Item[] items;

    public Achievement getAchievement() { return achievement; }
    public List<Item> getItems() { return Arrays.asList(items); }
}