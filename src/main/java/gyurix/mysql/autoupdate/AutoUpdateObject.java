package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Field;

public class AutoUpdateObject implements AutoUpdatable {
    private MySQLDatabase db;
    private String key;

    @Override
    public void deleteAll(MySQLDatabase db) {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                delete(db, key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void insertAll(MySQLDatabase db) {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                insert(db, key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                if (f.getType().isAssignableFrom(AutoUpdatable.class)) {
                    AutoUpdatable o = (AutoUpdatable) f.getType().newInstance();
                    o.setup(key + "." + f.getName(), db);
                    f.set(this, o);
                }
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public void updateAll(MySQLDatabase db) {
        try {
            for (Field f : Reflection.getAllFields(getClass())) {
                Object o = f.get(this);
                update(db, key + "." + f.getName(), o);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }
}
