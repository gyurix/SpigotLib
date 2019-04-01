package gyurix.commands.plugin;

import gyurix.protocol.Primitives;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ItemUtils;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.*;
import java.util.*;

import static gyurix.spigotlib.Main.lang;
import static gyurix.spigotlib.Main.pl;
import static java.util.Collections.EMPTY_LIST;

public class CommandMatcher implements Comparable<CommandMatcher> {
    private static final String[] noSub = new String[0];
    private static HashMap<Class, CustomMatcher> customMatchers = new HashMap<>();
    private static HashMap<Class, Integer> parameterWeights = new HashMap<>();
    @Getter
    private TreeSet<String> aliases = new TreeSet<>();
    private boolean async;
    private TreeMap<String, CommandMatcher> children = new TreeMap<>();
    @Getter
    private String command;
    private Method executor;
    private Object executorOwner;
    private TreeSet<CommandMatcher> matchers = new TreeSet<>();
    private Parameter[] parameters;
    private String pluginName;
    private Class senderType;
    private String subOf;

    public CommandMatcher(String pluginName, String command) {
        this.pluginName = pluginName;
        this.command = command;
    }

    public CommandMatcher(String pluginName, String command, String subOf, Object executorOwner, Method executor) {
        this.pluginName = pluginName;
        this.command = command;
        this.subOf = subOf;
        this.executorOwner = executorOwner;
        this.executor = executor;
        Aliases al = executor.getAnnotation(Aliases.class);
        if (al != null)
            aliases.addAll(Arrays.asList(al.value()));
        Async async = executorOwner.getClass().getAnnotation(Async.class);
        if (async != null)
            this.async = async.async();

        Parameter[] pars = executor.getParameters();
        for (Parameter p : pars) {
            Async as = p.getAnnotation(Async.class);
            if (as != null) {
                this.async = as.async();
                break;
            }
            as = p.getType().getAnnotation(Async.class);
            if (as != null && as.async()) {
                this.async = true;
                break;
            }
        }
        this.senderType = pars[0].getType();
        this.parameters = new Parameter[pars.length - 1];
        System.arraycopy(pars, 1, parameters, 0, parameters.length);
    }

    public static void addCustomMatcher(CustomMatcher m, Class... classes) {
        for (Class cl : classes)
            customMatchers.put(cl, m);
    }

    public static void registerCustomMatchers() {
        setParameterWeight(9, List.class, Set.class, ArrayList.class, LinkedList.class, HashSet.class, TreeSet.class);
        setParameterWeight(8, String.class, StringBuilder.class);
        setParameterWeight(7, Float.class, Double.class, Long.class);
        setParameterWeight(6, Integer.class);
        setParameterWeight(5, Byte.class);
        setParameterWeight(4, Boolean.class);

        addCustomMatcher((arg, type) -> {
            try {
                return Bukkit.getPlayer(UUID.fromString(arg));
            } catch (Throwable ignored) {
            }
            return Bukkit.getPlayer(arg);
        }, Player.class);
        addCustomMatcher((arg, type) -> Bukkit.getWorld(arg), World.class);
        addCustomMatcher((arg, type) -> ItemUtils.stringToItemStack(arg), ItemStack.class);
        addCustomMatcher((arg, type) -> arg.charAt(0), Character.class);
        addCustomMatcher((arg, type) -> arg.equalsIgnoreCase("on") ||
                arg.equalsIgnoreCase("true") ||
                arg.equalsIgnoreCase("enable") ||
                arg.equalsIgnoreCase("yes") ||
                arg.equalsIgnoreCase("y") ||
                arg.equalsIgnoreCase(""), boolean.class, Boolean.class);
    }

