package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.WrappedPacket;

public class PacketPlayInAbilities
        extends WrappedPacket {
    public boolean canFly;
    public boolean canInstantlyBuild;
    public float flySpeed;
    public boolean isFlying;
    public boolean isInvulnerable;
    public float walkSpeed;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.Abilities.newPacket(isInvulnerable, isFlying, canFly, canInstantlyBuild, Float.valueOf(flySpeed), Float.valueOf(walkSpeed));
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.Abilities.getPacketData(packet);
        isInvulnerable = (Boolean) data[0];
        isFlying = (Boolean) data[1];
        canFly = (Boolean) data[2];
        canInstantlyBuild = (Boolean) data[3];
        flySpeed = ((Float) data[4]).floatValue();
        walkSpeed = ((Float) data[5]).floatValue();
    }
}

