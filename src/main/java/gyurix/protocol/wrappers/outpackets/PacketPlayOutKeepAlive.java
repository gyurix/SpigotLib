package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.02.28..
 */
public class PacketPlayOutKeepAlive extends WrappedPacket {
    public int id;

    public PacketPlayOutKeepAlive() {

    }

    public PacketPlayOutKeepAlive(int id) {
        this.id = id;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.KeepAlive.newPacket(id);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        id = (int) PacketOutType.KeepAlive.getPacketData(packet)[0];
    }
}
