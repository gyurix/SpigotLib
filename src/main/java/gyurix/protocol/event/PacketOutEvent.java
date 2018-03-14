package gyurix.protocol.event;

import org.bukkit.entity.Player;

public class PacketOutEvent extends PacketEvent {
    private final PacketOutType type;

    public PacketOutEvent(Object channel, Player plr, Object packet) {
        super(channel, plr, packet);
        type = PacketOutType.getType(packet);
    }

    @Override
    public Object[] getPacketData() {
        return type.getPacketData(packet);
    }

    @Override
    public void setPacketData(Object... data) {
        type.fillPacket(packet, data);
    }

    @Override
    public boolean setPacketData(int id, Object o) {
        try {
            type.fs.get(id).set(packet, o);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public PacketOutType getType() {
        return type;
    }
}

