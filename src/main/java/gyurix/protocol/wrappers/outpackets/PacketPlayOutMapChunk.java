package gyurix.protocol.wrappers.outpackets;

import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTTagType;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GyuriX, on 2017. 03. 29..
 */
public class PacketPlayOutMapChunk extends WrappedPacket {
  public byte[] chunkData;
  public ChunkMap chunkMap;
  public int chunkX;
  public int chunkZ;
  public NBTCompound heightMaps = new NBTCompound();
  public boolean groundUpContinuous;
  public int primaryBitMask;
  public List<NBTCompound> tileEntities = new ArrayList<>();

  @Override
  public Object getVanillaPacket() {
    if (Reflection.ver.isAbove(ServerVersion.v1_14))
      return PacketOutType.MapChunk.newPacket(chunkX, chunkZ, primaryBitMask, heightMaps.toNMS(), chunkData, toNMSTileEntityList(), groundUpContinuous);
    if (Reflection.ver.isAbove(ServerVersion.v1_13))
      return PacketOutType.MapChunk.newPacket(chunkX, chunkZ, primaryBitMask, chunkData, toNMSTileEntityList(), groundUpContinuous);
    return PacketOutType.MapChunk.newPacket(chunkX, chunkZ, chunkMap.toNMS(), groundUpContinuous);
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.MapChunk.getPacketData(packet);
    chunkX = (int) d[0];
    chunkZ = (int) d[1];
    if (Reflection.ver.isAbove(ServerVersion.v1_13)) {
      primaryBitMask = (int) d[2];
      int from = 3;
      if (Reflection.ver.isAbove(ServerVersion.v1_14))
        heightMaps = (NBTCompound) NBTTagType.Compound.makeTag(d[from++]);
      chunkData = (byte[]) d[from++];
      for (Object o : (List) d[from++])
        tileEntities.add((NBTCompound) NBTTagType.tag(o));
      groundUpContinuous = (boolean) d[from++];
    } else {
      chunkMap = new ChunkMap(d[2]);
      groundUpContinuous = (boolean) d[3];
    }
  }

  public List<Object> toNMSTileEntityList() {
    List<Object> out = new ArrayList<>();
    for (NBTCompound tag : tileEntities)
      out.add(tag.toNMS());
    return out;
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
