package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;
import lombok.Getter;

import java.util.TreeMap;

@Getter
public class AutoUpdateTMap<K, V> extends TreeMap<K, V> implements AutoLoadableMap<K, V> {
    private String key;
    private MySQLDatabase db;


    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
    }

    @Override
    public V putNow(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public V removeNow(Object key) {
        return super.remove(key);
    }
}
