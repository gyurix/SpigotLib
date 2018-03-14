package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInSetCreativeSlot extends WrappedPacket {
    public ItemStackWrapper itemStack;
    public int slot;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.SetCreativeSlot.newPacket(slot, itemStack == null ? null : itemStack.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.SetCreativeSlot.getPacketData(packet);
        slot = (Integer) data[0];
        itemStack = new ItemStackWrapper(data[1]);
    }
}

