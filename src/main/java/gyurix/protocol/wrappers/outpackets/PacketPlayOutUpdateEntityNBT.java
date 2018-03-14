package gyurix.protocol.wrappers.outpackets;

import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTTagType;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutUpdateEntityNBT extends WrappedPacket {
    public int entityId;
    public NBTCompound nbt;

    public PacketPlayOutUpdateEntityNBT() {
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.UpdateEntityNBT.newPacket(entityId, nbt.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.UpdateEntityNBT.getPacketData(obj);
        entityId = (int) data[0];
        nbt = (NBTCompound) NBTTagType.tag(data[1]);
    }
}
