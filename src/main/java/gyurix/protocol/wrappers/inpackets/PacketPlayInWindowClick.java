package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.InventoryClickType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInWindowClick
        extends WrappedPacket {
    public short actionNumber;
    public int button;
    public InventoryClickType clickType;
    public ItemStackWrapper item;
    public int slot;
    public int windowId;

    public PacketPlayInWindowClick() {

    }

    public PacketPlayInWindowClick(int windowId, int slot, int button, short actionNumber, ItemStackWrapper item, InventoryClickType clickType) {
        this.windowId = windowId;
        this.slot = slot;
        this.button = button;
        this.actionNumber = actionNumber;
        this.item = item;
        this.clickType = clickType;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketInType.WindowClick.newPacket(windowId, slot, button, actionNumber, item == null ? null : item.toNMS(), clickType.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] o = PacketInType.WindowClick.getPacketData(packet);
        windowId = (Integer) o[0];
        slot = (Integer) o[1];
        button = (Integer) o[2];
        actionNumber = (Short) o[3];
        item = new ItemStackWrapper(o[4]);
        clickType = Reflection.ver.isAbove(ServerVersion.v1_9) ? InventoryClickType.valueOf(o[5].toString()) : InventoryClickType.values()[(int) o[5]];
    }
}

