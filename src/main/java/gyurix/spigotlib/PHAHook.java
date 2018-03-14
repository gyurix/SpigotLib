package gyurix.spigotlib;

import gyurix.api.VariableAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI %sl_% placeholder hook, used for making plugins using PlaceholderAPI instead of SpigotLib's
 * PlaceholderAPI compatible with SpigotLib PlaceholderAPI placeholders.
 */
public class PHAHook extends PlaceholderHook {
    public PHAHook() {
        PlaceholderAPI.registerPlaceholderHook("sl", this);
    }

    @Override
    public String onPlaceholderRequest(Player plr, String msg) {
        return VariableAPI.fillVariables(msg, plr);
    }
}
