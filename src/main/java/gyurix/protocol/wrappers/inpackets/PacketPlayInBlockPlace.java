package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.utils.HandType;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

public class PacketPlayInBlockPlace extends WrappedPacket {
    public float cursorX;
    public float cursorY;
    public float cursorZ;
    public Direction face;
    public HandType hand;
    public ItemStackWrapper itemStack;
    public BlockLocation location;
    public long timestamp;

    @Override
    public Object getVanillaPacket() {
        Object[] d;
        if (Reflection.ver == ServerVersion.v1_7) {
            d = new Object[8];
            d[0] = location.x;
            d[1] = location.y;
            d[2] = location.z;
            d[3] = face == null ? 255 : face.ordinal();
            d[4] = itemStack == null ? null : itemStack.toNMS();
            d[5] = cursorX;
            d[6] = cursorY;
            d[7] = cursorZ;
        } else if (Reflection.ver == ServerVersion.v1_8) {
            d = new Object[7];
            d[0] = location.toNMS();
            d[1] = face == null ? 255 : face.ordinal();
            d[2] = itemStack == null ? null : itemStack.toNMS();
            d[3] = cursorX;
            d[4] = cursorY;
            d[5] = cursorZ;
            d[6] = timestamp;
        } else {
            d = new Object[2];
            d[0] = hand.toNMS();
            d[1] = timestamp;
        }
        return PacketInType.BlockPlace.newPacket(d);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.BlockPlace.getPacketData(packet);
        if (Reflection.ver.isBellow(ServerVersion.v1_8)) {
            int st = 1;
            if (Reflection.ver == ServerVersion.v1_8) {
                location = new BlockLocation(data[0]);
            } else {
                location = new BlockLocation((int) data[0], (int) data[1], (int) data[2]);
                st = 3;
            }
            face = Direction.get((Integer) data[st]);
            itemStack = data[st + 1] == null ? null : new ItemStackWrapper(data[st + 1]);
            cursorX = (Float) data[st + 2];
            cursorY = (Float) data[st + 3];
            cursorZ = (Float) data[st + 4];
            timestamp = Reflection.ver.isAbove(ServerVersion.v1_8) ? (Long) data[st + 5] : System.currentTimeMillis();
        } else {
            hand = HandType.valueOf(data[0].toString());
            timestamp = (Long) data[1];
        }
    }
}

