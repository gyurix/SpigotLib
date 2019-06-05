package gyurix.configfile;

import gyurix.mysql.MySQLDatabase;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static gyurix.spigotlib.SU.*;

/**
 * Data structure used for managing plugins configurations.
 */
public class ConfigFile {
  private static final String addressSplit = "\\.";
  private static final ExecutorService ioThread = Executors.newFixedThreadPool(1);
  /**
   * The core of this ConfigFile
   */
  public ConfigData data = new ConfigData();
  /**
   * The MySQLDatabase settings used for storing and loading the configuration from MySQL
   */
  public MySQLDatabase db;

  /**
   * The MySQLDatabase settings used for storing and loading the configuration from MySQL
   */
  public String dbTable, dbKey, dbValue, dbArgs;
  /**
   * The file used for storing and loading the configuration
   */
  public File file;

  /**
   * Constructs an empty ConfigFile
   */
  public ConfigFile() {
  }

  /**
   * Constructs an empty ConfigFile and loads it from the given InputStream
   *
   * @param stream - The InputStream containing the loadable configuration
   */
  public ConfigFile(InputStream stream) {
    load(stream);
  }

  /**
   * Constructs an empty ConfigFile and loads it from the given InputStream
   *
   * @param file - The File containing the loadable configuration
   */
  public ConfigFile(File file) {
    load(file);
  }

  /**
   * Constructs an empty ConfigFile and loads it from MySQL
   *
   * @param mysql - The MySQLDatabase containing the loadable configuration
   */
  public ConfigFile(MySQLDatabase mysql) {
    db = mysql;
    dbTable = mysql.table;
    dbKey = "key";
    dbValue = "value";
  }

  /**
   * Constructs an empty ConfigFile and loads it from MySQL
   *
   * @param mysql - The MySQLDatabase containing the loadable configuration
   * @param table - The table which contains the configuration, usually <b>mysql.table</b>
   * @param key   - The columns name which should be used for storing configurations addresses, usually <b>"key"</b>
   * @param value - The columns name which should be used for storing configurations values, usually <b>"value"</b>
   */
  public ConfigFile(MySQLDatabase mysql, String table, String key, String value) {
    db = mysql;
    dbTable = table;
    dbKey = key;
    dbValue = value;
  }

  /**
   * Constructs an empty ConfigFile and loads it from the given String
   *
   * @param in - The String containing the loadable configuration
   */
  public ConfigFile(String in) {
    load(in);
  }

  /**
   * Wrap an already existing ConfigData
   *
   * @param d - The wrapable ConfigData
   */
  public ConfigFile(ConfigData d) {
    data = d;
  }

  /**
   * Get the given typed Object from the ConfigFiles given address. If the given address does not exist
   * then it will be created automatically
   *
   * @param adress - The address
   * @param cl     - The requested Objects type
   * @param <T>    - The requested Objects type
   * @return The requested Object
   */
  public <T> T get(String adress, Class<T> cl) {
    return getData(adress, true).deserialize(cl);
  }

  /**
   * Get the given typed Object from the ConfigFiles given address. If the given address does not exist
   * then it will be created automatically
   *
   * @param adress - The address
   * @param cl     - The requested Objects type
   * @param types  - The parameter types of the requested Object
   * @param <T>    - The requested Objects type
   * @return The requested Object
   */
  public <T> T get(String adress, Class<T> cl, Type... types) {
    return getData(adress, true).deserialize(cl, types);
  }

  /**
   * Get a boolean variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested boolean variable or def it it was not found
   */
  public boolean getBoolean(String address, boolean def) {
    return getObject(address, Boolean.class, def);
  }

  /**
   * Get a boolean variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested byte variable or 0 if it was not found
   */
  public boolean getBoolean(String address) {
    return getObject(address, Boolean.class, false);
  }

  /**
   * Get a byte variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested byte variable or def it it was not found
   */
  public byte getByte(String address, byte def) {
    return getObject(address, Byte.class, def);
  }

  /**
   * Get a byte variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested byte variable or 0 if it was not found
   */
  public byte getByte(String address) {
    return getObject(address, Byte.class, (byte) 0);
  }

  /**
   * Get data found in the given address
   *
   * @param address - Requested address
   * @return The data found on the given address or empty ConfigData, if it can
   * not be found.
   */
  public ConfigData getData(String address) {
    String[] parts = address.split(addressSplit);
    ConfigData d = data;
    for (String p : parts) {
      if (p.matches("#\\d+")) {
        int num = Integer.valueOf(p.substring(1));
        if (d.listData == null || d.listData.size() <= num)
          return new ConfigData("");
        d = d.listData.get(num);
      } else {
        ConfigData key = new ConfigData(p);
        if (d.mapData == null)
          return new ConfigData("");
        if (d.mapData.containsKey(key)) {
          d = d.mapData.get(key);
        } else {
          return new ConfigData("");
        }
      }
    }
    return d;
  }

