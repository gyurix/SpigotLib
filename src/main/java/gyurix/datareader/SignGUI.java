package gyurix.datareader;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.inpackets.PacketPlayInUpdateSign;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutBlockChange;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutOpenSignEditor;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutUpdateSign;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static gyurix.chat.ChatTag.fromColoredText;
import static gyurix.spigotlib.Main.pl;
import static gyurix.spigotlib.SU.tp;

/**
 * SignGUI, which can be used
 */
public class SignGUI extends DataReader<String[]> {
  private final BlockLocation bl;

  /**
   * Opens a new SignGUI to the given player with empty lines
   *
   * @param plr            - Player to which the SignGUI should be opened
   * @param resultConsumer - The consumer of the result
   */
  public SignGUI(Player plr, Consumer<String[]> resultConsumer) {
    this(plr, resultConsumer, new String[]{"", "", "", ""});
  }

  /**
   * Opens a new SignGUI to the given player with the given default lines
   *
   * @param plr            - Player to which the SignGUI should be opened
   * @param resultConsumer - The consumer of the result
   * @param initialLines   - Default lines of the sign
   */
  public SignGUI(Player plr, Consumer<String[]> resultConsumer, String[] initialLines) {
    super(plr, resultConsumer);
    Location loc = plr.getLocation();
    bl = new BlockLocation(loc.getBlockX(), loc.getBlockY() > 128 ? 0 : 255, loc.getBlockZ());
    tp.sendPacket(plr, new PacketPlayOutBlockChange(bl,
            (Reflection.ver.isAbove(ServerVersion.v1_14) ? Material.OAK_SIGN : Material.valueOf("SIGN")).getId(),
            (byte) 0).getVanillaPacket());
    tp.sendPacket(plr, new PacketPlayOutUpdateSign(bl, new ChatTag[]{
            fromColoredText(initialLines[0]), fromColoredText(initialLines[1]),
            fromColoredText(initialLines[2]), fromColoredText(initialLines[3])}));
    tp.registerIncomingListener(pl, this, PacketInType.UpdateSign);
    tp.sendPacket(plr, new PacketPlayOutOpenSignEditor(bl));
  }

  @Override
  protected boolean onPacket(Object packet) {
    PacketPlayInUpdateSign p = new PacketPlayInUpdateSign();
    p.loadVanillaPacket(packet);
    String[] res = new String[4];
    for (int i = 0; i < 4; ++i)
      res[i] = p.lines[i].toColoredString();
    Block b = bl.getBlock(getPlayer().getWorld());
    tp.sendPacket(getPlayer(), new PacketPlayOutBlockChange(bl, b.getType().getId(), b.getData()).getVanillaPacket());
    onResult(res);
    return true;
  }
}
