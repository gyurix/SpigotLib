package gyurix.configfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class ConfigReader {
    final int blockLvl;
    final ConfigData key;
    final ConfigData value;
    private boolean keyRead, noList;

    ConfigReader(int lvl, ConfigData data) {
        blockLvl = lvl;
        value = data;
        key = null;
    }

    ConfigReader(int lvl, ConfigData key, ConfigData value) {
        blockLvl = lvl;
        this.key = key;
        this.value = value;
    }

    public ConfigReader(int lvl, ConfigData value, boolean noList) {
        blockLvl = lvl;
        this.value = value;
        key = null;
        this.noList = noList;
    }

    public void addComment(String com) {
        ConfigData data = getData();
        if (data.comment == null) {
            data.comment = com;
        } else {
            data.comment += '\n' + com;
        }
    }

    public ConfigData getData() {
        return keyRead ? key : value;
    }

    public void handleInput(ArrayList<ConfigReader> readers, String line, int lvl) {
        ConfigData data = getData();
        if (!noList && line.equals("-") || line.equals(">") || line.indexOf(':') == line.length() - 1)
            line += ' ';
        if (!noList && line.startsWith("- ")) {
            if (data.listData == null)
                data.listData = new ArrayList();
            ConfigData d = new ConfigData();
            data.listData.add(d);
            ConfigReader r = new ConfigReader(lvl + 1, d, true);
            readers.add(r);
            r.handleInput(readers, line.substring(2), lvl + 2);
        } else if (line.startsWith("> ")) {
            ConfigData key = new ConfigData(ConfigData.unescape(line.substring(2)));
            ConfigData value = new ConfigData();
            ConfigReader reader = new ConfigReader(lvl, key, value);
            reader.keyRead = true;
            if (lvl == blockLvl) {
                int size = readers.size() - 2;
                readers.remove(size + 1);
                readers.get(size).getData().mapData.put(key, value);
            } else {
                if (data.mapData == null)
                    data.mapData = new LinkedHashMap();
                data.mapData.put(key, value);
            }
            readers.add(reader);
        } else if (keyRead && line.startsWith(": ")) {
            keyRead = false;
            value.stringData = ConfigData.unescape(line.substring(2));
        } else {
            String[] s = line.startsWith(": ") ? new String[]{line} : line.split(" *: +", 2);
            if (s.length == 2) {
                ConfigData key;
                if (keyRead) {
                    this.key.stringData += ConfigData.unescape('\n' + s[0]);
                    key = this.key;
                } else {
                    key = new ConfigData(ConfigData.unescape(s[0]));
                }
                ConfigData value = new ConfigData(ConfigData.unescape(s[1]));
                ConfigReader reader = new ConfigReader(lvl, key, value);
                if (lvl == blockLvl) {
                    int size = readers.size() - 2;
                    readers.remove(size + 1);
                    data = readers.get(size).getData();
                    if (data.mapData == null)
                        data.mapData = new LinkedHashMap();
                    data.mapData.put(key, value);
                } else {
                    if (data.mapData == null)
                        data.mapData = new LinkedHashMap();
                    data.mapData.put(key, value);
                }
                readers.add(reader);

            } else if (data.stringData == null) {
                data.stringData = ConfigData.unescape(line);
            } else {
                data.stringData += ConfigData.unescape('\n' + line);
            }
        }
    }
}