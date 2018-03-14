package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;


public class PacketPlayOutTabComplete extends WrappedPacket {
    public String[] complete;

    public PacketPlayOutTabComplete() {
    }

    public PacketPlayOutTabComplete(Object nms) {
        loadVanillaPacket(nms);
    }

    public PacketPlayOutTabComplete(String[] complete) {
        this.complete = complete;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.TabComplete.newPacket(new Object[]{complete});
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        complete = (String[]) PacketOutType.TabComplete.getPacketData(obj)[0];
    }
}
