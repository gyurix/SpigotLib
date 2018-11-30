package gyurix.mysql.autoupdate;

import gyurix.configfile.ConfigData;
import gyurix.mysql.MySQLDatabase;

import static gyurix.spigotutils.SafeTools.async;

public interface AutoUpdatable {
    default void delete(MySQLDatabase db, String key, Object value) {
        async(() -> {
            if (value instanceof AutoUpdatable)
                ((AutoUpdatable) value).deleteAll(db);
            db.command("DELETE FROM `" + db.table + "` WHERE `key` = ?", key);
        });
    }

    void deleteAll(MySQLDatabase db);

    default void insert(MySQLDatabase db, String key, Object value) {
        if (value instanceof AutoUpdatable)
            insertAll(db);
        else
            async(() -> db.command("INSERT INTO `" + db.table + "` VALUES (?,?)", key, new ConfigData(value).toString()));
    }

    void insertAll(MySQLDatabase db);

    void setup(String key, MySQLDatabase db);

    default void update(MySQLDatabase db, String key, Object value) {
        if (value instanceof AutoUpdatable)
            ((AutoUpdatable) value).updateAll(db);
        else
            async(() -> db.command("UPDATE `" + db.table + "` WHERE `key`=? SET `value`=?", key, value));
    }

    void updateAll(MySQLDatabase db);
}
