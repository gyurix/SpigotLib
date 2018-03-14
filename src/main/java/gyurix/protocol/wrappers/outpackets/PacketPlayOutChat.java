package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.json.JsonAPI;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotutils.ServerVersion;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutChat extends WrappedPacket {
    private static Object[] nmsValues;

    static {
        try {
            nmsValues = (Object[]) Reflection.getMethod(Reflection.getNMSClass("ChatMessageType"), "values").invoke(null);
        } catch (Throwable e) {
        }
    }

    public ChatTag tag;
    /**
     * The type of this chat message 0: chat (chat box) 1: system message (chat box) 2: action bar
     */
    public byte type;

    public PacketPlayOutChat() {
    }

    public PacketPlayOutChat(byte type, ChatTag tag) {
        this.type = type;
        this.tag = tag;
    }

    @Override
    public Object getVanillaPacket() {
        if (Reflection.ver.isAbove(ServerVersion.v1_8))
            return PacketOutType.Chat.newPacket(ChatAPI.toICBC(tag.toString()), null, Reflection.ver.isAbove(ServerVersion.v1_12) ? nmsValues[type] : type);
        return PacketOutType.Chat.newPacket(ChatAPI.toICBC(tag.toString()), null, true, 0);
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.Chat.getPacketData(obj);
        if (data[1] != null) {
            tag = ChatTag.fromBaseComponents((BaseComponent[]) data[1]);
        } else
            tag = JsonAPI.deserialize(ChatAPI.toJson(data[0]), ChatTag.class);
        if (Reflection.ver.isAbove(ServerVersion.v1_8))
            type = (byte) (Reflection.ver.isAbove(ServerVersion.v1_12) ? (byte) (int) ((Enum) data[2]).ordinal() : data[2]);
    }
}
