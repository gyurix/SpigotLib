package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

import java.util.UUID;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutSpawnEntityPainting extends WrappedPacket {
    public int entityId;
    public UUID entityUUID;
    public Direction facing;
    public BlockLocation location;
    public String title;

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_9) ?
                PacketOutType.SpawnEntityPainting.newPacket(entityId, entityUUID, location.toNMS(), facing.toNMS(), title) :
                PacketOutType.SpawnEntityPainting.newPacket(entityId, location.toNMS(), facing.toNMS(), title);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.SpawnEntityPainting.getPacketData(packet);
        entityId = (int) d[0];
        int id = 1;
        if (Reflection.ver.isAbove(ServerVersion.v1_9)) {
            entityUUID = (UUID) d[1];
            id = 2;
        }
        location = new BlockLocation(d[id]);
        facing = Direction.valueOf(d[id + 1].toString().toUpperCase());
        title = (String) d[id + 2];
    }
}
