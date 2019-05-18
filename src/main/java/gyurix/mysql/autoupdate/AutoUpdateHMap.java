package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;
import lombok.Getter;

import java.util.HashMap;

@Getter
public class AutoUpdateHMap<K, V> extends HashMap<K, V> implements AutoLoadableMap<K, V> {
  private MySQLDatabase db;
  private String key;

  @Override
  public V put(K key, V value) {
    return processPut(key, value);
  }

  @Override
  public V remove(Object key) {
    return processRemove(key);
  }

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
  public void setup(String key, MySQLDatabase db) {
    System.out.println("AutoUpdateHMap - setup - " + key);
    this.key = key;
    this.db = db;
  }

  @Override
  public void setupForInsertion(String key, MySQLDatabase db) {
    System.out.println("AutoUpdateHMap - setupForInsertion - " + key);
    this.key = key;
    this.db = db;
    insertAll();
  }
}