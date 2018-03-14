package gyurix.configfile;

import java.lang.reflect.Type;

/**
 * ValueClassSelector is a special ability for keys for being able to choose the type of their values. The most common
 * usage of this interface is in EnumMaps.
 */
public interface ValueClassSelector {
    Class getValueClass();

    Type[] getValueTypes();
}
