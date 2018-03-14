package gyurix.inventory;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.spigotutils.ItemUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static gyurix.spigotutils.ItemUtils.fillVariables;

/**
 * Created by GyuriX on 2016. 09. 17..
 */
public class StaticItem implements StringSerializable {
    public ItemStack item;
    public int slot;

    public StaticItem(String in) {
        String[] s = in.split(" ", 2);
        slot = Integer.valueOf(s[0]);
        item = ItemUtils.stringToItemStack(s[1]);
    }

    public void set(Inventory inv) {
        inv.setItem(slot, item == null ? null : item.clone());
    }

    public void set(Inventory inv, Object... vars) {
        inv.setItem(slot, fillVariables(item, vars));
    }

    @Override
    public String toString() {
        return slot + " " + ItemUtils.itemToString(item);
    }
}
