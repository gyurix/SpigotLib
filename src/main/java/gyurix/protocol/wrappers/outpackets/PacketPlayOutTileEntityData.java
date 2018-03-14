package gyurix.protocol.wrappers.outpackets;

import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTTagType;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayOutTileEntityData extends WrappedPacket {
    public int action;
    public BlockLocation block;
    public NBTCompound nbt;

    public PacketPlayOutTileEntityData() {
    }

    public PacketPlayOutTileEntityData(BlockLocation block, int action, NBTCompound nbt) {
        this.block = block;
        this.action = action;
        this.nbt = nbt;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.TileEntityData.newPacket(block.toNMS(), action, nbt.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.TileEntityData.getPacketData(obj);
        block = new BlockLocation(data[0]);
        action = (int) data[1];
        nbt = (NBTCompound) NBTTagType.tag(data[2]);
    }
}
