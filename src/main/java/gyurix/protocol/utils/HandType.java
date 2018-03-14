package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2016.03.26..
 */
public enum HandType implements WrappedData {
    MAIN_HAND,
    OFF_HAND;
    Method valueOf = Reflection.getMethod(Reflection.getNMSClass("EnumHand"), "valueOf", String.class);

    @Override
    public Object toNMS() {
        try {
            return valueOf.invoke(null, name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }
}
