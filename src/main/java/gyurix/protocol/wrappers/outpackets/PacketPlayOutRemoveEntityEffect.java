package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutRemoveEntityEffect extends WrappedPacket {
    /**
     * 1  - Speed 2  - Slowness 3  - Haste 4  - Mining Fatigue 5  - Strength 6  - Instant Health 7  - Instant Damage 8
     * - Jump Boost 9  - Nausea 10 - Regeneration 11 - Resistance 12 - Fire Resistance 13 - Water Breathing 14 -
     * Invisibility 15 - Blindness 16 - Night Vision 17 - Hunger 18 - Weakness 19 - Poison 20 - Wither 21 - Health Boost
     * 22 - Absorption 23 - Saturation 24 - Glowing 25 - Levitation 26 - Luck 27 - Bad Luck
     */
    public byte effectId;
    public int entityId;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.RemoveEntityEffect.newPacket(entityId, effectId);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.RemoveEntityEffect.getPacketData(packet);
        entityId = (int) d[0];
        effectId = (byte) (int) d[1];
    }
}
