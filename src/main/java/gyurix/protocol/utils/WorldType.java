package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2016.02.28..
 */
public enum WorldType implements WrappedData {
    DEFAULT, FLAT, LARGEBIOMES, AMPLIFIED, CUSTOMIZED, DEBUG_ALL_BLOCK_STATES, DEFAULT_1_1;
    public static final Method enumDifficultyVO;
    public static final Class enumGmCl = Reflection.getNMSClass("WorldSettings$EnumGamemode"),
            enumDifficultyCl = Reflection.getNMSClass("EnumDifficulty"),
            worldTypeCl = Reflection.getNMSClass("WorldType");
    public static final Method enumGmVO = Reflection.getMethod(enumGmCl, "valueOf", String.class);
    private static Field name = Reflection.getField(Reflection.getNMSClass("WorldType"), "name");
    private static Method valueOf = Reflection.getMethod(worldTypeCl, "getType", String.class);

    static {
        enumDifficultyVO = Reflection.getMethod(enumDifficultyCl, "getById", int.class);
    }

    public static WorldType fromVanillaWorldType(Object vanilla) {
        try {
            return valueOf(((String) name.get(vanilla)).toUpperCase());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object toVanillaDifficulty(Difficulty mode) {
        try {
            return enumDifficultyVO.invoke(null, mode.getValue());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static Object toVanillaGameMode(GameMode mode) {
        try {
            return enumGmVO.invoke(null, mode.name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public Object toNMS() {
        try {
            return valueOf.invoke(null, name());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}
