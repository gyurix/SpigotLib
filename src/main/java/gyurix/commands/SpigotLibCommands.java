package gyurix.commands;

import com.google.common.collect.Lists;
import gyurix.api.VariableAPI;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.nbt.NBTCompound;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.GlobalLangFile;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.ItemUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static gyurix.spigotlib.Config.PlayerFile.backend;
import static gyurix.spigotlib.Config.PlayerFile.mysql;
import static gyurix.spigotlib.Config.allowAllPermsForAuthor;
import static gyurix.spigotlib.Config.purgePF;
import static gyurix.spigotlib.Main.*;
import static gyurix.spigotlib.SU.*;

public class SpigotLibCommands implements CommandExecutor, TabCompleter {
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        try {
            Player plr = sender instanceof Player ? (Player) sender : null;
            String cmd = args.length == 0 ? "help" : args[0].toLowerCase();
            if (!sender.hasPermission("spigotlib.command." + cmd) && !(allowAllPermsForAuthor && plr != null && plr.getUniqueId().equals(author))) {
                lang.msg(sender, "noperm");
                return true;
            }
            ArrayList<Player> pls = plr == null ? Lists.<Player>newArrayList() : newArrayList(plr);
            int stripArg = 1;
            if (args.length > 1) {
                if (args[1].equals("*")) {
                    stripArg = 2;
                    pls = new ArrayList<>(Bukkit.getOnlinePlayers());
                } else if (args[1].startsWith("p:")) {
                    stripArg = 2;
                    pls.clear();
                    for (String s : args[1].substring(2).split(",")) {
                        Player p = getPlayer(s);
                        if (p == null) {
                            lang.msg(sender, "player.notfound", "player", p.getName());
                            continue;
                        }
                        pls.add(p);
                    }
                }
            }
            args = (String[]) ArrayUtils.subarray(args, stripArg, args.length);
            String fullMsg = StringUtils.join(args, ' ');
            if (fullMsg.contains("<eval:") && plr != null && !Config.playerEval) {
                lang.msg(plr, "vars.noeval");
                return true;
            }
            fullMsg = VariableAPI.fillVariables(fullMsg, plr);
            switch (cmd) {
                case "help":
                    lang.msg(sender, "help", "version", version);
                    return true;
                case "nbt": {
                    String nms = new ConfigData(new ItemStackWrapper(plr.getItemInHand()).getNbtData()).toString();
                    sender.sendMessage(nms);
                    ItemStackWrapper isw = new ItemStackWrapper(new ConfigFile(nms).data.deserialize(NBTCompound.class));
                    sender.sendMessage("After load:\n" + new ConfigData(isw));
                    plr.setItemInHand(isw.toBukkitStack());
                    return true;
                }
                case "cmd":
                    for (Player p : pls) {
                        for (String s : fullMsg.split(";"))
                            new gyurix.commands.Command(s).execute(p);
                    }
                    return true;
                case "vars":
                    if (args.length == 0)
                        lang.msg(sender, "vars", "vars", StringUtils.join(new TreeSet<>(VariableAPI.handlers.keySet()), ", "));
                    else
                        lang.msg(sender, "vars.filled", "result", fullMsg);
                    return true;
                case "perm":
                    if (args.length == 0) {
                        String f = lang.get(plr, "perms.fillformat");
                        String denyperm = lang.get(plr, "perms.denyformat");
                        String allowperm = lang.get(plr, "perms.allowformat");
                        StringBuilder sb = new StringBuilder();
                        for (Player p : pls) {
                            p.getEffectivePermissions().forEach((permInfo) -> {
                                sb.append('\n');
                                permInfo.getAttachment().getPermissions().forEach((perm, value) ->
                                        sb.append('\n').append(SU.fillVariables(value ? allowperm : denyperm, "perm", perm)));
                            });
                            sender.sendMessage(SU.fillVariables(f, "player", p.getName(), "<perms>", sb.toString()));
                        }
                        return true;
                    }
                    for (Player p : pls)
                        lang.msg(sender, p.hasPermission(args[0]) ? "perms.yes" : "perms.no", "player", p.getName(), "perm", args[0]);
                    return true;
                case "debug":
                    Config.debug.handleCommand(sender, "sl", args, 0);
                    return true;
                case "class":
                    sender.sendMessage("Classes in package " + args[0] + ": " + StringUtils.join(getClasses(args[0]), '\n'));
                    return true;
                case "purge":
                    lang.msg(sender, "purge.pf");
                    purgePF = true;
                    kf.save();
                    return true;
                case "pf":
                    int page = 1;
                    boolean pageChange = false;
                    try {
                        page = Integer.valueOf(args[args.length - 1]);
                        pageChange = true;
                    } catch (Throwable ignored) {
                    }
                    if (page < 1)
                        page = 1;
                    if (args.length > (pageChange ? 1 : 0)) {
                        if (args[0].equalsIgnoreCase("console")) {
                            String[] txt = splitPage(getPlayerConfig((UUID) null).toString(), 10);
                            if (page > txt.length)
                                page = txt.length;
                            sender.sendMessage("§6§lPlayerFileViewer - §e§lCONSOLE§6§l - page §e§l" + page + "§6§l of §e§l" + txt.length + "\n§f" + txt[page - 1]);
                            return true;
                        }
                        Player p = getPlayer(args[0]);
                        String[] txt = splitPage(getPlayerConfig(p.getUniqueId()).toString(), 10);
                        if (page > txt.length)
                            page = txt.length;
                        sender.sendMessage("§6§lPlayerFileViewer - §e§l" + p.getName() + "§6§l - page §e§l" + page + "§6§l of §e§l" + txt.length + "\n§f" + txt[page - 1]);
                        return true;
                    }
                    String[] txt = splitPage(pf.toString(), 10);
                    if (page > txt.length)
                        page = txt.length;
                    sender.sendMessage("§6§lPlayerFileViewer - page " + page + " of " + txt.length + "\n§f" + txt[page - 1]);
                    return true;
                case "reload":
                    if (args.length == 0) {
                        lang.msg(sender, "reload");
                        return true;
                    }
                    switch (args[0]) {
                        case "config":
                            kf.reload();
                            kf.data.deserialize(Config.class);
                            lang.msg(sender, "reload.config");
                            return true;
                        case "lf":
                            GlobalLangFile.unloadLF(lang);
                            saveResources(pl, "lang.yml");
                            lang = GlobalLangFile.loadLF("spigotlib", pl.getResource("lang.yml"), pl.getDataFolder() + File.separator + "lang.yml");
                            lang.msg(sender, "reload.lf");
                            return true;
                        case "pf":
                            if (backend == BackendType.FILE) {
                                pf.reload();
                            } else {
                                pf.data.mapData = new LinkedHashMap<>();
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    loadPlayerConfig(pl.getUniqueId());
                                }
                                loadPlayerConfig(null);
                            }
                            lang.msg(sender, "reload.pf");
                            return true;
                    }
                    lang.msg(sender, "invalidcmd");
                    return true;
                case "save":
                    if (args.length == 0) {
                        lang.msg(sender, "save");
                        return true;
                    }
                    if (args[0].equals("pf")) {
                        if (backend == BackendType.FILE)
                            pf.save();
                        else {
                            for (ConfigData cd : new ArrayList<>(pf.data.mapData.keySet())) {
                                savePlayerConfig(cd.stringData.length() == 40 ? UUID.fromString(cd.stringData) : null);
                            }
                        }
                        lang.msg(sender, "save.pf");
                        return true;
                    }
                    lang.msg(sender, "invalidcmd");
                    return true;
                case "velocity":
                    org.bukkit.util.Vector v = new Vector(Double.valueOf(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]));
                    for (Player p : pls) {
                        p.setVelocity(v);
                        lang.msg(sender, "velocity.set");
                    }
                    return true;
                case "migratetodb":
                    pf.db = mysql;
                    pf.dbKey = "key";
                    pf.dbValue = "value";
                    pf.dbTable = mysql.table;
                    lang.msg(sender, "migrate.start");
                    ArrayList<String> l = new ArrayList<>();
                    l.add("DROP TABLE IF EXISTS " + mysql.table);
                    l.add("CREATE TABLE " + mysql.table + " (uuid VARCHAR(40), `key` TEXT(1), `value` TEXT(1))");
                    for (Map.Entry<ConfigData, ConfigData> e : pf.data.mapData.entrySet()) {
                        ConfigFile kf = pf.subConfig("" + e.getKey(), "uuid='" + e.getKey() + "'");
                        kf.mysqlUpdate(l, null);
                    }
                    ConfigFile kff = pf.subConfig("CONSOLE", "uuid='CONSOLE'");
                    kff.mysqlUpdate(l, null);
                    mysql.batch(l, () -> lang.msg(sender, "migrate.end"));
                    backend = BackendType.MYSQL;
                    kf.save();
                    return true;
                case "lang":
                    if (args.length == 0) {
                        lang.msg(sender, "lang.list", "langs", StringUtils.join(GlobalLangFile.map.keySet(), ", "));
                        for (Player p : pls) {
                            String lng = getPlayerConfig(p).getString("lang");
                            if (lng == null)
                                lng = Config.defaultLang;
                            lang.msg(sender, "lang." + (p == sender ? "own" : "other"), "player", sender.getName(), "lang", lng);
                        }
                        return true;
                    }
                    args[0] = args[0].toLowerCase();
                    for (Player p : pls) {
                        getPlayerConfig(p).setString("lang", args[0]);
                        CommandSender cs = p == null ? SU.cs : p;
                        lang.msg(sender, "lang.set" + (p == sender ? "" : ".other"), "player", cs.getName(), "lang", args[0]);
                    }
                    return true;
                case "item":
                    if (args.length == 0) {
                        for (Player p : pls)
                            lang.msg(sender, p == sender ? "item.own" : "item.player", "name", p.getName(), "item", ItemUtils.itemToString(p.getItemInHand()));
                        return true;
                    }
                    boolean give = fullMsg.startsWith("give ");
                    if (give)
                        fullMsg = fullMsg.substring(5);
                    ItemStack is = ItemUtils.stringToItemStack(fullMsg);
                    fullMsg = ItemUtils.itemToString(is);
                    if (give)
                        for (Player p : pls) {
                            ItemUtils.addItem(p.getInventory(), is, is.getMaxStackSize());
                            lang.msg(sender, "item.give", "player", p.getName(), "item", fullMsg);
                        }
                    else
                        for (Player p : pls) {
                            plr.setItemInHand(is);
                            lang.msg(sender, "item.set", "player", p.getName(), "item", fullMsg);
                        }
                    return true;
                default:
                    lang.msg(sender, "help", "version", version);
                    return true;
            }
        } catch (Throwable e) {
            error(sender, e, "SpigotLib", "gyurix");
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> out = new ArrayList<>();
        if (!sender.hasPermission("spigotlib.use")) {
            lang.msg(sender, "noperm");
            return out;
        }
        if (args.length == 1) {
            args[0] = args[0].toLowerCase();
            for (String cmd : commands) {
                if (!cmd.startsWith(args[0]) || !sender.hasPermission("spigotlib.command." + cmd)) continue;
                out.add(cmd);
            }
        } else if (args.length == 2) {
            if (args[0].equals("reload")) {
                return filterStart(new String[]{"config", "pf", "lf"}, args[1]);
            } else {
                return null;
            }
        }
        return out;
    }
}