  /**
   * Get data found in the given address.
   *
   * @param address    - Requested address
   * @param autoCreate - Automatically create the road for accessing the given address
   * @return The data found on the given address or empty ConfigData, if it can
   * not be found.
   */
  public ConfigData getData(String address, boolean autoCreate) {
    if (!autoCreate)
      return getData(address);
    String[] parts = address.split(addressSplit);
    ConfigData d = data;
    for (String p : parts) {
      if (p.matches("#\\d+")) {
        int num = Integer.valueOf(p.substring(1));
        if (d.listData == null) {
          d.listData = new ArrayList<>();
        }
        while (d.listData.size() <= num) {
          d.listData.add(new ConfigData(""));
        }
        d = d.listData.get(num);
      } else {
        ConfigData key = new ConfigData(p);
        if (d.mapData == null)
          d.mapData = new LinkedHashMap<>();
        if (d.mapData.containsKey(key)) {
          d = d.mapData.get(key);
        } else {
          d.mapData.put(key, d = new ConfigData(""));
        }
      }
    }
    return d;
  }


  /**
   * Get a double variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested double variable or def it it was not found
   */
  public double getDouble(String address, double def) {
    return getObject(address, Double.class, def);
  }

  /**
   * Get a double variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested double variable or 0 if it was not found
   */
  public double getDouble(String address) {
    return getObject(address, Double.class, 0D);
  }

  /**
   * Get a float variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested float variable or def it it was not found
   */
  public float getFloat(String address, float def) {
    return getObject(address, Float.class, def);
  }

  /**
   * Get a float variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested float variable or 0 if it was not found
   */
  public float getFloat(String address) {
    return getObject(address, Float.class, 0f);
  }

  /**
   * Get an int variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested int variable or def it it was not found
   */
  public int getInt(String address, int def) {
    return getObject(address, Integer.class, def);
  }

  /**
   * Get an int variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested int variable or 0 if it was not found
   */
  public int getInt(String address) {
    return getObject(address, Integer.class, 0);
  }

  /**
   * Get a long variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested long variable or def it it was not found
   */
  public long getLong(String address, long def) {
    return getObject(address, Long.class, def);
  }

  /**
   * Get a long variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested long variable or 0 if it was not found
   */
  public long getLong(String address) {
    return getObject(address, Long.class, 0L);
  }

  /**
   * Get the given typed Object from the ConfigFile
   *
   * @param adress - The address of the Object
   * @param cl     - The objects type
   * @param types  - Object type parameters
   * @param <T>    - The type of the Object
   * @return The requested Object or null if it was not found.
   */
  public <T> T getObject(String adress, Class<T> cl, Type... types) {
    return getObject(adress, cl, null, types);
  }

  /**
   * Get the given typed Object from the ConfigFile
   *
   * @param adress - The address of the Object
   * @param cl     - The objects type
   * @param def    - Default return Object used, when Object can not be found under the given key
   * @param types  - Object type parameters
   * @param <T>    - The type of the Object
   * @return The requested Object or def if it was not found.
   */
  public <T> T getObject(String adress, Class<T> cl, T def, Type... types) {
    ConfigData cd = getData(adress);
    return cd.isEmpty() ? def : cd.deserialize(cl, types);
  }

  /**
   * Get a short variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested short variable or def it it was not found
   */
  public short getShort(String address, short def) {
    return getObject(address, Short.class, def);
  }

  /**
   * Get a short variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested short variable or 0 if it was not found
   */
  public short getShort(String address) {
    return getObject(address, Short.class, (short) 0);
  }

  /**
   * Get a String variable from the configuration
   *
   * @param address - The address used for storing the value
   * @param def     - Default value which should be returned if the address does not exist
   * @return The requested String variable or def it it was not found
   */
  public String getString(String address, String def) {
    return getObject(address, String.class, def);
  }

  /**
   * Get a String variable from the configuration
   *
   * @param address - The address used for storing the value
   * @return The requested String variable or empty string if it was not found
   */
  public String getString(String address) {
    return getObject(address, String.class, "");
  }

  /**
   * Get all the main sub keys of this configuration
   *
   * @return The list of main sub keys of this configuration
   */
  public ArrayList<String> getStringKeyList() {
    ArrayList<String> out = new ArrayList<>();
    try {
      for (ConfigData cd : data.mapData.keySet())
        out.add(cd.stringData);
    } catch (Throwable ignored) {
    }
    return out;
  }

  /**
   * Get all the main sub keys of this configuration which are under the root key
   *
   * @param key - Root key
   * @return The list of main sub keys under the root key
   */
  public ArrayList<String> getStringKeyList(String key) {
    ArrayList<String> out = new ArrayList<>();
    try {
      for (ConfigData cd : getData(key).mapData.keySet()) {
        out.add(cd.stringData);
      }
    } catch (Throwable ignored) {
    }
    return out;
  }

