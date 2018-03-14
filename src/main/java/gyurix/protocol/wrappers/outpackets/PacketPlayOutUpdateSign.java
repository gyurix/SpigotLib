package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.nbt.NBTCompound;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Array;

import static gyurix.nbt.NBTTagType.tag;

public class PacketPlayOutUpdateSign extends WrappedPacket {
    public BlockLocation block;
    public ChatTag[] lines;

    public PacketPlayOutUpdateSign(BlockLocation loc, ChatTag[] lines) {
        block = loc;
        this.lines = lines;
    }

    public PacketPlayOutUpdateSign() {

    }

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver.isAbove(ServerVersion.v1_9)) {
            NBTCompound nbt = new NBTCompound();
            for (int i = 0; i < 4; ++i)
                nbt.put("Text" + (i + 1), tag(lines[i].toString()));
            PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData(block, 9, nbt);
            return packet.getVanillaPacket();
        }
        Object[] lines = (Object[]) Array.newInstance(ChatAPI.icbcClass, 4);
        for (int i = 0; i < 4; ++i)
            lines[i] = this.lines[i].toICBC();
        return PacketOutType.UpdateSign.newPacket(null, block.toNMS(), lines);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketOutType.UpdateSign.getPacketData(packet);
        block = new BlockLocation(data[1]);
        lines = new ChatTag[4];
        Object[] packetLines = (Object[]) data[2];
        for (int i = 0; i < 4; ++i) {
            lines[i] = ChatTag.fromICBC(packetLines[i]);
        }
    }
}

