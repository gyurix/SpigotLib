package gyurix.mysql.autoupdate;

import gyurix.configfile.ConfigData;
import gyurix.mysql.MySQLDatabase;

import static gyurix.spigotutils.SafeTools.async;

public interface AutoUpdatable {
  default void delete(String key, Object value) {
    System.out.println("AutoUpdatable - delete - " + key);
    MySQLDatabase db = getDb();
    if (value instanceof AutoUpdatable)
      ((AutoUpdatable) value).deleteAll();
    async(() -> {
      db.command("DELETE FROM `" + db.table + "` WHERE `key` = ?", key);
    });
  }

  void deleteAll();

  MySQLDatabase getDb();

  String getKey();

  default void insert(String key, Object value) {
    MySQLDatabase db = getDb();
    if (!(value instanceof AutoUpdatable))
      async(() -> db.command("INSERT INTO `" + db.table + "` VALUES (?,?)", key, new ConfigData(value).toString()));
  }

  void insertAll();

  void setup(String key, MySQLDatabase db);

  void setupForInsertion(String key, MySQLDatabase db);

  default void update(String key, Object value) {
    System.out.println("AutoUpdatable - update - " + key + " - " + value);
    MySQLDatabase db = getDb();
    if (value instanceof AutoUpdatable)
      ((AutoUpdatable) value).updateAll();
    else
      async(() -> db.command("UPDATE `" + db.table + "` SET `value`=? WHERE `key`=?", value, key));
  }

  void updateAll();
}
