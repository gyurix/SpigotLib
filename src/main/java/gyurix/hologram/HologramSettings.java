package gyurix.hologram;

import lombok.Getter;

@Getter
public class HologramSettings {
  LineAlign align = LineAlign.DOWN;
  double dist = 50;
  double lineDist = 0.225;
  int update = 100;

  public HologramSettings clone() {
    HologramSettings settings = new HologramSettings();
    settings.dist = dist;
    settings.lineDist = lineDist;
    settings.align = align;
    return settings;
  }
}
