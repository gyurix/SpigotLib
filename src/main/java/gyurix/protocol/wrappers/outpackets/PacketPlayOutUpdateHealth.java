package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by gyurix on 25/11/2015.
 */
public class PacketPlayOutUpdateHealth extends WrappedPacket {
    public int food;
    public float health, saturation;

    public PacketPlayOutUpdateHealth() {
    }

    public PacketPlayOutUpdateHealth(float health, int food, float saturation) {
        this.health = health;
        this.food = food;
        this.saturation = saturation;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.UpdateHealth.newPacket(health, food, saturation);
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] d = PacketOutType.UpdateHealth.getPacketData(obj);
        health = (float) d[0];
        food = (int) d[1];
        saturation = (float) d[2];
    }
}
