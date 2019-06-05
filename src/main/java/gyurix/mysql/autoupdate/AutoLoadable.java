package gyurix.mysql.autoupdate;

import java.lang.reflect.Type;

public interface AutoLoadable extends AutoUpdatable {
  void load(Class type, Type[] types);
}
