package gyurix.commands.plugin;

import gyurix.spigotlib.SU;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static gyurix.spigotlib.Main.lang;
import static gyurix.spigotlib.SU.error;
import static gyurix.spigotlib.SU.sch;
import static java.util.Collections.EMPTY_LIST;

public class PluginCommands {
    private static final String[] emptyStringArray = new String[0];

    public PluginCommands(JavaPlugin pl, Object executor) {
        String pln = pl.getDescription().getName();
        HashMap<String, CommandMatcher> executors = new HashMap<>();
        SubOf subOfAnnotation = executor.getClass().getAnnotation(SubOf.class);
        String subOf = subOfAnnotation == null ? null : subOfAnnotation.value();
        for (Method m : executor.getClass().getMethods()) {
            String mn = m.getName();
            if (!mn.startsWith("cmd"))
                continue;
            String[] cmd = mn.substring(3).toLowerCase().split("_");
            CommandMatcher cm = executors.computeIfAbsent(cmd[0], (s) -> new CommandMatcher(pln, cmd[0]));
            if (cmd.length == 1) {
                cm.addMatcher(new CommandMatcher(pln, cmd[0], subOf, executor, m));
                continue;
            }
            CommandMatcher cur = cm;
            for (int i = 1; i < cmd.length; ++i) {
                cur = cur.getOrAddChild(pln, cmd[i]);
            }
            cur.addMatcher(new CommandMatcher(pln, cmd[cmd.length - 1], null, executor, m));
        }
        if (subOf == null) {
            executors.forEach((cmd, exec) -> {
                PluginCommand pcmd = pl.getCommand(cmd);
                if (pcmd == null) {
                    error(SU.cs, new Exception("Command " + cmd + " should be added to plugin.yml."), pln, pl.getDescription().getMain());
                    return;
                }
                ExtendedCommandExecutor ece = register(pl, cmd, exec);
                pcmd.setExecutor(ece);
                pcmd.setTabCompleter(ece);
            });
            return;
        }
        HashMap<String, ExtendedCommandExecutor> mapping = new HashMap<>();
        executors.forEach((cmd, exec) -> {
            ExtendedCommandExecutor ee = register(pl, cmd, exec);
            mapping.put(cmd, ee);
            for (String a : ee.getAliases())
                mapping.put(a, ee);
        });
        PluginCommand pc = pl.getCommand(subOf);
        pc.setExecutor((sender, command, s, args) -> {
            String sub = args.length == 0 ? "help" : args[0].toLowerCase();
            ExtendedCommandExecutor exec = mapping.get(sub);
            if (exec == null) {
                lang.msg("", sender, "command.wrongsub");
                return true;
            }
            String[] subArgs = args.length < 2 ? emptyStringArray : new String[args.length - 1];
            if (args.length > 1)
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            exec.onCommand(sender, command, s, subArgs);
            return true;
        });
        pc.setTabCompleter((sender, command, s, args) -> {
            ArrayList<String> out = new ArrayList<>();
            if (args.length == 1) {
                for (String sub : mapping.keySet()) {
                    if (sender.hasPermission(pln.toLowerCase() + ".command." + sub))
                        out.add(sub);
                }
                return SU.filterStart(out, args[0]);
            }
            String sub = args[0].toLowerCase();
            if (!sender.hasPermission(pln.toLowerCase() + ".command." + sub))
                return EMPTY_LIST;
            ExtendedCommandExecutor exec = mapping.get(sub);
            if (exec == null)
                return EMPTY_LIST;
            String[] subArgs = args.length < 2 ? emptyStringArray : new String[args.length - 1];
            if (args.length > 1)
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            return exec.onTabComplete(sender, command, s, subArgs);
        });
    }

    private ExtendedCommandExecutor register(JavaPlugin pl, String cmd, CommandMatcher m) {
        TreeSet<String> al = m.getAliases();
        String permission = pl.getName().toLowerCase() + ".command." + cmd;
        return new ExtendedCommandExecutor() {
            public void executeNow(CommandSender sender, String[] args) {
                try {
                    if (m.checkParameters(sender, args)) {
                        m.execute(sender, args);
                        return;
                    }
                    lang.msg("", sender, "command.usage");
                    for (String s : m.getUsage(sender, args))
                        sender.sendMessage(s);
                } catch (Throwable e) {
                    error(sender, e, "SpigotLib", "gyurix");
                }
            }

            @Override
            public TreeSet<String> getAliases() {
                return al;
            }

            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
                if (!sender.hasPermission(permission)) {
                    lang.msg("", sender, "command.noperm");
                    return true;
                }
                sch.runTaskAsynchronously(pl, () -> executeNow(sender, args));
                return true;
            }

            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
                return m.tabComplete(sender, args);
            }
        };
    }

    private interface ExtendedCommandExecutor extends CommandExecutor, TabCompleter {
        TreeSet<String> getAliases();
    }
}
