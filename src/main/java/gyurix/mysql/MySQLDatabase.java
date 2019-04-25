package gyurix.mysql;

import com.mysql.jdbc.Connection;
import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static gyurix.spigotlib.Main.pl;

/**
 * Class representing a MySQL storage connection and containing the required methods and utils
 * for executing MySQL queries
 */
public class MySQLDatabase {
  @ConfigOptions(serialize = false)
  private static ExecutorService batchThread = Executors.newSingleThreadExecutor();
  public String table;
  @ConfigOptions(serialize = false)
  private Connection con;
  private String database;
  private String host;
  private String password;
  private int timeout = 10000;
  private String username;

  public MySQLDatabase() {

  }

  /**
   * @param host     - The host of the MySQL server
   * @param database - The name of the database
   * @param username - The username to the MySQL server
   * @param password - The password to the MySQL server
   */
  public MySQLDatabase(String host, String database, String username, String password) {
    this.host = host;
    this.username = username;
    this.password = password;
    this.database = database;
    openConnection();
  }

  public static String escape(String in) {
    StringBuilder out = new StringBuilder();
    for (char c : in.toCharArray()) {
      switch (c) {
        case '\u0000':
          out.append("\\0");
          break;
        case '\u001a':
          out.append("\\Z");
          break;
        case '\n':
          out.append("\\n");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\'':
          out.append("\\'");
          break;
        case '"':
          out.append("\\\"");
          break;
        case '\\':
          out.append("\\\\");
          break;
        default:
          out.append(c);
      }
    }
    return out.toString();
  }

  public void batch(Iterable<String> commands) {
    batchThread.submit(new MySQLBatch(commands, null));
  }

  public void batch(Iterable<String> commands, Runnable r) {
    batchThread.submit(new MySQLBatch(commands, r));
  }

  public void batchNoAsync(Iterable<String> commands) {
    try {
      Statement st = getConnection().createStatement();
      for (String s : commands)
        st.addBatch(s);
      st.executeBatch();
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
  }

  public boolean command(String cmd) {
    PreparedStatement st;
    try {
      st = getConnection().prepareStatement(cmd);
      return st.execute();
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return false;
  }

  public boolean command(String cmd, Object... args) {
    try {
      return prepare(cmd, args).execute();
    } catch (Throwable e) {
      SU.log(pl, "MySQL - Command", cmd, StringUtils.join(args, ", "));
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return false;
  }

  /**
   * @return - The Connection
   */
  private Connection getConnection() {
    try {
      if (con == null || !con.isValid(timeout)) {
        openConnection();
      }
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return con;
  }

  /**
   * @return True on successful connection otherwise false
   */
  public boolean openConnection() {
    try {
      con = (Connection) DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true&useSSL=" + Config.mysqlSSL, username, password);
      con.setAutoReconnect(true);
      con.setConnectTimeout(timeout);
    } catch (Throwable e) {
      SU.cs.sendMessage("§cFailed to connect to storage, please check the plugins configuration.");
      return false;
    }
    return true;
  }

  private PreparedStatement prepare(String cmd, Object... args) throws Throwable {
    PreparedStatement st = getConnection().prepareStatement(cmd);
    for (int i = 0; i < args.length; ++i)
      st.setObject(i + 1, args[i] instanceof Enum ? ((Enum) args[i]).name() :
              String.valueOf(args[i]));
    return st;
  }

  public ResultSet query(String cmd) {
    ResultSet rs;
    PreparedStatement st;
    try {
      st = getConnection().prepareStatement(cmd);
      rs = st.executeQuery();
      return rs;
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  public ResultSet query(String cmd, Object... args) {
    try {
      return prepare(cmd, args).executeQuery();
    } catch (Throwable e) {
      SU.log(pl, "MySQL - Query", cmd, StringUtils.join(args, ", "));
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  public int update(String cmd) {
    PreparedStatement st;
    try {
      st = getConnection().prepareStatement(cmd);
      return st.executeUpdate();
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return -1;
    }
  }

  public int update(String cmd, Object... args) {
    try {
      return prepare(cmd, args).executeUpdate();
    } catch (Throwable e) {
      SU.log(pl, "MySQL - Update", cmd, StringUtils.join(args, ", "));
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return -1;
    }
  }


  public class MySQLBatch implements Runnable {
    private final Iterable<String> ps;
    private final Runnable r;

    public MySQLBatch(Iterable<String> cmds, Runnable r) {
      ps = cmds;
      this.r = r;
    }

    @Override
    public void run() {
      try {
        Statement st = getConnection().createStatement();
        for (String s : ps) {
          st.addBatch(s);
        }
        st.executeBatch();
        if (r != null)
          r.run();

      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    }
  }
}

