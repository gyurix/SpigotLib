package gyurix.spigotutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Created by GyuriX on 2015.12.29..
 */
public class Orderable<K, V extends Comparable> implements Comparable<Orderable<K, V>> {
    public final K key;
    public final V value;

    public Orderable(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V extends Comparable> HashMap<K, Integer> makeMap(ArrayList<Orderable<K, V>> topList) {
        HashMap<K, Integer> map = new HashMap<>();
        int id = 1;
        for (Orderable<K, V> o : topList) {
            map.put(o.key, id++);
        }
        return map;
    }

    public static <K, V extends Comparable> TreeSet<Orderable<K, V>> order(Map<K, V> data) {
        TreeSet<Orderable<K, V>> out = new TreeSet<>();
        for (Entry<K, V> e : data.entrySet()) {
            out.add(new Orderable(e.getKey(), e.getValue()));
        }
        return out;
    }

    @Override
    public int compareTo(Orderable<K, V> o) {
        if (value.compareTo(o.value) == 0)
            return key.toString().compareTo(o.key.toString());
        return 0 - value.compareTo(o.value);
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 100000 + value.hashCode();
    }

    @Override
    public String toString() {
        return key + " - " + value;
    }
}
