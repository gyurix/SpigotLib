package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.scoreboard.ScoreboardDisplayMode;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayOutScoreboardObjective extends WrappedPacket {
  /**
   * Possible values:
   * 0 - create the scoreboard
   * 1 - remove the scoreboard
   * 2 - update title
   */
  public int action;
  public ScoreboardDisplayMode displayMode;
  public String name, title;


  public PacketPlayOutScoreboardObjective() {

  }

  public PacketPlayOutScoreboardObjective(Object packet) {
    loadVanillaPacket(packet);
  }

  public PacketPlayOutScoreboardObjective(String name, String title, ScoreboardDisplayMode displayMode, int action) {
    this.name = name;
    this.title = title;
    this.displayMode = displayMode;
    this.action = action;
  }

  @Override
  public Object getVanillaPacket() {
    if (Reflection.ver.isAbove(ServerVersion.v1_8))
      return PacketOutType.ScoreboardObjective.newPacket(name, title, displayMode == null ? null : displayMode.toNMS(), action);
    return PacketOutType.ScoreboardObjective.newPacket(name, title, action);
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] o = PacketOutType.ScoreboardObjective.getPacketData(packet);
    name = (String) o[0];
    title = (String) o[1];
    if (Reflection.ver.isAbove(ServerVersion.v1_8)) {
      displayMode = o[2] == null ? null : ScoreboardDisplayMode.valueOf(o[2].toString());
      action = (int) o[3];
    } else
      action = (int) o[2];
  }
}

