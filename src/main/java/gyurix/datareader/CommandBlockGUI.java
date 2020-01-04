package gyurix.datareader;

import gyurix.json.JsonAPI;
import gyurix.nbt.NBTCompound;
import gyurix.protocol.Protocol;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.inpackets.PacketPlayInCustomPayload;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutBlockChange;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutTileEntityData;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.BlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static gyurix.spigotlib.Main.pl;
import static gyurix.spigotlib.SU.tp;

/**
 * SignGUI, which can be used
 */
public class CommandBlockGUI extends DataReader<String> {
  private final BlockLocation bl;
  private final String[] initialLines;
  private final Location loc;
  private final Protocol.PacketInListener dig = this::onDig;
  private int rid = -1;
  private final Protocol.PacketInListener click = this::onClick;

  public CommandBlockGUI(Player player, Consumer<String> onResult) {
    this(player, onResult, new String[]{"", ""});
  }

  public CommandBlockGUI(Player plr, Consumer<String> onResult, String[] initialLines) {
    super(plr, onResult);
    this.initialLines = initialLines;
    loc = plr.getLocation();
    loc.setX(loc.getBlockX() + 0.5);
    loc.setY(loc.getBlockY());
    loc.setZ(loc.getBlockZ() + 0.5);
    bl = new BlockLocation(loc);
    showCmdBlock();
    tp.registerIncomingListener(pl, this, PacketInType.CustomPayload);
    tp.registerIncomingListener(pl, dig, PacketInType.BlockDig);
    tp.registerIncomingListener(pl, click, PacketInType.BlockPlace);
  }

  @Override
  public boolean cancel() {
    if (super.cancel()) {
      System.out.println("Cancelled.");
      SU.sch.cancelTask(rid);
      SU.tp.unregisterIncomingListener(click);
      SU.tp.unregisterIncomingListener(dig);
      return true;
    }
    return false;
  }

  @Override
  protected boolean onPacket(Object packet) {
    PacketPlayInCustomPayload p = new PacketPlayInCustomPayload();
    p.loadVanillaPacket(packet);
    if (!p.channel.equals("MC|AdvCdm"))
      return false;
    String msg = new String(p.data, 14, p.data.length - 15);
    onResult(msg);
    return true;
  }

  public void hideCmdBlock() {
    Player plr = getPlayer();
    plr.teleport(loc);
    for (int x = -1; x <= 1; ++x) {
      for (int y = -1; y <= 2; ++y) {
        for (int z = -1; z <= 1; ++z) {
          Block b = getPlayer().getWorld().getBlockAt(bl.x + x, bl.y + y, bl.z + z);
          tp.sendPacket(plr, new PacketPlayOutBlockChange(new BlockLocation(bl.x + x, bl.y + y, bl.z + z), new BlockData(b)));
        }
      }
    }
    rid = SU.sch.scheduleSyncRepeatingTask(pl, () -> {
      double dist = plr.getLocation().distance(loc);
      if (dist > 0)
        cancel();
    }, 5, 5);
  }

  private void onClick(PacketInEvent e) {
    if (e.getPlayer() != getPlayer())
      return;
    e.setCancelled(true);
    SU.sch.scheduleSyncDelayedTask(pl, this::hideCmdBlock);
  }

  private void onDig(PacketInEvent e) {
    if (e.getPlayer() != getPlayer())
      return;
    e.setCancelled(true);
    SU.sch.scheduleSyncDelayedTask(pl, this::showCmdBlock);
  }

  public void showCmdBlock() {
    Player plr = getPlayer();
    plr.teleport(loc);
    NBTCompound nbt = new NBTCompound();
    nbt.set("Command", initialLines[0]);
    nbt.set("LastOutput", "\"" + JsonAPI.escape(initialLines[1]) + "\"");
    for (int x = -1; x <= 1; ++x) {
      for (int y = -1; y <= 2; ++y) {
        for (int z = -1; z <= 1; ++z) {
          if (x == 0 && y == 0 && z == 0 || x == 0 && y == 1 && z == 0) {
            tp.sendPacket(plr, new PacketPlayOutBlockChange(new BlockLocation(bl.x + x, bl.y + y, bl.z + z), new BlockData(Material.AIR)));
          } else {
            tp.sendPacket(plr, new PacketPlayOutBlockChange(new BlockLocation(bl.x + x, bl.y + y, bl.z + z), new BlockData(Material.COMMAND_BLOCK)));
            tp.sendPacket(plr, new PacketPlayOutTileEntityData(new BlockLocation(bl.x + x, bl.y + y, bl.z + z), 2, nbt));
          }
        }
      }
    }
  }
}
