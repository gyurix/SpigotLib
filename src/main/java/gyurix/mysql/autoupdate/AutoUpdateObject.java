package gyurix.mysql.autoupdate;

import gyurix.configfile.ConfigFile;
import gyurix.mysql.MySQLDatabase;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;

import static gyurix.json.JsonAPI.emptyTypeArray;

@Getter
public class AutoUpdateObject implements AutoUpdatable {
    private MySQLDatabase db;
    private String key;

    @Override
    public void deleteAll() {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                delete(key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void insertAll() {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                insert(key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
        SU.cs.sendMessage("§eLoad object:§f " + key + " - " + getClass().getName());
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                if (AutoUpdatable.class.isAssignableFrom(f.getType())) {
                    AutoUpdatable o = (AutoUpdatable) f.getType().newInstance();
                    o.setup(key == null ? f.getName() : (key + "." + f.getName()), db);
                    f.set(this, o);
                    if (AutoLoadable.class.isAssignableFrom(f.getType())) {
                        Class type = f.getType();
                        Type[] types = f.getGenericType() instanceof Class ? emptyTypeArray : ((ParameterizedType) f.getGenericType()).getActualTypeArguments();
                        Type t = types[0];
                        types = t instanceof Class ? emptyTypeArray : ((ParameterizedType) t).getActualTypeArguments();
                        type = (Class) (t instanceof Class ? t : ((ParameterizedType) t).getRawType());
                        ((AutoLoadable) o).load(type, types);
                    }
                } else {
                    ResultSet rs = db.querry("SELECT `value` FROM `" + db.table + "` WHERE `key` = ? LIMIT 1", key + "." + f.getName());
                    if (rs.next())
                        f.set(this, new ConfigFile(rs.getString(1)).data.deserialize(f.getType()));
                }
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void updateAll() {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                update(key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }
}
