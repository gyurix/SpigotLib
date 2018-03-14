package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

import java.util.UUID;

public class PacketPlayInSpectate
        extends WrappedPacket {
    private UUID entityUUID;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Spectate.newPacket(entityUUID);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        entityUUID = (UUID) PacketInType.Spectate.getPacketData(packet)[0];
    }
}

