package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutRelEntityMove extends WrappedPacket {
    public byte deltaX, deltaY, deltaZ;
    public int entityId;
    public boolean onGround;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.RelEntityMove.newPacket(entityId, deltaX, deltaY, deltaZ, onGround);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.RelEntityMove.getPacketData(packet);
        entityId = (int) d[0];
        deltaX = (byte) d[1];
        deltaY = (byte) d[2];
        deltaZ = (byte) d[3];
        onGround = (boolean) d[4];
    }
}
