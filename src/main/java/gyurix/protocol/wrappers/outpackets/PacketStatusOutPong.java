package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketStatusOutPong extends WrappedPacket {
    public long id;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.StatusOutPong.newPacket(id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        id = (long) PacketOutType.StatusOutPong.getPacketData(packet)[0];
    }
}
