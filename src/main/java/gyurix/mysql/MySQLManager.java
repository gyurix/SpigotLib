package gyurix.mysql;

import gyurix.configfile.ConfigData;
import gyurix.protocol.Reflection;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.function.Consumer;

import static gyurix.spigotutils.SafeTools.async;

public class MySQLManager {
    private MySQLDatabase db;

    public MySQLManager(MySQLDatabase db) {
        this.db = db;
    }

    public void delete(String key) {
        async(() ->
                db.command("DELETE FROM `" + db.table + "` WHERE " +
                        "`key`=\"" + MySQLDatabase.escape(key) + "\""));
    }

    public void deleteAll(String key) {
        async(() ->
                db.command("DELETE FROM `" + db.table + "` WHERE " +
                        "`key` LIKE \"" + MySQLDatabase.escape(key) + "%\""));
    }

    public void insert(String key, Object value) {
        async(() -> {
            String k = MySQLDatabase.escape(key);
            String val = MySQLDatabase.escape(new ConfigData(value).toString());
            int done = db.update("UPDATE `" + db.table + "` " +
                    "SET `value`=\"" + val + "\" " +
                    "WHERE `key`=\"" + k + "\"");
            if (done == 0)
                db.command("INSERT INTO `" + db.table + "` (`key`,`value`) VALUES " +
                        "(\"" + MySQLDatabase.escape(k) + "\", \"" + val + "\")");
        });
    }

    private void loadField(String parentKey, Object parent, Field field) {

    }

    private <T> T loadObject(String key, Class<T> cl) {
        Object obj = Reflection.newInstance(cl);
        return (T) obj;
    }

    public void with(String key, Consumer<ResultSet> con) {
        async(() -> {
          ResultSet rs = db.query("SELECT * FROM `" + db.table + "` WHERE `key` LIKE \"" + MySQLDatabase.escape(key) + ".%\"");
            rs.close();
        });
    }
}
