package gyurix.map;

import gyurix.protocol.wrappers.outpackets.PacketPlayOutMap;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.EntityUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static gyurix.protocol.Reflection.*;

/**
 * Created by GyuriX on 2016. 07. 06..
 */
public class MapData {
    private static final Object itemWorldMap, worldMap;
    private static final Method mapGenMethod;
    private static final MapView view;
    private static final byte[] viewColors;

    static {
        Class cl = getNMSClass("ItemWorldMap");
        Class cl2 = getNMSClass("WorldMap");
        itemWorldMap = newInstance(cl);
        worldMap = newInstance(cl2, new Class[]{String.class}, "map_0");
        view = (MapView) getFieldData(cl2, "mapView", worldMap);
        view.getRenderers().clear();
        viewColors = (byte[]) getFieldData(cl2, "colors", worldMap);
        mapGenMethod = getMethod(cl, "a", getNMSClass("World"), getNMSClass("Entity"), cl2);
    }

    public final ArrayList<MapIcon> icons = new ArrayList<>();
    private final byte[] colors = new byte[16384];
    public int mapId = 1, centerX, centerZ;
    public Scale scale;
    public boolean showIcons = true;
    public World world;

    public void clear(byte color) {
        if (color < 0 && color > -113)
            color = 0;
        for (int i = 0; i < 16384; i++)
            colors[i] = color;
    }

    public byte[] cloneColors() {
        return colors.clone();
    }

    public void setColor(int x, int y, byte color) {
        if (color < 0 && color > -113)
            color = 0;
        if (x > -1 && x < 128 && y > -1 && y < 128)
            colors[x + y * 128] = color;
    }

    public void setColor(int x, int y, int xLen, int yLen, byte color) {
        if (color < 0 && color > -113)
            color = 0;
        for (int cx = 0; cx < xLen; cx++) {
            for (int cy = 0; cy < yLen; cy++) {
                if (x + cx > -1 && x + cx < 128 && y + cy > -1 && y + cy < 128)
                    colors[x + cx + (y + cy) * 128] = color;
            }
        }
    }

    public void setVanillaMapGenData(Player plr) {
        view.setWorld(world);
        view.setCenterX(centerX);
        view.setCenterZ(centerZ);
        view.setScale(scale);
        try {
            mapGenMethod.invoke(itemWorldMap, EntityUtils.getNMSWorld(world), EntityUtils.getNMSEntity(plr), worldMap);
            System.arraycopy(viewColors, 0, colors, 0, 16384);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public void update(Player plr) {
        SU.tp.sendPacket(plr, new PacketPlayOutMap(this));
    }
}
