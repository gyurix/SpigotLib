package gyurix.mysql.autoupdate;

import gyurix.configfile.ConfigFile;
import gyurix.configfile.ConfigSerialization;
import gyurix.mysql.MySQLDatabase;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Map;

import static gyurix.json.JsonAPI.emptyTypeArray;

public interface AutoLoadableMap<K, V> extends AutoLoadable, Map<K, V> {
    @Override
    default void deleteAll() {
        String key = getKey();
        forEach((k, v) -> delete(key + "." + k, v));
    }

    @Override
    default void insertAll() {
        String key = getKey();
        forEach((k, v) -> insert(key + "." + k, v));
    }

    @Override
    default void updateAll() {
        String key = getKey();
        forEach((k, v) -> update(key + "." + k, v));
    }

    @Override
    default void load(Class type, Type[] types) {
        MySQLDatabase db = getDb();
        String key = getKey();
        SU.cs.sendMessage("§eLoad map:§f " + key + " - " + type.getName() + " - " + Arrays.toString(types));
        Class keyCl = ConfigSerialization.getNotInterfaceClass((Class) (types[0] instanceof Class ? types[0] : ((ParameterizedType) types[0]).getRawType()));
        Type[] keyTypes = types[0] instanceof Class ? emptyTypeArray : ((ParameterizedType) types[0]).getActualTypeArguments();
        Class valueCl = ConfigSerialization.getNotInterfaceClass((Class) (types[1] instanceof Class ? types[1] : ((ParameterizedType) types[1]).getRawType()));
        Type[] valueTypes = types[1] instanceof Class ? emptyTypeArray : ((ParameterizedType) types[1]).getActualTypeArguments();
        try {
            ResultSet rs = db.querry("SELECT * FROM `" + db.table + "` WHERE `key` LIKE ?", key + ".%");
            while (rs.next()) {
                String[] subKey = rs.getString(1).substring(key.length() + 1).split("\\.");
                String value = rs.getString(2);
                K keyObj = (K) new ConfigFile(subKey[0]).data.deserialize(keyCl, keyTypes);
                V data = get(keyObj);
                if (data == null) {
                    if (subKey.length == 1)
                        data = (V) new ConfigFile(value).data.deserialize(valueCl, valueTypes);
                    else
                        data = (V) Reflection.newInstance(valueCl);
                    if (data instanceof AutoUpdatable) {
                        AutoUpdatable au = ((AutoUpdatable) data);
                        au.setup(key + "." + subKey[0], db);
                        if (data instanceof AutoLoadable)
                            ((AutoLoadable) data).load(valueCl, valueTypes);
                    }
                    putNow(keyObj, data);
                }
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    default V put(K key, V value) {
        V out = putNow(key, value);
        if (out != value) {
            if (out == null)
                insert(getKey() + "." + key, value);
            else
                update(getKey() + "." + key, value);
        }
        return out;
    }

    @Override
    default V remove(Object key) {
        V removed = removeNow(key);
        if (removed != null)
            delete(getKey() + "." + key, removed);
        return removed;
    }

    V putNow(K key, V value);

    V removeNow(Object key);
}
