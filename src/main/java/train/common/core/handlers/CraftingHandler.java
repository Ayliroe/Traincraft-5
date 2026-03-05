/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 * 
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.common.core.handlers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.item.Item;
import train.common.items.ItemRollingStock;
import train.common.library.AchievementRecord;

public class CraftingHandler {

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        for (AchievementRecord record : AchievementRecord.achievements.values()) {
            if (record.getItems().contains(event.crafting.getItem())) {
                event.player.addStat(record.getAchievement(), 1);
                break;
            }
        }
        if ((event.crafting.getItem() instanceof ItemRollingStock)) {
            if (!event.player.worldObj.isRemote) {
                if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                    ItemRollingStock stock = (ItemRollingStock) event.crafting.getItem();
                    //TraincraftSaveHandler.createFile(FMLCommonHandler.instance().getMinecraftServerInstance());
                    //int readID = TraincraftSaveHandler.readInt(FMLCommonHandler.instance().getMinecraftServerInstance(), "numberOfTrains:");
                    //int newID = stock.setNewUniqueID(event.crafting, event.player, readID);
                    stock.setNewUniqueID(event.crafting, event.player, -1);
                    //TraincraftSaveHandler.writeValue(FMLCommonHandler.instance().getMinecraftServerInstance(), "numberOfTrains:", "" + newID);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSmelting(PlayerEvent.ItemSmeltedEvent event) {
        for (AchievementRecord record : AchievementRecord.achievements.values()) {
            if (record.getItems().contains(event.smelting.getItem())) {
                event.player.addStat(record.getAchievement(), 1);
                break;
            }
        }
    }
}