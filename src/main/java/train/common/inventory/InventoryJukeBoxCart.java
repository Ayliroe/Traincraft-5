package train.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import train.common.api.AbstractJukeBox;
import train.common.api.EntityRollingStock;

public class InventoryJukeBoxCart extends Container {

	private AbstractJukeBox jukebox;
	private InventoryPlayer player;

	public InventoryJukeBoxCart(InventoryPlayer iinventory, EntityRollingStock entityminecart) {
		player = iinventory;
		jukebox = (AbstractJukeBox) entityminecart;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return !jukebox.isDead;
	}
}