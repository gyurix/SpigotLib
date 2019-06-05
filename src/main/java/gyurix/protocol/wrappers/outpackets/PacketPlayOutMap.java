package gyurix.protocol.wrappers.outpackets;

import gyurix.json.JsonSettings;
import gyurix.map.MapData;
import gyurix.map.MapIcon;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import static gyurix.protocol.Reflection.ver;
import static gyurix.spigotutils.ServerVersion.v1_9;

/**
 * Created by GyuriX on 2016. 07. 06..
 */
public class PacketPlayOutMap extends WrappedPacket {
  @JsonSettings(serialize = false)
  private static final Constructor con;
  @JsonSettings(serialize = false)
  private static final Class[] newer = new Class[]{int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class},
          v1_8 = new Class[]{int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class};

  static {
    con = Reflection.getConstructor(Reflection.getNMSClass("PacketPlayOutMap"), ver.isAbove(v1_9) ? newer : v1_8);
  }

  public int columns;
  public byte[] data;
  public ArrayList<MapIcon> icons = new ArrayList<>();
  public int mapId;
  public int rows;
  /**
   * From 0 to 4
   * 0 - 1 x 1 blocks per pixel (fully zoomed-in map)
   * 1 - 2 x 2 blocks per pixel
   * 2 - 4 x 4 blocks per pixel
   * 3 - 8 x 8 blocks per pixel
   * 4 - 16 x 16 blocks per pixel
   */
  public byte scale;
  public boolean showIcons;
  public int x;
  public int z;

  public PacketPlayOutMap() {

  }

  public PacketPlayOutMap(MapData mapData) {
    mapId = mapData.getMapId();
    scale = mapData.getScale().getValue();
    showIcons = mapData.isShowIcons();
    icons = mapData.getIcons();
    columns = 0;
    rows = 0;
    x = 128;
    z = 128;
    data = mapData.cloneColors();
  }

  public ArrayList<Object> getNMSIcons() {
    ArrayList<Object> out = new ArrayList<>();
    if (icons == null)
      return out;
    for (MapIcon ic : icons) {
      out.add(ic.toNMS());
    }
    return out;
  }

  @Override
  public Object getVanillaPacket() {
    try {
      return ver.isAbove(v1_9) ? con.newInstance(mapId, scale, showIcons, getNMSIcons(), data, columns, rows, x, z)
              : con.newInstance(mapId, scale, getNMSIcons(), data, columns, rows, x, z);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.Map.getPacketData(packet);
    mapId = (int) d[0];
    scale = (byte) d[1];
    int st = 2;
    if (ver.isAbove(v1_9))
      showIcons = (boolean) d[st++];
    Object[] nmsIcons = (Object[]) d[st++];
    icons.clear();
    for (Object nmsIcon : nmsIcons) icons.add(new MapIcon(nmsIcon));
    columns = (int) d[st++];
    rows = (int) d[st++];
    x = (int) d[st++];
    z = (int) d[st++];
    data = (byte[]) d[st++];
  }
}
