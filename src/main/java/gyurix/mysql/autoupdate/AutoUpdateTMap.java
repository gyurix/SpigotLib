package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;
import lombok.Getter;

import java.util.TreeMap;

@Getter
public class AutoUpdateTMap<K, V> extends TreeMap<K, V> implements AutoLoadableMap<K, V> {
  private MySQLDatabase db;
  private String key;

  @Override
  public void clear() {
    super.clear();
    deleteAll();
  }

  @Override
  public V putNow(K key, V value) {
    return super.put(key, value);
  }

  @Override
  public V removeNow(Object key) {
    return super.remove(key);
  }

  @Override
  public void setupForInsertion(String key, MySQLDatabase db) {
    this.key = key;
    this.db = db;
    insertAll();
  }

  @Override
  public void setup(String key, MySQLDatabase db) {
    this.key = key;
    this.db = db;
  }
}
