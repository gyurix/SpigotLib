package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInTabComplete extends WrappedPacket {
    public boolean assumeCommand;
    public BlockLocation block;
    public String text;

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver.isAbove(ServerVersion.v1_10))
            return PacketInType.TabComplete.newPacket(text, assumeCommand, block == null ? null : block.toNMS());
        return PacketInType.TabComplete.newPacket(text, block == null ? null : block.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.TabComplete.getPacketData(packet);
        text = (String) data[0];
        if (Reflection.ver.isAbove(ServerVersion.v1_10)) {
            assumeCommand = (boolean) data[1];
            block = data[2] == null ? null : new BlockLocation(data[2]);
        } else if (Reflection.ver.isAbove(ServerVersion.v1_8))
            block = data[1] == null ? null : new BlockLocation(data[1]);
    }
}

