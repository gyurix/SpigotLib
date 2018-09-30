package gyurix.commands.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static gyurix.spigotlib.Main.lang;
import static gyurix.spigotlib.SU.error;
import static gyurix.spigotlib.SU.sch;

public class PluginCommands {
    private static final String[] emptyStringArray = new String[0];

    public PluginCommands(JavaPlugin pl, Object executor) {
        String pln = pl.getDescription().getName();
        HashMap<String, ArrayList<CommandMatcher>> executors = new HashMap<>();
        SubOf subOfAnnotation = executor.getClass().getAnnotation(SubOf.class);
        String subOf = subOfAnnotation == null ? null : subOfAnnotation.value();
        for (Method m : executor.getClass().getMethods()) {
            String mn = m.getName();
            if (!mn.startsWith("cmd"))
                continue;
            String cmd = mn.substring(3).toLowerCase();
            ArrayList<CommandMatcher> exc = executors.computeIfAbsent(cmd, (s) -> new ArrayList<>());
            exc.add(new CommandMatcher(pln, cmd, subOf, executor, m));
        }
        if (subOf == null) {
            executors.forEach((cmd, exec) -> pl.getCommand(cmd).setExecutor(register(pl, cmd, exec)));
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
    }

    private ExtendedCommandExecutor register(JavaPlugin pl, String cmd, ArrayList<CommandMatcher> matchers) {
        matchers.sort(Comparator.comparing(CommandMatcher::getParameterCount));
        String[] aliases = emptyStringArray;
        for (CommandMatcher m : matchers) {
            String[] a = m.getAliases();
            if (a != null) {
                aliases = a;
                break;
            }
        }
        String permission = pl.getName().toLowerCase() + ".command." + cmd;
        String[] finalAliases = aliases;
        boolean async = false;
        for (CommandMatcher m : matchers) {
            if (m.isAsync()) {
                async = true;
                break;
            }
        }
        boolean finalAsync = async;
        return new ExtendedCommandExecutor() {
            public void executeNow(CommandSender sender, String[] args) {
                try {
                    for (CommandMatcher m : matchers) {
                        if (!m.senderMatch(sender))
                            continue;
                        if (m.getParameterCount() != args.length)
                            continue;
                        int pars = m.getParameterCount();
                        boolean allParsValid = true;
                        for (int i = 0; i < pars; ++i) {
                            if (!m.isValidParameter(i, args[i])) {
                                allParsValid = false;
                                break;
                            }
                        }
                        if (allParsValid) {
                            m.execute(sender, args);
                            return;
                        }
                    }
                    for (CommandMatcher m : matchers) {
                        if (!m.senderMatch(sender))
                            continue;
                        if (m.getParameterCount() > args.length)
                            continue;
                        int pars = m.getParameterCount();
                        boolean allParsValid = true;
                        for (int i = 0; i < pars; ++i) {
                            if (!m.isValidParameter(i, args[i])) {
                                allParsValid = false;
                                break;
                            }
                        }
                        if (allParsValid) {
                            m.execute(sender, args);
                            return;
                        }
                    }
                    for (CommandMatcher m : matchers) {
                        if (!m.senderMatch(sender))
                            continue;
                        if (m.getParameterCount() != args.length)
                            continue;
                        m.execute(sender, args);
                        return;
                    }
                    for (CommandMatcher m : matchers) {
                        if (!m.senderMatch(sender))
                            continue;
                        if (m.getParameterCount() > args.length) {
                            lang.msg("", sender, "command.usage", "usage", m.getUsage(args));
                            return;
                        }
                    }
                    lang.msg("", sender, "command.noconsole");
                } catch (Throwable e) {
                    error(sender, e, "SpigotLib", "gyurix");
                }
            }

            @Override
            public String[] getAliases() {
                return finalAliases;
            }

            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
                if (!sender.hasPermission(permission)) {
                    lang.msg("", sender, "command.noperm");
                    return true;
                }
                if (finalAsync)
                    sch.runTaskAsynchronously(pl, () -> executeNow(sender, args));
                else
                    executeNow(sender, args);
                return true;
            }
        };
    }

    private interface ExtendedCommandExecutor extends CommandExecutor {
        String[] getAliases();
    }
}
