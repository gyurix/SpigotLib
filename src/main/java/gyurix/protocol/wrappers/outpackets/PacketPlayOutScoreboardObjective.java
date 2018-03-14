package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.scoreboard.ScoreboardDisplayMode;

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
        return PacketOutType.ScoreboardObjective.newPacket(name, title, displayMode == null ? null : displayMode.toNMS(), action);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.ScoreboardObjective.getPacketData(packet);
        name = (String) o[0];
        title = (String) o[1];
        displayMode = o[2] == null ? null : ScoreboardDisplayMode.valueOf(o[2].toString());
        action = (int) o[3];
    }
}

