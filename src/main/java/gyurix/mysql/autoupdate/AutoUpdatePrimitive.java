package gyurix.mysql.autoupdate;

import gyurix.mysql.MySQLDatabase;
import lombok.Getter;

public class AutoUpdatePrimitive<T> implements AutoUpdatable {
    private MySQLDatabase db;
    private String key;
    @Getter
    private T value;

    @Override
    public void deleteAll(MySQLDatabase db) {
        delete(db, key, value);
    }

    @Override
    public void insertAll(MySQLDatabase db) {
        insert(db, key, value);
    }

    @Override
    public void setup(String key, MySQLDatabase db) {
        this.key = key;
        this.db = db;
    }

    @Override
    public void updateAll(MySQLDatabase db) {
        update(db, key, value);
    }

    public void init(T value) {
        this.value = value;
        insert(db, key, this.value);
    }

    public void setValue(T value) {
        this.value = value;
        update(db, key, value);
    }
}
