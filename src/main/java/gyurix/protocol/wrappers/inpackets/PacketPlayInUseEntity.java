package gyurix.protocol.wrappers.inpackets;


import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.HandType;
import gyurix.protocol.utils.Vector;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Method;

public class PacketPlayInUseEntity extends WrappedPacket {
    public EntityUseAction action;
    public int entityId;
    public HandType hand;
    public Vector targetLocation;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.UseEntity.newPacket(entityId, action.toNMS(), targetLocation == null ? null : targetLocation.toNMS(), hand == null ? null : hand.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketInType.UseEntity.getPacketData(packet);
        entityId = (int) d[0];
        action = EntityUseAction.valueOf(d[1].toString());
        targetLocation = d[2] == null ? null : new Vector(d[2]);
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            hand = d[3] == null ? null : HandType.valueOf(d[3].toString());
    }

    public enum EntityUseAction implements WrappedData {
        INTERACT,
        ATTACK,
        INTERACT_AT;
        Method valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInUseEntity$EnumEntityUseAction"), "valueOf", String.class);

        @Override
        public Object toNMS() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }

    }
}

