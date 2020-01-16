package gyurix.hologram;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashMap;

public class HologramAPI {
  static HashMap<String, Hologram> holograms = new HashMap<>();
  static int nextHologramId = 459786;
  @Getter
  @Setter
  static int visibilityCheckRate = 10;
  static int visibilityCheckTaskId = -1;

  private static void enable() {
    if (visibilityCheckTaskId == -1)
      visibilityCheckTaskId = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> holograms.values().forEach(Hologram::checkVisibility), visibilityCheckRate, visibilityCheckRate);
  }

  public static boolean destroyHologram(String id) {
    Hologram h = holograms.get(id);
    if (h == null)
      return false;
    h.destroy();
    return true;
  }

  public static Hologram getHologram(String id) {
    return holograms.get(id);
  }

  public static Hologram createHologram(Location loc, String id, String... lines) {
    enable();
    Hologram h = new Hologram(loc, id, lines);
    holograms.put(id, h);
    return h;
  }
}
