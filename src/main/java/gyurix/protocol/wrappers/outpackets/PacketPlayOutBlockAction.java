package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.BlockUtils;

/**
 * Created by GyuriX, on 2017. 02. 05..
 */
public class PacketPlayOutBlockAction extends WrappedPacket {
    public int actionId, actionData, blockId;
    public BlockLocation loc;

    public PacketPlayOutBlockAction() {
    }

    public PacketPlayOutBlockAction(BlockLocation loc, int actionId, int actionData, int blockId) {
        this.loc = loc;
        this.actionId = actionId;
        this.actionData = actionData;
        this.blockId = blockId;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.BlockAction.newPacket(loc.toNMS(), actionId, actionData, BlockUtils.getNMSBlockType(blockId));
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.BlockAction.getPacketData(packet);
        loc = new BlockLocation(d[0]);
        actionId = (int) d[1];
        actionData = (int) d[2];
        blockId = BlockUtils.getNMSBlockTypeId(d[3]);
    }
}
