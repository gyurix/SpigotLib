package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInKeepAlive
        extends WrappedPacket {
    public int id;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.KeepAlive.newPacket(id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        id = (Integer) PacketInType.KeepAlive.getPacketData(packet)[0];
    }
}

