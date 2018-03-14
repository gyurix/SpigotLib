package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInEnchantItem
        extends WrappedPacket {
    public int enchantment;
    public int window;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.EnchantItem.newPacket(window, enchantment);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.EnchantItem.getPacketData(packet);
        window = (Integer) data[0];
        enchantment = (Integer) data[1];
    }
}

