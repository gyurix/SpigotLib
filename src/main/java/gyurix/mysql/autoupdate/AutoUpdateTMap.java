package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;

import java.util.TreeMap;

public class AutoUpdateTMap<K, V> extends TreeMap<K, V> implements AutoUpdatable {
    private MySQLDatabase db;
    private String key;

    @Override
    public void deleteAll(MySQLDatabase db) {
        forEach((k, v) -> delete(db, key + "." + k, v));
    }

    @Override
    public void insertAll(MySQLDatabase db) {
        forEach((k, v) -> insert(db, key + "." + k, v));
    }

    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
    }

    @Override
    public void updateAll(MySQLDatabase db) {
        forEach((k, v) -> update(db, key + "." + k, v));
    }

    @Override
    public V put(K key, V value) {
        V out = super.put(key, value);
        if (out != value) {
            if (out == null)
                insert(db, this.key + "." + key, value);
            else
                update(db, this.key + "." + key, value);
        }
        return out;
    }

    @Override
    public boolean remove(Object key, Object value) {
        boolean found = super.remove(key, value);
        if (found)
            delete(db, this.key + "." + key, value);
        return found;
    }
}
