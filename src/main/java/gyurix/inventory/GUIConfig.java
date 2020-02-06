package gyurix.inventory;

import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static gyurix.spigotutils.ItemUtils.fillVariables;

public class GUIConfig {
  public HashMap<Integer, ItemStack> items = new HashMap<>();
  public ItemStack separator;
  public int size;
  public String title;

  public Inventory getInventory(int size, CustomGUI gui, Object... vars) {
    return getInventory(size, (InventoryHolder) gui, vars);
  }

  public Inventory getInventory(int size, InventoryHolder holder, Object... vars) {
    size = (size + 8) / 9 * 9;
    Inventory inv = Bukkit.createInventory(holder, size, SU.fillVariables(title, vars));
    ItemStack sep = fillVariables(separator, vars);
    for (int i = 0; i < size; i++) {
      ItemStack is = items.get(i);
      inv.setItem(i, is == null ? sep : fillVariables(is, vars));
    }
    return inv;
  }

  public Inventory getInventory(InventoryHolder holder, Object... vars) {
    return getInventory(size, holder, vars);
  }

  public Inventory getInventory(CustomGUI gui, Object... vars) {
    return getInventory((InventoryHolder) gui, vars);
  }
}
