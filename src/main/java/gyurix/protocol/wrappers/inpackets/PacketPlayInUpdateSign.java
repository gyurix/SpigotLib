package gyurix.protocol.wrappers.inpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Array;

public class PacketPlayInUpdateSign extends WrappedPacket {
    public BlockLocation block;
    public ChatTag[] lines;

    @Override
    public Object getVanillaPacket() {
        Object[] lines = (Object[]) Array.newInstance(ChatAPI.icbcClass, 4);
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            for (int i = 0; i < 4; ++i)
                lines[i] = this.lines[i].toColoredString();
        else
            for (int i = 0; i < 4; ++i)
                lines[i] = this.lines[i].toICBC();
        return PacketInType.UpdateSign.newPacket(block.toNMS(), lines);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.UpdateSign.getPacketData(packet);
        block = new BlockLocation(data[0]);
        lines = new ChatTag[4];
        Object[] packetLines = (Object[]) data[1];
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            for (int i = 0; i < 4; ++i)
                lines[i] = ChatTag.fromColoredText((String) packetLines[i]);
        else
            for (int i = 0; i < 4; ++i)
                lines[i] = ChatTag.fromICBC(packetLines[i]);
    }
}