    private static Object convert(String arg, Type type) {
        Class cl = (Class) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
        cl = Primitives.wrap(cl);
        if (cl == String.class)
            return arg;
        CustomMatcher cm = customMatchers.get(cl);
        if (cm != null)
            return cm.convert(arg, type);
        if (Collection.class.isAssignableFrom(cl)) {
            String[] d = arg.split(",");
            Type target = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (cl == List.class)
                cl = ArrayList.class;
            if (cl == Set.class)
                cl = LinkedHashSet.class;
            Collection out = (Collection) Reflection.newInstance(cl);
            for (String subArg : d)
                out.add(convert(subArg, target));
            return out;
        } else if (cl.isArray()) {
            String[] d = arg.split(",");
            int len = d.length;
            Class target = cl.getComponentType();
            Object out = Array.newInstance(target, len);
            for (int i = 0; i < len; ++i)
                Array.set(out, i, convert(d[i], target));
            return out;
        } else if (Map.class.isAssignableFrom(cl)) {
            String[] d = arg.split(",");
            Type targetKey = ((ParameterizedType) type).getActualTypeArguments()[0];
            Type targetValue = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (cl == Map.class)
                cl = LinkedHashMap.class;
            Map out = (Map) Reflection.newInstance(cl);
            for (String subArg : d) {
                String[] d2 = subArg.split("=", 2);
                out.put(convert(d2[0], targetKey), convert(d2[1], targetValue));
            }
            return out;
        }
        try {
            Method m = cl.getMethod("valueOf", String.class);
            try {
                return m.invoke(null, arg);
            } catch (Throwable e) {
                return m.invoke(null, arg.toUpperCase());
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable e) {
            return null;
        }
        try {
            return cl.getConstructor(String.class).newInstance(arg);
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable e) {
            return null;
        }
        try {
            return cl.getMethod("fromString", String.class).invoke(null, arg);
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable e) {
            return null;
        }
        try {
            return cl.getMethod("of", String.class).invoke(null, arg);
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable e) {
            return null;
        }
        return null;
    }

    private static String getParameterName(Parameter p) {
        Arg a = p.getAnnotation(Arg.class);
        if (a != null)
            return a.value();
        a = p.getType().getAnnotation(Arg.class);
        if (a != null)
            return a.value();
        return p.getType().getSimpleName().toLowerCase();
    }

    public static void setParameterWeight(int weight, Class... classes) {
        for (Class cl : classes)
            parameterWeights.put(cl, weight);
    }

    public static void removeCustomMatcher(Class... classes) {
        for (Class cl : classes)
            customMatchers.remove(cl);
    }

    public void addMatcher(CommandMatcher cm) {
        matchers.add(cm);
    }

    public boolean checkParameters(CommandSender sender, String[] args) {
        if (args.length > 0) {
            CommandMatcher child = children.get(args[0].toLowerCase());
            if (child != null)
                return child.checkParameters(sender, subArgs(args));
        }
        for (CommandMatcher m : matchers)
            if (m.checkParameters(sender, args))
                return true;
        if (executor == null)
            return false;
        if (!senderMatch(sender))
            return false;
        if (args.length < parameters.length)
            return false;
        String[] usedArgs = new String[parameters.length];
        if (parameters.length > 0) {
            if (args.length > parameters.length) {
                System.arraycopy(args, 0, usedArgs, 0, parameters.length - 1);
                usedArgs[parameters.length - 1] = StringUtils.join(args, ' ', parameters.length - 1, args.length);
            } else
                System.arraycopy(args, 0, usedArgs, 0, parameters.length);
        }
        for (int id = 0; id < parameters.length; ++id) {
            Object res = convert(usedArgs[id], parameters[id].getParameterizedType());
            if (res == null)
                return false;
            ArgRange as = parameters[id].getAnnotation(ArgRange.class);
            if (as != null) {
                Comparable min = (Comparable) convert(as.min(), parameters[id].getParameterizedType());
                Comparable max = (Comparable) convert(as.min(), parameters[id].getParameterizedType());
                return min.compareTo(res) <= 0 && max.compareTo(res) >= 0;
            }
        }
        return true;
    }

    @Override
    public int compareTo(CommandMatcher o) {
        int parL = -Integer.compare(parameters.length, o.parameters.length);
        if (parL != 0)
            return parL;
        return Long.compare(getParameterWeight(), o.getParameterWeight());
    }

    private long getParameterWeight() {
        long weight = 0;
        for (Parameter p : parameters)
            weight = weight * 10 + parameterWeights.getOrDefault(Primitives.wrap(p.getType()), 0);
        return weight;
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            CommandMatcher m = children.get(args[0].toLowerCase());
            if (m != null) {
                m.execute(sender, subArgs(args));
                return;
            }
        }
        for (CommandMatcher m : matchers) {
            if (m.checkParameters(sender, args)) {
                m.execute(sender, args);
                return;
            }
        }
        if (executor == null)
            return;
        if (!senderType.isAssignableFrom(sender.getClass())) {
            lang.msg("", sender, "command.noconsole");
            return;
        }
        if (async)
            executeNow(sender, args);
        SU.sch.scheduleSyncDelayedTask(pl, () -> executeNow(sender, args));
    }

