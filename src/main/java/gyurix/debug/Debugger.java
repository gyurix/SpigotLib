package gyurix.debug;

import gyurix.spigotlib.SU;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;

import static gyurix.configfile.ConfigSerialization.ConfigOptions;
import static gyurix.spigotlib.Main.lang;

@Data
public class Debugger {
    private HashSet<String> channels;
    private HashSet<String> enabledChannels = new HashSet<>();
    @ConfigOptions(serialize = false)
    private Plugin plugin;

    public Debugger() {

    }

    public Debugger(Plugin pl) {
        this.plugin = pl;
    }

    public String getPluginName() {
        return plugin == null ? "Test" : plugin.getName();
    }

    public void handleCommand(CommandSender sender, String cmd, String[] args) {
        if (args.length == 0) {
            lang.msg(sender, "debug.list", "channels", StringUtils.join(enabledChannels, ", "));
            return;
        }
        args[0] = Character.toUpperCase(args[0].charAt(0)) + args[0].substring(1).toLowerCase();
        if (enabledChannels.remove(args[0])) {
            lang.msg(sender, "debug.disabled", "channel", args[0]);
            return;
        }
        enabledChannels.add(args[0]);
        lang.msg(sender, "debug.enabled", "channel", args[0]);
    }

    public boolean isEnabled(String channel) {
        return enabledChannels.contains(channel);
    }

    public void msg(String channel, Throwable e) {
        if (enabledChannels.contains(channel))
            SU.error(SU.cs, e, getPluginName() + " - " + channel, plugin.getDescription().getMain().replaceFirst("\\..*", ""));
    }

    public void msg(String channel, Object msg) {
        if (enabledChannels.contains(channel))
            SU.cs.sendMessage("§a[" + getPluginName() + " - DEBUG - " + channel + "]§f " + msg);
    }
}
