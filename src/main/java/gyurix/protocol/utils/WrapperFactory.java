package gyurix.protocol.utils;

import com.google.common.primitives.Primitives;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.GameProfile.Property;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class WrapperFactory {
    public static final HashMap<Class, Method> enumWrap = new HashMap<>();
    public static final HashMap<Class, Constructor> wrap = new HashMap<>();

    public static void init() {
        try {
            wrap.put(Reflection.getNMSClass("BaseBlockPosition"), BlockLocation.class.getConstructor(Object.class));
            wrap.put(Reflection.getNMSClass("DataWatcher"), DataWatcher.class.getConstructor(Object.class));
            enumWrap.put(Reflection.getNMSClass("EnumDirection"), Direction.class.getMethod("valueOf", String.class));
            wrap.put(Reflection.getUtilClass("com.mojang.authlib.GameProfile"), GameProfile.class.getConstructor(Object.class));
            wrap.put(Reflection.getUtilClass("com.mojang.authlib.properties.Property"), Property.class.getConstructor(Object.class));
            wrap.put(Reflection.getNMSClass("ItemStack"), ItemStackWrapper.class.getConstructor(Object.class));
            wrap.put(Reflection.getNMSClass("Vec3D"), Vector.class.getConstructor(Object.class));
            wrap.put(Reflection.getNMSClass("Vector3f"), Rotation.class.getConstructor(Object.class));
            enumWrap.put(Reflection.getNMSClass("WorldType"), WorldType.class.getMethod("valueOf", String.class));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public static Object unwrap(Object o) {
        if (o == null)
            return null;
        if (o instanceof WrappedData)
            return ((WrappedData) o).toNMS();
        return o;
    }

    public static Object wrap(Object o) {
        if (o == null)
            return null;
        Class cl = Primitives.unwrap(o.getClass());
        try {
            Constructor con = wrap.get(cl);
            if (con != null)
                return con.newInstance(o);
            Method m = enumWrap.get(cl);
            if (m != null)
                return m.invoke(null, o.toString());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return o;
    }
}
