package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.GameProfile;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.03..
 */
public class PacketLoginInStart extends WrappedPacket {
    public GameProfile gp;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.LoginInStart.newPacket(gp.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        gp = new GameProfile(PacketInType.LoginInStart.getPacketData(packet)[0]);
    }
}
