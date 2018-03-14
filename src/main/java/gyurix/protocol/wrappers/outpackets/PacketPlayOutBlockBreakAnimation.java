package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutBlockBreakAnimation extends WrappedPacket {
    public BlockLocation block;
    /**
     * 0-9, other value = remove effect
     */
    public int destroyStage;
    public int entityId;

    public PacketPlayOutBlockBreakAnimation() {

    }

    public PacketPlayOutBlockBreakAnimation(int entityId, BlockLocation block, int destroyStage) {
        this.entityId = entityId;
        this.block = block;
        this.destroyStage = destroyStage;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.BlockBreakAnimation.newPacket(entityId, block.toNMS(), destroyStage);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.BlockBreakAnimation.getPacketData(packet);
        entityId = (int) d[0];
        block = new BlockLocation(d[1]);
        destroyStage = (int) d[2];
    }
}
