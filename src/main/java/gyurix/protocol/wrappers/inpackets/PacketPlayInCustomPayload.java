package gyurix.protocol.wrappers.inpackets;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class PacketPlayInCustomPayload extends WrappedPacket implements StringSerializable {
    public String channel;
    public byte[] data;

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver.isAbove(ServerVersion.v1_8)) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(data.length);
            buf.writeBytes(data);
            return PacketInType.CustomPayload.newPacket(channel, buf);
        }
        return PacketInType.CustomPayload.newPacket(channel, data.length, data);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketInType.CustomPayload.getPacketData(packet);
        channel = (String) d[0];
        data = Reflection.ver.isAbove(ServerVersion.v1_8) ? ((ByteBuf) d[2]).array() : (byte[]) d[2];
    }

    @Override
    public String toString() {
        return channel;
    }
}

