package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

/**
 * Created by GyuriX, on 2017. 02. 05..
 */
public class PacketPlayOutAttachEntity extends WrappedPacket {
    public int entity1, entity2;

    public PacketPlayOutAttachEntity() {

    }

    public PacketPlayOutAttachEntity(int entity1, int entity2) {
        this.entity1 = entity1;
        this.entity2 = entity2;
    }

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_9) ? PacketOutType.AttachEntity.newPacket(entity1, entity2) : PacketOutType.AttachEntity.newPacket(0, entity1, entity2);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.AttachEntity.getPacketData(packet);
        entity1 = (int) d[0];
        entity2 = (int) d[1];
    }
}
