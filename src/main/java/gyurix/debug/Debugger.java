package gyurix.debug;

import gyurix.spigotlib.SU;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;

import static gyurix.configfile.ConfigSerialization.ConfigOptions;
import static gyurix.spigotlib.Main.lang;

@Data
public class Debugger {
    private HashSet<String> availableChannels = new HashSet<>();
    private HashSet<String> channels;
    private HashSet<String> enabledChannels = new HashSet<>();
    @ConfigOptions(serialize = false)
    private Plugin plugin;

    public Debugger() {

    }

    public Debugger(Plugin pl) {
        this.plugin = pl;
    }

    public void disableChannel(String channel) {
        enabledChannels.remove(channel);
    }

    public void enableChannel(String channel) {
        enabledChannels.add(channel);
    }

    public String getPluginName() {
        return plugin == null ? "Test" : plugin.getName();
    }

    public void handleCommand(CommandSender sender, String cmd, String[] args, int startingArg) {
        StringBuilder sb = new StringBuilder(cmd);
        for (int i = 0; i < startingArg; ++i)
            sb.append(' ').append(args[i]);
        cmd = sb.toString();
        Player plr = sender instanceof Player ? (Player) sender : null;
        if (args.length == startingArg) {
            String on = lang.get(plr, "debug.on");
            String off = lang.get(plr, "debug.off");
            String sep = lang.get(plr, "debug.sep");
            lang.msg(sender, "debug.list", "plugin", getPluginName(), "list", sb.length() == 0 ? "" : sb.substring(sep.length()));
        }
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
