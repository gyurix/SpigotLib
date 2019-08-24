package gyurix.sign;

import gyurix.spigotlib.SU;
import lombok.Data;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

@Data
public class SignConfig {
  public ArrayList<String> lines = new ArrayList<>();

  public String[] getLinesArray(Object... vars) {
    String[] out = new String[4];
    for (int i = 0; i < 4; i++)
      out[i] = SU.fillVariables(lines.get(i), vars);
    return out;
  }

  public String[] getLinesArray(Map<String, String> vars) {
    String[] out = new String[4];
    for (int i = 0; i < 4; i++)
      out[i] = SU.fillVariables(lines.get(i), vars);
    return out;
  }

  public TreeMap<String, String> getPlaceholders(String[] sign) {
    TreeMap<String, String> out = new TreeMap<>();
    for (int i = 0; i < 4; ++i) {
      TreeMap<String, String> map = SU.getPlaceholders(lines.get(i), sign[i]);
      if (map == null)
        return null;
      out.putAll(map);
    }
    return out;
  }

  public boolean matches(String[] sign) {
    return getPlaceholders(sign) != null;
  }

  public boolean set(Sign s, Object... vars) {
    try {
      String[] newLines = new String[4];
      for (int i = 0; i < 4; i++)
        newLines[i] = SU.fillVariables(lines.get(i), vars);
      PluginSignChangeEvent e = new PluginSignChangeEvent(s.getBlock(), newLines);
      SU.pm.callEvent(e);
      if (e.isCancelled())
        return false;
      for (int i = 0; i < 4; i++)
        s.setLine(i, newLines[i]);
      s.update(true, false);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  public boolean setNoEvent(Sign s, Object... vars) {
    try {
      for (int i = 0; i < 4; i++)
        s.setLine(i, SU.fillVariables(lines.get(i), vars));
      s.update(true, false);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "- " + lines.get(0) + "\n- " + lines.get(1) + "\n- " + lines.get(2) + "\n- " + lines.get(3);
  }
}
