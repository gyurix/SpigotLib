package gyurix.commands.plugin;

import java.lang.reflect.Type;

public interface CustomMatcher {
    Object convert(String arg, Type type);
}
