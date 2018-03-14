package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.BlockUtils;

/**
 * Created by GyuriX, on 2017. 02. 05..
 */
public class PacketPlayOutBlockChange extends WrappedPacket {
    public byte blockData;
    public int blockId;
    public BlockLocation loc;

    public PacketPlayOutBlockChange() {
    }

    public PacketPlayOutBlockChange(BlockLocation loc, int blockId, byte blockData) {
        this.loc = loc;
        this.blockId = blockId;
        this.blockData = blockData;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.BlockChange.newPacket(loc.toNMS(), BlockUtils.getNMSBlock(blockId, blockData));
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.BlockChange.getPacketData(packet);
        loc = new BlockLocation(d[0]);
        int id = BlockUtils.getCombinedId(d[1]);
        blockId = id & 4095;
        blockData = (byte) (id >> 12 & 15);
    }
}
