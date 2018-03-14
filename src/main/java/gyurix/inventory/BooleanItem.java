package gyurix.inventory;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.spigotutils.ItemUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static gyurix.spigotutils.ItemUtils.fillVariables;
import static gyurix.spigotutils.ItemUtils.itemToString;

/**
 * Created by GyuriX on 2016. 09. 17..
 */
public class BooleanItem implements StringSerializable {
    public int slot;
    public ItemStack yes, no;

    public BooleanItem(String in) {
        String[] s = in.split(" ", 2);
        slot = Integer.valueOf(s[0]);
        String[] items = s[1].split("\n", 2);
        yes = ItemUtils.stringToItemStack(items[0]);
        no = ItemUtils.stringToItemStack(items[1]);
    }

    public void set(Inventory inv, boolean value) {
        inv.setItem(slot, value ? yes : no);
    }

    public void set(Inventory inv, boolean value, Object... vars) {
        inv.setItem(slot, fillVariables(value ? yes : no, vars));
    }

    @Override
    public String toString() {
        return slot + " " + itemToString(yes) + '\n' + itemToString(no);
    }
}
