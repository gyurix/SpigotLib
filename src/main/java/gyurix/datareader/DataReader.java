package gyurix.datareader;

import gyurix.protocol.Protocol;
import gyurix.protocol.event.PacketInEvent;
import gyurix.spigotlib.SU;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class DataReader<T> implements Protocol.PacketInListener {
  private static final HashMap<String, DataReader> open = new HashMap<>();
  private final Consumer<T> onResult;
  @Getter
  private final Player player;

  protected DataReader(Player player, Consumer<T> onResult) {
    cancel(player);
    this.player = player;
    this.onResult = onResult;
    open.put(player.getName(), this);
  }

  public static boolean cancel(Plugin plugin) {
    open.values().removeIf((dr) -> {
      if (SU.getPlugin(dr.onResult.getClass()) == plugin) {
        SU.tp.unregisterIncomingListener(dr);
        return true;
      }
      return false;
    });
    return false;
  }

  public static boolean cancel(Player plr) {
    DataReader dr = open.get(plr.getName());
    if (dr != null) {
      dr.cancel();
      return true;
    }
    return false;
  }

  public boolean cancel() {
    DataReader dr = open.remove(getPlayer().getName());
    if (dr == null)
      return false;
    SU.tp.unregisterIncomingListener(this);
    return true;
  }

  protected abstract boolean onPacket(Object packet);

  @Override
  public void onPacketIN(PacketInEvent e) {
    if (e.getPlayer() != player)
      return;
    if (onPacket(e.getPacket()))
      e.setCancelled(true);
  }

  public void onResult(T result) {
    SU.sch.scheduleSyncDelayedTask(SU.getPlugin(onResult.getClass()), () -> {
      try {
        cancel();
        onResult.accept(result);
      } catch (Throwable e) {
        SU.error(SU.cs, e, SU.getPlugin(onResult.getClass()).getName(), "gyurix");
      }
    });
  }
}
