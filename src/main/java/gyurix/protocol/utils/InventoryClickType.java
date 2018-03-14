package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2016.04.06..
 */
public enum InventoryClickType implements WrappedData {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL;
    static Method valueOf;

    static {
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            valueOf = Reflection.getMethod(Reflection.getNMSClass("InventoryClickType"), "valueOf", String.class);
    }

    @Override
    public Object toNMS() {
        if (Reflection.ver.isAbove(ServerVersion.v1_9))
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        return ordinal();
    }
}
