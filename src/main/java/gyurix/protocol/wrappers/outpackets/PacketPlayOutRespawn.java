package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WorldType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.EntityUtils;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;

import static gyurix.protocol.utils.WorldType.*;

/**
 * Created by GyuriX on 2016.08.22..
 */
public class PacketPlayOutRespawn extends WrappedPacket implements Cloneable {
    public Difficulty difficulty;
    public int dimension;
    public GameMode gameMode;
    public WorldType worldType;

    public PacketPlayOutRespawn(int dimension, Difficulty difficulty, GameMode gameMode, WorldType worldType) {
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.gameMode = gameMode;
        this.worldType = worldType;
    }

    public PacketPlayOutRespawn(World w) {
        dimension = w.getEnvironment().getId();
        difficulty = w.getDifficulty();
        Object wd = EntityUtils.getWorldData(w);
        gameMode = GameMode.ADVENTURE;
        worldType = EntityUtils.getWorldType(wd);
    }

    public PacketPlayOutRespawn(int dimension, World w) {
        this.dimension = dimension;
        difficulty = w.getDifficulty();
        Object wd = EntityUtils.getWorldData(w);
        gameMode = GameMode.ADVENTURE;
        worldType = EntityUtils.getWorldType(wd);
    }

    public PacketPlayOutRespawn() {
    }

    public PacketPlayOutRespawn(Object packet) {
        loadVanillaPacket(packet);
    }

    @Override
    public PacketPlayOutRespawn clone() {
        return new PacketPlayOutRespawn(dimension, difficulty, gameMode, worldType);
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Respawn.newPacket(dimension, toVanillaDifficulty(difficulty), toVanillaGameMode(gameMode), worldType.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.Respawn.getPacketData(packet);
        dimension = (int) d[0];
        difficulty = Difficulty.valueOf(d[1].toString());
        gameMode = GameMode.valueOf(d[2].toString());
        worldType = fromVanillaWorldType(d[3]);
    }
}
