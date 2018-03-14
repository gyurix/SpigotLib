package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.02.28..
 */
public class PacketPlayOutSpawnPosition extends WrappedPacket {
    public BlockLocation location;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.SpawnPosition.newPacket(location.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        location = new BlockLocation(PacketOutType.SpawnPosition.getPacketData(packet)[0]);

    }

}
