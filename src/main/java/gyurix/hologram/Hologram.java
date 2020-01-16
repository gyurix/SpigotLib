package gyurix.hologram;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.PostLoadable;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.LocationData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;

import static gyurix.spigotlib.Main.pl;


public class Hologram implements PostLoadable {
  @Getter
  @Setter
  private Function<Player, Boolean> canSee = (plr) -> true;
  private String id;
  @ConfigOptions(serialize = false)
  @Getter
  private ArrayList<HologramLine> lineEntities = new ArrayList<>();
  @Getter
  private ArrayList<String> lines = new ArrayList<>();
  private LocationData loc;
  private HologramSettings settings;
  @ConfigOptions(serialize = false)
  private int taskId = -1;
  private HashSet<String> viewers = new HashSet<>();

  Hologram() {

  }

  Hologram(Location loc, String id, String... lines) {
    this.loc = new LocationData(loc);
    this.id = id;
    this.lines.addAll(Arrays.asList(lines));
    this.settings = new HologramSettings();
    postLoad();
  }

  public void checkVisibility() {
    Iterator<String> it = viewers.iterator();
    Location l = loc.getLocation();
    while (it.hasNext()) {
      String s = it.next();
      Player p = Bukkit.getPlayerExact(s);
      if (p == null || !p.getWorld().getName().equals(loc.world)) {
        for (HologramLine hl : lineEntities)
          hl.viewers.remove(s);
        it.remove();
        continue;
      }
      if (p.getLocation().distance(l) > settings.dist || !canSee.apply(p)) {
        for (HologramLine hl : lineEntities)
          hl.hide(p);
        it.remove();
      }
    }
    World w = l.getWorld();
    for (Player p : w.getPlayers()) {
      if (viewers.contains(p.getName()))
        continue;
      if (p.isOnline() && p.getLocation().distance(l) <= settings.dist && canSee.apply(p)) {
        for (HologramLine hl : lineEntities)
          hl.show(p);
        viewers.add(p.getName());
      }
    }
  }

  public void destroy() {
    SU.sch.cancelTask(taskId);
    for (HologramLine hl : lineEntities)
      hl.destroy();
    HologramAPI.holograms.remove(id);
  }

  @Override
  public void postLoad() {
    LocationData ld = loc.clone();
    ld.y += settings.lineDist;
    if (settings.align == LineAlign.CENTER)
      ld.y += settings.lineDist * (lines.size() - 1) / 2;
    else if (settings.align == LineAlign.UP)
      ld.y += settings.lineDist * (lines.size() - 1);
    for (String line : lines) {
      ld = ld.clone();
      ld.y -= settings.lineDist;
      lineEntities.add(new HologramLine(line, ld));
    }
  }

  public void reload() {
    viewers.clear();
    for (HologramLine hl : lineEntities)
      hl.destroy();
    lineEntities.clear();
    postLoad();
    checkVisibility();
  }

  public void setLines(String... lines) {
    int len = lines.length;
    this.lines = new ArrayList<>(Arrays.asList(lines));
    if (len != lineEntities.size()) {
      reload();
      return;
    }
    for (int i = 0; i < lines.length; ++i) {
      HologramLine line = lineEntities.get(i);
      line.text = lines[i];
      line.update();
    }
  }

  public void setUpdateTicks(int update) {
    settings.update = update;
    SU.sch.cancelTask(taskId);
    taskId = SU.sch.scheduleSyncRepeatingTask(pl, this::update, settings.update, settings.update);
  }

  public void teleport(LocationData loc, boolean skipPackets) {
    loc = loc.clone();
    loc.world = this.loc.world;
    if (this.loc.equals(loc))
      return;
    this.loc = loc.clone();
    LocationData ld = loc.clone();
    ld.y += settings.lineDist;
    if (settings.align == LineAlign.CENTER)
      ld.y += settings.lineDist * (lines.size() - 1) / 2;
    else if (settings.align == LineAlign.UP)
      ld.y += settings.lineDist * (lines.size() - 1);
    for (int i = 0; i < lines.size(); i++) {
      ld = ld.clone();
      ld.y -= settings.lineDist;
      lineEntities.get(i).teleport(ld, skipPackets);
    }
  }

  public void update() {
    for (HologramLine hl : lineEntities)
      hl.update();
  }
}
