package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Method;

public class PacketPlayInEntityAction
        extends WrappedPacket {
    public PlayerAction action;
    public int entityId;
    public int jumpBoost;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.EntityAction.newPacket(entityId, action.toVanillaPlayerAction(), jumpBoost);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketInType.EntityAction.getPacketData(packet);
        entityId = (Integer) d[0];
        action = Reflection.ver.isAbove(ServerVersion.v1_8) ? PlayerAction.valueOf(d[1].toString()) : PlayerAction.values()[((Integer) d[1]) - 1];
        jumpBoost = (Integer) d[2];
    }

    public enum PlayerAction {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        RIDING_JUMP,
        OPEN_INVENTORY;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInEntityAction$EnumPlayerAction"), "valueOf", String.class);
        }

        PlayerAction() {
        }

        public Object toVanillaPlayerAction() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
                return null;
            }
        }
    }

}

