package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayOutOpenWindow
        extends WrappedPacket {
    public int entityId;
    public int slots;
    public ChatTag title;
    public String type;
    public int windowId;

    public PacketPlayOutOpenWindow() {

    }

    public PacketPlayOutOpenWindow(int windowId, String type, ChatTag title, int slots) {
        this.windowId = windowId;
        this.type = type;
        this.title = title;
        this.slots = slots;
    }

    public PacketPlayOutOpenWindow(int windowId, String type, ChatTag title, int slots, int entityId) {
        this.windowId = windowId;
        this.type = type;
        this.title = title;
        this.slots = slots;
        this.entityId = entityId;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.OpenWindow.newPacket(windowId, type, title.toICBC(), slots, entityId);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketOutType.OpenWindow.getPacketData(packet);
        windowId = (Integer) o[0];
        type = (String) o[1];
        title = ChatTag.fromICBC(o[2]);
        slots = (Integer) o[3];
        entityId = (Integer) o[4];
    }
}

