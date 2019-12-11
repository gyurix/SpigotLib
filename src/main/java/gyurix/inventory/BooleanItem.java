package gyurix.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static gyurix.spigotutils.ItemUtils.fillVariables;

/**
 * Created by GyuriX on 2016. 09. 17..
 */
public class BooleanItem {
  public int slot;
  public ItemStack yes, no;

  public void set(Inventory inv, boolean value) {
    inv.setItem(slot, value ? yes : no);
  }

  public void set(Inventory inv, boolean value, Object... vars) {
    inv.setItem(slot, fillVariables(value ? yes : no, vars));
  }
}
