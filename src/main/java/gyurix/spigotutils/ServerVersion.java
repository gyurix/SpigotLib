package gyurix.spigotutils;

/**
 * Created by GyuriX on 2016.03.06..
 */
public enum ServerVersion {
    UNKNOWN, v1_7, v1_8, v1_9, v1_10, v1_11, v1_12, v1_13;

    public boolean isAbove(ServerVersion version) {
        return compareTo(version) >= 0;
    }

    public boolean isBellow(ServerVersion version) {
        return compareTo(version) <= 0;
    }
}
