package gyurix.spigotutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DualMap<K, V> implements Map<K, V> {
  final HashMap<K, V> keys = new HashMap<>();
  final HashMap<V, K> values = new HashMap<>();

  public K getKey(V value) {
    return values.get(value);
  }

  private void putAllValue(Map<K, V> m) {
    for (Entry<K, V> e : m.entrySet()) {
      values.put(e.getValue(), e.getKey());
    }
  }

  public K removeValue(V value) {
    K key = values.remove(value);
    keys.remove(key);
    return key;
  }

  public int size() {
    return keys.size();
  }

  public boolean isEmpty() {
    return keys.isEmpty();
  }

  public boolean containsKey(Object key) {
    return keys.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return values.containsKey((V) value);
  }

  public V get(Object key) {
    return keys.get(key);
  }

  public V put(K key, V value) {
    keys.remove(values.get(value));
    V o = keys.put(key, value);
    values.put(value, key);
    return o;
  }

  public V remove(Object key) {
    V o = keys.remove(key);
    values.remove(o);
    return o;
  }

  public void putAll(Map m) {
    keys.putAll(m);
    putAllValue(m);
  }

  public void clear() {
    keys.clear();
    values.clear();
  }

  public Set<K> keySet() {
    return keys.keySet();
  }

  public Collection<V> values() {
    return values.keySet();
  }

  public Set<Entry<K, V>> entrySet() {
    return keys.entrySet();
  }
}