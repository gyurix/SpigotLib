package gyurix.spigotlib;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization.Serializer;
import gyurix.economy.EconomyAPI;
import gyurix.protocol.Reflection;
import gyurix.sign.SignConfig;
import gyurix.spigotutils.ItemUtils;
import gyurix.spigotutils.TPSMeter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.script.ScriptException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static gyurix.api.VariableAPI.handlers;
import static gyurix.configfile.ConfigSerialization.getSerializers;

/**
 * Collection of custom config serializers and custom variable handlers used only on Spigot
 */
public class ConfigHook {
    /**
     * Data storage for dget and dstore placeholders
     */
    public static HashMap<String, Object> data = new HashMap<>();

    /**
     * Registers the custom serializers used only on Spigot
     */
    public static void registerSerializers() {
        HashMap<Class, Serializer> serializers = getSerializers();
        serializers.put(SignConfig.class, new Serializer() {
            @Override
            public Object fromData(ConfigData cd, Class cl, Type... paramVarArgs) {
                SignConfig sc = new SignConfig();
                for (int i = 0; i < 4; i++) {
                    String s = cd.listData.get(i).stringData;
                    sc.lines.add(s.equals(" ") ? "" : s);
                }
                return sc;
            }

            @Override
            public ConfigData toData(Object sco, Type... paramVarArgs) {
                SignConfig sc = (SignConfig) sco;
                ConfigData cd = new ConfigData();
                cd.listData = new ArrayList<>();
                for (int i = 0; i < 4; i++)
                    cd.listData.add(new ConfigData(sc.lines.get(i)));
                return cd;
            }
        });
        serializers.put(Vector.class, new Serializer() {
            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 3);
                return new Vector(Double.valueOf(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]));
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Vector v = (Vector) obj;
                return new ConfigData(String.valueOf(v.getX()) + ' ' + v.getY() + ' ' + v.getZ());
            }
        });
        serializers.put(Location.class, new Serializer() {
            @Override
            public Object fromData(ConfigData data, Class paramClass, Type... paramVarArgs) {
                String[] s = data.stringData.split(" ", 6);
                if (s.length == 4) {
                    return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
                }
                return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]), Float.valueOf(s[4]), Float.valueOf(s[5]));
            }

            @Override
            public ConfigData toData(Object obj, Type... paramVarArgs) {
                Location loc = (Location) obj;
                if (loc.getPitch() == 0.0f && loc.getYaw() == 0.0f) {
                    return new ConfigData(loc.getWorld().getName() + ' ' + loc.getX() + ' ' + loc.getY() + ' ' + loc.getZ());
                }
                return new ConfigData(loc.getWorld().getName() + ' ' + loc.getX() + ' ' + loc.getY() + ' ' + loc.getZ() + ' ' + loc.getYaw() + ' ' + loc.getPitch());
            }
        });
        serializers.put(ItemStack.class, new ItemSerializer());
    }

    /**
     * Registers the built in custom variable handlers
     */
    public static void registerVariables() {
        handlers.put("eval", (plr, inside, oArgs) -> {
            String s = StringUtils.join(inside, "");
            try {
                return SU.js.eval(s);
            } catch (ScriptException e) {
                return "<eval:" + s + '>';
            }
        });
        handlers.put("tobool", (plr, inside, oArgs) -> Boolean.valueOf(StringUtils.join(inside, "")));
        handlers.put("tobyte", (plr, inside, oArgs) -> (byte) Double.valueOf(StringUtils.join(inside, "")).doubleValue());
        handlers.put("toshort", (plr, inside, oArgs) -> (short) Double.valueOf(StringUtils.join(inside, "")).doubleValue());
        handlers.put("toint", (plr, inside, oArgs) -> (int) Double.valueOf(StringUtils.join(inside, "")).doubleValue());
        handlers.put("tolong", (plr, inside, oArgs) -> (long) Double.valueOf(StringUtils.join(inside, "")).doubleValue());
        handlers.put("tofloat", (plr, inside, oArgs) -> Float.valueOf(StringUtils.join(inside, "")));
        handlers.put("todouble", (plr, inside, oArgs) -> Double.valueOf(StringUtils.join(inside, "")));
        handlers.put("tostr", (plr, inside, oArgs) -> StringUtils.join(inside, ""));
        handlers.put("toarray", (plr, inside, oArgs) -> inside.toArray());
        handlers.put("substr", (plr, inside, oArgs) -> {
            String[] s = StringUtils.join(inside, "").split(" ", 3);
            int from = Integer.valueOf(s[0]);
            int to = Integer.valueOf(s[1]);
            return s[2].substring(from < 0 ? s[2].length() + from : from, to < 0 ? s[2].length() + to : to);
        });
        handlers.put("splits", (plr, inside, oArgs) -> StringUtils.join(inside, "").split(" "));
        handlers.put("splitlen", (plr, inside, oArgs) -> {
            String[] s = StringUtils.join(inside, "").split(" ", 3);
            Integer max = Integer.valueOf(s[0]);
            String pref = SU.unescapeText(s[1]);
            String text = s[2];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i += max) {
                sb.append('\n').append(pref).append(text.substring(i, Math.min(text.length(), i + max)));
            }
            return sb.length() == 0 ? "" : sb.substring(1);
        });
        handlers.put("noout", (plr, inside, oArgs) -> "");
        handlers.put("lang", (plr, inside, oArgs) -> {
            String s = StringUtils.join(inside, "");
            String lang = SU.getPlayerConfig(plr).getString("lang");
            return s.isEmpty() ? lang : GlobalLangFile.get(lang, s);
        });
        handlers.put("booltest", (plr, inside, oArgs) -> {
            String[] s = StringUtils.join(inside, "").split(";");
            return Boolean.valueOf(s[0]) ? s[1] : s[2];
        });
        handlers.put("args", (plr, inside, oArgs) -> {
            int id = Integer.valueOf(StringUtils.join(inside, ""));
            return oArgs[id];
        });
        handlers.put("len", (plr, inside, oArgs) -> {
            Object o = inside.get(0);
            return o.getClass().isArray() ? Array.getLength(o) : ((Collection) o).size();
        });
        handlers.put("iarg", (plr, inside, oArgs) -> {
            int id = Integer.valueOf(inside.get(0).toString());
            return inside.get(id);
        });
        handlers.put("plr", (plr, inside, oArgs) -> Reflection.getData(plr, inside));
        handlers.put("obj", (plr, inside, oArgs) -> Reflection.getData(oArgs[0], inside));
        handlers.put("iobj", (plr, inside, oArgs) -> {
            Object obj = inside.remove(0);
            return Reflection.getData(obj, inside);
        });
        handlers.put("dstore", (plr, inside, oArgs) -> {
            if (inside.size() == 1) {
                String[] s = StringUtils.join(inside, "").split(" ", 2);
                return data.put(s[0], s[1]);
            }
            return data.put(inside.get(0).toString(), inside.get(1));
        });
        handlers.put("dget", (plr, inside, oArgs) -> data.get(StringUtils.join(inside, "")));
        handlers.put("tps", (plr, inside, oArgs) -> TPSMeter.tps);
        handlers.put("real", (plr, inside, oArgs) -> System.currentTimeMillis());
        handlers.put("formattime", (plr, inside, oArgs) -> {
            String str = StringUtils.join(inside, "");
            int id = str.indexOf(' ');
            long time = Long.valueOf(str.substring(0, id));
            String format = str.substring(id + 1);
            return new SimpleDateFormat(format).format(time);
        });
        handlers.put("balf", (plr, inside, oArgs) -> {
            if (inside == null || inside.isEmpty()) {
                return EconomyAPI.balanceTypes.get("default").format(EconomyAPI.getBalance(plr.getUniqueId()).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            String str = StringUtils.join(inside, "");
            return EconomyAPI.balanceTypes.get(str).format(EconomyAPI.getBalance(plr.getUniqueId(), str).setScale(2, BigDecimal.ROUND_HALF_UP));
        });
    }

    /**
     * Custom config Serializer used for saving and loading ItemStacks
     */
    public static class ItemSerializer implements Serializer {
        @Override
        public Object fromData(ConfigData data, Class cl, Type... paramVarArgs) {
            return ItemUtils.stringToItemStack(data.stringData);
        }

        @Override
        public ConfigData toData(Object is, Type... paramVarArgs) {
            return new ConfigData(ItemUtils.itemToString((ItemStack) is));
        }
    }

}