  /**
   * Load configuration from an InputStream
   *
   * @param is - The loadable InputStream
   * @return The success of the load
   */
  public boolean load(InputStream is) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      for (int len = is.read(buf); len > 0; len = is.read(buf))
        bos.write(buf, 0, len);
      is.close();
      return load(new String(bos.toByteArray(), utf8));
    } catch (Throwable e) {
      error(cs, e, "SpigotLib", "gyurix");
    }
    return false;
  }

  /**
   * Load configuration from a String
   *
   * @param in - The loadable String
   * @return The success of the load
   */
  public boolean load(String in) {
    in = in.replaceAll("&([0-9a-fk-or])", "§$1");
    ArrayList<ConfigReader> readers = new ArrayList<>();
    readers.add(new ConfigReader(-1, data));
    for (String s : in.split("\r?\n")) {
      int blockLvl = 0;
      while (s.length() > blockLvl && s.charAt(blockLvl) == ' ')
        blockLvl++;
      s = s.substring(blockLvl);
      int id = readers.size() - 1;
      if (!s.isEmpty()) {
        if (s.startsWith("#")) {
          readers.get(id).addComment(s.substring(1));
        } else {
          while (readers.get(id).blockLvl > blockLvl) {
            readers.remove(id);
            id--;
          }
          readers.get(id).handleInput(readers, s, blockLvl);
        }
      }
    }
    return true;
  }

  /**
   * Load configuration from a File
   *
   * @param f - The loadable File
   * @return The success of the load
   */
  public boolean load(File f) {
    try {
      file = f;
      f.createNewFile();
      byte[] b = Files.readAllBytes(f.toPath());
      load(new String(b, utf8));
      return true;
    } catch (Throwable e) {
      error(cs, e, "SpigotLib", "gyurix");
    }
    return false;
  }

  /**
   * Load the given address of this configuration from MySQL
   *
   * @param address - The loadable address
   * @param args    - Arguments of the MySQL WHERE clause
   */
  public void mysqlLoad(String address, String args) {
    String q = "SELECT `" + dbKey + "`, `" + dbValue + "` FROM " + dbTable + " WHERE " + args;
    try {
      ResultSet rs = db.query(q);
      while (rs.next()) {
        String k = rs.getString(dbKey);
        String v = rs.getString(dbValue);
        setData(address + "." + k, new ConfigFile(v).data);
      }
    } catch (Throwable e) {
      cs.sendMessage("§cFailed to load data from MySQL storage.\n" +
              "The used query command:\n");
      error(cs, e, "SpigotLib", "gyurix");
      e.printStackTrace();
    }
  }

  /**
   * Load the configuration from MySQL
   */
  public void mysqlLoad() {
    String q = "SELECT `" + dbKey + "`, `" + dbValue + "` FROM " + dbTable;
    try {
      if (db == null || !db.openConnection())
        return;
      ResultSet rs = db.query(q);
      while (rs.next()) {
        String k = rs.getString(dbKey);
        String v = rs.getString(dbValue);
        setData(k, new ConfigFile(v).data);
      }
    } catch (Throwable e) {
      cs.sendMessage("§cFailed to load data from MySQL storage.\n" +
              "The used query command:\n");
      error(cs, e, "SpigotLib", "gyurix");
      e.printStackTrace();
    }
  }

  /**
   * Add to the given list the MySQL commands required for updating the storage based on this Configuration
   *
   * @param l    - The list
   * @param args - The arguments which should be passed after MySQL WHERE clause.
   */
  public void mysqlUpdate(ArrayList<String> l, String args) {
    if (dbTable == null)
      return;
    l.add("DELETE FROM " + dbTable + (dbArgs == null ? "" : (" WHERE " + dbArgs)));
    if (args == null)
      args = dbArgs == null ? "'<key>','<value>'" : dbArgs.substring(dbArgs.indexOf('=') + 1) + ",'<key>','<value>'";
    data.saveToMySQL(l, dbTable, args, "");
  }


  /**
   * Reloads the configuration from the file used in initialization
   *
   * @return The success of reload
   */
  public boolean reload() {
    if (file == null) {
      SU.cs.sendMessage("§cError on reloading ConfigFile, missing file data.");
      return false;
    }
    data = new ConfigData();
    return load(file);
  }

  /**
   * Remove the given address from the configuration
   *
   * @param address - The removable address
   * @return True if the data was removed, false otherwise
   */
  public boolean removeData(String address) {
    String[] allParts = address.split(addressSplit);
    int len = allParts.length - 1;
    String[] parts = (String[]) ArrayUtils.subarray(allParts, 0, len);
    ConfigData d = data;
    for (String p : parts) {
      if (p.matches("#\\d+")) {
        if (d.listData == null)
          return false;
        int num = Integer.valueOf(p.substring(1));
        if (d.listData.size() >= num)
          return false;
        d = d.listData.get(num);
      } else {
        ConfigData key = new ConfigData(p);
        if (d.mapData == null || !d.mapData.containsKey(key))
          return false;
        else
          d = d.mapData.get(key);
      }
    }
    if (allParts[len].matches("#\\d+")) {
      int id = Integer.valueOf(allParts[len].substring(1));
      return d.listData.remove(id) != null;
    }
    return d.mapData == null || d.mapData.remove(new ConfigData(allParts[len])) != null;
  }

  /**
   * Saves the configuration asynchronously.
   *
   * @return The success rate of the saving.
   */
  public boolean save() {
    if (db != null) {
      ArrayList<String> sl = new ArrayList<>();
      mysqlUpdate(sl, dbArgs);
      db.batch(sl, null);
      return true;
    } else if (file != null) {
      final String data = toString();
      ioThread.submit(() -> saveDataToFile(data));
      return true;
    }
    SU.cs.sendMessage("§cFailed to save ConfigFile: §eMissing file / valid MySQL data.");
    return false;
  }

  /**
   * Saves the configuration to the given OutputStream.
   *
   * @return The success rate of the saving.
   */
  public boolean save(OutputStream out) {
    try {
      byte[] data = toString().getBytes(utf8);
      out.write(data);
      out.flush();
      out.close();
      return true;
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
  }

  private void saveDataToFile(String data) {
    try {
      File tempf = new File(file + ".tmp");
      tempf.createNewFile();
      Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempf), utf8));
      if (data != null)
        w.write(data.replaceAll("§([0-9a-fk-or])", "&$1"));
      w.close();
      file.delete();
      tempf.renameTo(file);
    } catch (Throwable e) {
      error(cs, e, "SpigotLib", "gyurix");
    }
  }

  /**
   * Save the config file without using async thread.
   *
   * @return The sucess rate of the saving
   */
  public boolean saveNoAsync() {
    if (db != null) {
      ArrayList<String> sl = new ArrayList<>();
      mysqlUpdate(sl, dbArgs);
      db.batchNoAsync(sl);
      return true;
    } else if (file != null) {
      saveDataToFile(toString());
      return true;
    }
    System.err.println("Failed to save ConfigFile: Missing file / valid MySQL data.");
    return false;
  }

  /**
   * Serializes the ConfigData for being able to throw all the object references
   */
  public void serialize() {
    data = new ConfigFile(data.toString()).data;
  }

  /**
   * Set the given the given address to the given ConfigData
   *
   * @param address - The changable address
   * @param cd      - Target ConfigData
   */
  public void setData(String address, ConfigData cd) {
    String[] parts = address.split(addressSplit);
    ConfigData last = data;
    ConfigData lastKey = data;
    ConfigData d = data;
    for (String p : parts) {
      ConfigData key = new ConfigData(p);
      if (d.mapData == null)
        d.mapData = new LinkedHashMap<>();
      last = d;
      lastKey = key;
      if (d.mapData.containsKey(key)) {
        d = d.mapData.get(key);
      } else {
        d.mapData.put(key, d = new ConfigData(""));
      }
    }
    last.mapData.put(lastKey, cd);
  }

  /**
   * Set the given address to the given Object.
   *
   * @param address - The setable address
   * @param obj     - Target Object
   */
  public void setObject(String address, Object obj) {
    getData(address, true).objectData = obj;
  }

  /**
   * Set the given address to the given String.
   *
   * @param address - The setable address
   * @param value   - Target String
   */
  public void setString(String address, String value) {
    getData(address, true).stringData = value;
  }

  /**
   * Get the given sub section of this ConfigFile
   *
   * @param address - Sub sections address
   * @return The requested sub section of this ConfigFile
   */
  public ConfigFile subConfig(String address) {
    return new ConfigFile(getData(address, true));
  }

  /**
   * Get the given sub section of this ConfigFile referring to
   *
   * @param address - Sub sections address
   * @param dbArgs  - The MySQL arguments which should be used for saving this sub section
   * @return The requested sub section of this ConfigFile
   */
  public ConfigFile subConfig(String address, String dbArgs) {
    ConfigFile kf = new ConfigFile(getData(address, true));
    kf.db = db;
    kf.dbTable = dbTable;
    kf.dbKey = dbKey;
    kf.dbValue = dbValue;
    kf.dbArgs = dbArgs;
    return kf;
  }

  /**
   * Convert this ConfigFile to a String
   *
   * @return The same result as data.toString()
   */
  public String toString() {
    String str = data.toString();
    return str.startsWith("\n") ? str.substring(1) : str;
  }
}