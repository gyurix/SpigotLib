package gyurix.inventory;

import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static gyurix.spigotutils.ItemUtils.fillVariables;

public class GUIConfig {
    public ItemStack separator;
    public int size;
    public String title;

    public Inventory getInventory(CustomGUI gui, Object... vars) {
        Inventory inv = Bukkit.createInventory(gui, size, SU.fillVariables(title, vars));
        if (separator != null && separator.getType() != Material.AIR) {
            ItemStack sep = fillVariables(separator, vars);
            for (int i = 0; i < size; i++)
                inv.setItem(i, sep);
        }
        return inv;
    }
}
