package gyurix.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Created by GyuriX on 2016.08.31..
 */
public abstract class CustomGUI implements InventoryHolder {
    public boolean canClose;
    public Inventory inv;
    public Player plr;

    public CustomGUI(Player plr) {
        this.plr = plr;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public abstract void onClick(int slot, boolean right, boolean shift);

    public void onClose() {
    }
}
