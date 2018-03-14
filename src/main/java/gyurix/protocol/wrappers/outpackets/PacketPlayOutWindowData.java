package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayOutWindowData
        extends WrappedPacket {
    public int property;
    public int value;
    public int windowId;

    public PacketPlayOutWindowData() {
    }

    public PacketPlayOutWindowData(int windowId, int property, int value) {
        this.windowId = windowId;
        this.property = property;
        this.value = value;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.WindowData.newPacket(windowId, property, value);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.WindowData.getPacketData(packet);
        windowId = (Integer) o[0];
        property = (Integer) o[1];
        value = (Integer) o[2];
    }
}

