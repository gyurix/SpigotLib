package gyurix.inventory;

import gyurix.spigotlib.SU;
import gyurix.spigotutils.TPSMeter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class CloseableGUI implements InventoryHolder {
  protected Inventory inv;
  protected Player plr;
  private RuntimeException firstClose;
  @Getter
  @Setter
  private boolean forceClose;
  private int lastCloseTick;

  public CloseableGUI(Player plr) {
    this.plr = plr;
  }

  public static void cancel(Plugin pl) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      Inventory inv = p.getOpenInventory().getTopInventory();
      if (inv != null && inv.getHolder() instanceof CloseableGUI) {
        CloseableGUI gui = (CloseableGUI) inv.getHolder();
        if (pl == gui.getPlugin()) {
          gui.setForceClose(true);
          p.closeInventory();
        }
      }
    }
  }

  public final void close() {
    int time = TPSMeter.totalTicks;
    if (firstClose != null) {
      SU.cs.sendMessage("§cDetected double closed GUI:");
      String pln = getPlugin().getName();
      SU.error(SU.cs, firstClose, pln, "gyurix");
      SU.error(SU.cs, new RuntimeException("Second close"), pln, "gyurix");
      return;
    }
    firstClose = new RuntimeException("First close");
    lastCloseTick = time;
    if (forceClose)
      onForceClose();
    else
      onClose();
  }

  @Override
  public Inventory getInventory() {
    return inv;
  }

  public Player getPlayer() {
    return plr;
  }

  public Plugin getPlugin() {
    return SU.getPlugin(getClass());
  }

  protected void onClose() {
  }

  protected void onForceClose() {
  }
}
