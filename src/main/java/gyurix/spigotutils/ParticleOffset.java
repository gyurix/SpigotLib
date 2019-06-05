package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization;
import lombok.Data;

@Data
public class ParticleOffset implements ConfigSerialization.StringSerializable {
  private float x, y, z;

  public ParticleOffset(String in) {
    String[] d = in.split(" ", 3);
    x = Float.valueOf(d[0]);
    y = Float.valueOf(d[1]);
    z = Float.valueOf(d[2]);
  }

  @Override
  public String toString() {
    return x + " " + y + " " + z;
  }
}
