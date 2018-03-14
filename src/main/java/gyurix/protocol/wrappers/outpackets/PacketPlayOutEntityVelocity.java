package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import org.bukkit.util.Vector;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityVelocity extends WrappedPacket {
    public int entityId;
    public int velX;
    public int velY;
    public int velZ;

    public PacketPlayOutEntityVelocity() {

    }

    public PacketPlayOutEntityVelocity(int entityId, Vector velocity) {
        this.entityId = entityId;
        velX = (int) (velocity.getX() * 8000.0);
        velY = (int) (velocity.getX() * 8000.0);
        velZ = (int) (velocity.getX() * 8000.0);
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityVelocity.newPacket(entityId, velX, velY, velZ);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityVelocity.getPacketData(packet);
        entityId = (int) d[0];
        velX = (int) d[1];
        velY = (int) d[2];
        velZ = (int) d[3];
    }

    public Vector getVelocity() {
        return new Vector(velX / 8000.0, velY / 8000.0, velZ / 8000.0);
    }
}
