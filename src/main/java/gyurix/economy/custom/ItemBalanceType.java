package gyurix.economy.custom;

import gyurix.economy.BalanceData;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemBalanceType extends BalanceType {
  @Getter
  @Setter
  private ItemStack item;

  public ItemBalanceType(BalanceData bd) {
    super(bd);
  }

  @Override
  public BigDecimal get(UUID plr) {
    Player p = SU.getPlayer(plr);
    if (p == null)
      throw new RuntimeException("Player " + plr + " was not found.");
    return new BigDecimal(ItemUtils.countItem(p.getInventory(), item));
  }

  @Override
  public boolean set(UUID plr, BigDecimal value) {
    Player p = SU.getPlayer(plr);
    int has = ItemUtils.countItem(p.getInventory(), item);
    int goal = value.intValue();
    if (goal < 0)
      return false;
    if (has == goal)
      return true;
    else if (goal > has) {
      ItemStack is = item.clone();
      is.setAmount(goal - has);
      int left = ItemUtils.addItem(p.getInventory(), is);
      if (left > 0)
        p.getWorld().dropItem(p.getLocation(), is);
      return true;
    }
    ItemStack is = item.clone();
    is.setAmount(has - goal);
    ItemUtils.removeItem(p.getInventory(), is);
    return true;
  }
}
