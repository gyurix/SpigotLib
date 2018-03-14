package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInChat
        extends WrappedPacket {
    public String message;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Chat.newPacket(message);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        message = (String) PacketInType.Chat.getPacketData(packet)[0];
    }
}