    private void executeNow(CommandSender sender, String[] args) {
        Object[] out = new Object[parameters.length + 1];
        out[0] = sender;
        if (parameters.length > 0 && args.length > parameters.length)
            args[parameters.length - 1] = StringUtils.join(args, ' ', parameters.length - 1, args.length);
        System.arraycopy(args, 0, out, 1, parameters.length);
        for (int i = 0; i < parameters.length; ++i) {
            Object res = convert(args[i], parameters[i].getParameterizedType());
            if (res == null) {
                lang.msg("", sender, "command.wrongarg", "type", getParameterName(parameters[i]), "value", args[i]);
                return;
            }
            ArgRange as = parameters[i].getAnnotation(ArgRange.class);
            if (as != null) {
                Comparable min = (Comparable) convert(as.min(), parameters[i].getParameterizedType());
                Comparable max = (Comparable) convert(as.min(), parameters[i].getParameterizedType());
                if (min.compareTo(res) > 0) {
                    lang.msg("", sender, "command.toolow", "type", getParameterName(parameters[i]), "value", min);
                    return;
                }
                if (max.compareTo(res) < 0) {
                    lang.msg("", sender, "command.toohigh", "type", getParameterName(parameters[i]), "value", max);
                    return;
                }
            }
            out[i + 1] = res;
        }
        try {
            executor.invoke(executorOwner, out);
        } catch (Throwable e) {
            SU.log(pl, executorOwner, StringUtils.join(out, ", "));
            for (int i = 0; i < out.length; ++i) {
                SU.log(pl, "should", executor.getParameterTypes()[i]);
                SU.log(pl, "is", i, out[i].getClass().getSimpleName());
            }
            SU.error(sender, e.getCause() == null ? e : e.getCause(), pluginName, "gyurix");
        }
    }

    public CommandMatcher getOrAddChild(String pluginName, String command) {
        return children.computeIfAbsent(command, (cmd) -> new CommandMatcher(pluginName, command));
    }

    public List<String> getUsage(CommandSender sender, String[] args) {
        StringBuilder prefixBuilder = new StringBuilder(
                subOf == null ? "§a/" + command : "§a/" + subOf + " §a<" + command);
        for (String s : getAliases())
            prefixBuilder.append('|').append(s);
        if (subOf != null)
            prefixBuilder.append('>');
        String prefix = prefixBuilder.toString();
        List<String> out = new ArrayList<>();
        if (!children.isEmpty()) {
            String[] sub = subArgs(args);
            if (args.length > 0) {
                CommandMatcher c = children.get(args[0].toLowerCase());
                if (c != null) {
                    for (String s : c.getUsage(sender, sub))
                        out.add(prefix + " " + s.substring(3));
                    return out;
                }
            }
            for (CommandMatcher cm : new LinkedHashSet<>(children.values())) {
                for (String s : cm.getUsage(sender, sub))
                    out.add(prefix + " §6" + s.substring(3).replaceFirst("§6", "§e"));
            }
        }
        for (CommandMatcher cm : matchers)
            out.addAll(cm.getUsage(sender, args));
        if (executor != null)
            out.add(getUsageOfThis(prefix, sender, args));
        return out;
    }

    private String getUsageOfThis(String prefix, CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder(prefix);
        int i = 0;
        for (Parameter p : parameters) {
            if (args.length == i)
                sb.append(" §6<");
            else if (args.length < i)
                sb.append(" §e<");
            else if (isValidParameter(sender, args, i))
                sb.append(" §a<");
            else
                sb.append(" §4<");
            sb.append(getParameterName(p)).append('>');
            ++i;
        }
        return sb.toString();
    }

    public boolean isValidParameter(CommandSender sender, String[] args, int id) {
        if (args.length > 0) {
            CommandMatcher child = children.get(args[0].toLowerCase());
            if (child != null)
                return child.isValidParameter(sender, subArgs(args), id - 1);
        }
        if (executor == null)
            return false;
        if (!senderMatch(sender))
            return false;
        if (parameters.length > 0 && args.length > parameters.length)
            args[parameters.length - 1] = StringUtils.join(args, ' ', parameters.length - 1, args.length);
        Object res = convert(args[id], parameters[id].getParameterizedType());
        if (res == null)
            return false;
        ArgRange as = parameters[id].getAnnotation(ArgRange.class);
        if (as != null) {
            Comparable min = (Comparable) convert(as.min(), parameters[id].getParameterizedType());
            Comparable max = (Comparable) convert(as.min(), parameters[id].getParameterizedType());
            return min.compareTo(res) <= 0 && max.compareTo(res) >= 0;
        }
        return true;
    }

    public boolean senderMatch(CommandSender sender) {
        return senderType.isAssignableFrom(sender.getClass());
    }

    private String[] subArgs(String[] args) {
        if (args.length < 2)
            return noSub;
        String[] out = new String[args.length - 1];
        System.arraycopy(args, 1, out, 0, out.length);
        return out;
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1)
            return SU.filterStart(children.keySet(), args[0]);
        return EMPTY_LIST;
    }
}