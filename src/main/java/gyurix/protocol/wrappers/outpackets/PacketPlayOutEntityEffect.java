package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityEffect extends WrappedPacket {
    public byte amplifier;
    public int duration;
    /**
     * 1  - Speed 2  - Slowness 3  - Haste 4  - Mining Fatigue 5  - Strength 6  - Instant Health 7  - Instant Damage 8
     * - Jump Boost 9  - Nausea 10 - Regeneration 11 - Resistance 12 - Fire Resistance 13 - Water Breathing 14 -
     * Invisibility 15 - Blindness 16 - Night Vision 17 - Hunger 18 - Weakness 19 - Poison 20 - Wither 21 - Health Boost
     * 22 - Absorption 23 - Saturation 24 - Glowing 25 - Levitation 26 - Luck 27 - Bad Luck
     */
    public byte effectId;
    public int entityId;
    /**
     * 0 - No particles, not ambient 1 - Ambient 2 - Particles 3 - Particles and ambient
     */
    public byte particles;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityEffect.newPacket(entityId, effectId, amplifier, duration, particles);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityEffect.getPacketData(packet);
        entityId = (int) d[0];
        effectId = (byte) d[1];
        amplifier = (byte) d[2];
        duration = (int) d[3];
        particles = (byte) d[4];
    }
}
