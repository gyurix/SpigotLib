package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WorldType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;

/**
 * Created by GyuriX on 2016.02.28..
 */
public class PacketPlayOutLogin extends WrappedPacket {
    public Difficulty difficulty;
    /**
     * Dimension -1: Nether 0: Overworld 1: End
     */
    public int dimension;
    public int entityId;
    public GameMode gameMode;
    public boolean hardcore;
    public WorldType levelType;
    public int maxPlayers;
    public boolean reducedDebugInfo;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Login.newPacket(entityId, hardcore, WorldType.toVanillaGameMode(gameMode), dimension, WorldType.toVanillaDifficulty(difficulty), maxPlayers, levelType.toNMS(), reducedDebugInfo);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.Login.getPacketData(packet);
        entityId = (int) d[0];
        hardcore = (boolean) d[1];
        gameMode = GameMode.valueOf(d[2].toString());
        dimension = (int) d[3];
        difficulty = Difficulty.valueOf(d[4].toString());
        maxPlayers = (int) d[5];
        levelType = WorldType.fromVanillaWorldType(d[6]);
        if (Reflection.ver.isAbove(ServerVersion.v1_8))
            reducedDebugInfo = (boolean) d[7];
    }
}
