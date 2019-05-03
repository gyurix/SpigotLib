package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;
import lombok.NoArgsConstructor;

/**
 * Created by GyuriX on 2016.02.28..
 */
@NoArgsConstructor
public class PacketPlayOutEntityEquipment extends WrappedPacket {
    public int entityId;
    public ItemStackWrapper item;
    /**
     * Slot number: 0 - held 1 - boots 2 - leggings 3 - chestplate 4 - helmet
     */
    public int slot;

    public PacketPlayOutEntityEquipment(int entityId, int slot, ItemStackWrapper item) {
        this.entityId = entityId;
        this.slot = slot;
        this.item = item;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityEquipment.newPacket(entityId, slot, item.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityEquipment.getPacketData(packet);
        entityId = (int) d[0];
        slot = (int) d[1];
        item = new ItemStackWrapper(d[2]);
    }
}
