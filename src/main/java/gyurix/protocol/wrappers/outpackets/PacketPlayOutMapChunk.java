package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;

import java.lang.reflect.Field;

/**
 * Created by GyuriX, on 2017. 03. 29..
 */
public class PacketPlayOutMapChunk extends WrappedPacket {
    public ChunkMap chunkMap;
    public int chunkX;
    public int chunkZ;
    public boolean groundUpContinuous;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.MapChunk.newPacket(chunkX, chunkZ, chunkMap.toNMS(), groundUpContinuous);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.MapChunk.getPacketData(packet);
        chunkX = (int) d[0];
        chunkZ = (int) d[1];
        chunkMap = new ChunkMap(d[2]);
        groundUpContinuous = (boolean) d[3];
    }

    public static class ChunkMap implements WrappedData {
        public static final Class nmsChunkMap = Reflection.getNMSClass("PacketPlayOutMapChunk$ChunkMap");
        private static final Field byteArrayF = Reflection.getFirstFieldOfType(nmsChunkMap, byte[].class);
        private static final Field intF = Reflection.getFirstFieldOfType(nmsChunkMap, int.class);
        public byte[] data;
        public int primaryBitMask;

        public ChunkMap() {

        }

        public ChunkMap(Object o) {
            try {
                primaryBitMask = intF.getInt(o);
                data = (byte[]) byteArrayF.get(o);
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }

        @Override
        public Object toNMS() {
            try {
                Object out = nmsChunkMap.newInstance();
                byteArrayF.set(out, data);
                intF.set(out, primaryBitMask);
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
            return null;
        }
    }
}
