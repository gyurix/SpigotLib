package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.utils.Direction;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Method;

public class PacketPlayInBlockDig extends WrappedPacket {
    public BlockLocation block;
    public DigType digType;
    public Direction direction;

    @Override
    public Object getVanillaPacket() {
        return Reflection.ver.isAbove(ServerVersion.v1_8) ? PacketInType.BlockDig.newPacket(block.toNMS(), direction == null ? null : direction.toNMS(), digType.toVanillaDigType()) :
                PacketInType.BlockDig.newPacket(block.x, block.y, block.z, direction.ordinal(), digType.ordinal());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketInType.BlockDig.getPacketData(packet);
        if (Reflection.ver.isAbove(ServerVersion.v1_8)) {
            block = new BlockLocation(d[0]);
            direction = d[1] == null ? null : Direction.valueOf(d[1].toString().toUpperCase());
            digType = DigType.valueOf(d[2].toString());
            return;
        }
        block = new BlockLocation((int) d[0], (int) d[1], (int) d[2]);
        direction = Direction.values()[(int) d[3]];
        digType = DigType.values()[(int) d[4]];
    }

    public enum DigType {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInBlockDig$EnumPlayerDigType"), "valueOf", String.class);
        }

        DigType() {
        }

        public Object toVanillaDigType() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

