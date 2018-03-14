package gyurix.map;

import gyurix.json.JsonAPI;
import gyurix.json.JsonSettings;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.WrappedData;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Created by GyuriX on 2016. 07. 06..
 */
public class MapIcon implements WrappedData {
    @JsonSettings(serialize = false)
    private static final Constructor con;
    @JsonSettings(serialize = false)
    private static final Field fType, fX, fY, fRotation;

    static {
        Class cl = Reflection.getNMSClass("MapIcon");
        con = Reflection.getConstructor(cl, byte.class, byte.class, byte.class, byte.class);
        fType = Reflection.getField(cl, "type");
        fX = Reflection.getField(cl, "x");
        fY = Reflection.getField(cl, "y");
        fRotation = Reflection.getField(cl, "rotation");
    }

    public byte rotation;
    public byte type;
    public byte x;
    public byte y;

    public MapIcon(Object nmsMapIcon) {
        try {
            type = (byte) fType.get(nmsMapIcon);
            x = (byte) fX.get(nmsMapIcon);
            y = (byte) fY.get(nmsMapIcon);
            rotation = (byte) fRotation.get(nmsMapIcon);
        } catch (Throwable e) {

        }
    }

    public MapIcon(int type, int x, int y, int rotation) {
        this.type = (byte) type;
        this.x = (byte) x;
        this.y = (byte) y;
        this.rotation = (byte) rotation;
    }

    @Override
    public Object toNMS() {
        try {
            return con.newInstance(type, x, y, rotation);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    @Override
    public String toString() {
        return JsonAPI.serialize(this);
    }
}
