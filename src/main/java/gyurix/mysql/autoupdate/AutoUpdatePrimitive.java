package gyurix.mysql.autoupdate;

import gyurix.configfile.ConfigFile;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotlib.SU;
import lombok.Getter;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.Arrays;

@Getter
public class AutoUpdatePrimitive<T> implements AutoLoadable {
    private MySQLDatabase db;
    private String key;
    private T value;

    @Override
    public void deleteAll() {
        delete(key, value);
    }

    @Override
    public void insertAll() {
        insert(key, value);
    }

    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
    }

    @Override
    public void updateAll() {
        update(key, value);
    }

    public void init(T value) {
        this.value = value;
        insert(key, this.value);
    }

    @Override
    public void load(Class type, Type[] types) {
        SU.cs.sendMessage("§eLoad primitive:§f " + key + " - " + type.getName() + " - " + Arrays.toString(types));
        try {
            ResultSet rs = db.querry("SELECT `value` FROM `" + db.table + "` WHERE `key` = ? LIMIT 1", key);
            if (rs.next()) {
                value = (T) new ConfigFile(rs.getString(1)).data.deserialize(type, types);
                SU.cs.sendMessage("§eValue = §f" + value);
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public void setValue(T value) {
        this.value = value;
        update(key, value);
    }

    public void update() {
        update(key, value);
    }
}
