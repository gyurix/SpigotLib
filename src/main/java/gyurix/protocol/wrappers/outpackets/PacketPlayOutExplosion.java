package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutExplosion extends WrappedPacket {
    public ArrayList<BlockLocation> blocks = new ArrayList<>();
    public float pushX;
    public float pushY;
    public float pushZ;
    public float radius;
    public double x;
    public double y;
    public double z;

    public void fromVanillaBlockLocations(List l) {
        blocks = new ArrayList<>();
        for (Object o : l) {
            blocks.add(new BlockLocation(o));
        }
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.Explosion.newPacket(x, y, z, radius, toVanillaBlockLocations(), pushX, pushY, pushZ);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.Explosion.getPacketData(packet);
        x = (double) d[0];
        y = (double) d[1];
        z = (double) d[2];
        radius = (float) d[3];
        fromVanillaBlockLocations((List) d[4]);
        pushX = (float) d[5];
        pushY = (float) d[6];
        pushZ = (float) d[7];
    }

    public List toVanillaBlockLocations() {
        List out = new ArrayList();
        for (BlockLocation bl : blocks)
            out.add(bl.toNMS());
        return out;
    }
}
