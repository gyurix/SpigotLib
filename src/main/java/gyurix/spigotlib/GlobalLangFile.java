package gyurix.spigotlib;

import gyurix.chat.ChatTag;
import gyurix.configfile.ConfigData;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.ChatAPI.ChatMessageType;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import static gyurix.spigotlib.Config.debug;

public class GlobalLangFile {
    public static final HashMap<String, HashMap<String, String>> map = new HashMap<>();

    public static String get(String lang, String adr) {
        HashMap<String, String> m;
        String msg;
        if (lang != null && !lang.isEmpty()) {
            m = map.get(lang);
            if (m != null) {
                msg = m.get(adr);
                if (msg != null) {
                    return msg;
                }
                debug.msg("Lang", "§cThe requested key (" + adr + ") is missing from language " + lang + ". Using servers default language...");
            }
            SU.log(Main.pl, "§cThe requested language (" + lang + ") is not available.");
        }
        if ((m = map.get(Config.defaultLang)) != null) {
            msg = m.get(adr);
            if (msg != null) {
                return msg;
            }
            debug.msg("Lang", "§cThe requested key (" + adr + ") is missing from servers default language (" + Config.defaultLang + "). Trying to find it in any other language...");
        }
        for (HashMap<String, String> l : map.values()) {
            String msg2 = l.get(adr);
            if (msg2 == null) continue;
            return msg2;
        }
        debug.msg("Lang", "§cThe requested key (" + adr + ") wasn't found in any language.");
        return "§cNot found (§f" + lang + "." + adr + "§c)\\-T§ePlease try to remove the plugins lang.yml file. If the problem still appears, please contact the plugins dev.\\-S" + lang + "." + adr + " ";
    }

    private static void load(String[] data) {
        StringBuilder adr = new StringBuilder(".en");
        StringBuilder cs = new StringBuilder();
        int lvl = 0;
        int line = 0;
        for (String s : data) {
            int blockLvl = 0;
            ++line;
            while (s.charAt(blockLvl) == ' ') {
                ++blockLvl;
            }
            String[] d = ((s = s.substring(blockLvl)) + " ").split(" *: +", 2);
            if (d.length == 1) {
                s = ConfigData.unescape(s);
                if (cs.length() != 0) {
                    cs.append('\n');
                }
                cs.append(s);
                continue;
            }
            put(adr.substring(1), cs.toString());
            cs.setLength(0);
            if (blockLvl == lvl + 2) {
                adr.append(".").append(d[0]);
                lvl += 2;
            } else if (blockLvl == lvl) {
                adr = new StringBuilder(adr.substring(0, adr.toString().lastIndexOf('.') + 1) + d[0]);
            } else if (blockLvl < lvl && blockLvl % 2 == 0) {
                while (blockLvl != lvl) {
                    lvl -= 2;
                    adr = new StringBuilder(adr.substring(0, adr.toString().lastIndexOf('.')));
                }
                adr = new StringBuilder(adr.substring(0, adr.toString().lastIndexOf('.') + 1) + d[0]);
            } else {
                throw new RuntimeException("Block leveling error in line " + line + "!");
            }
            if (d[1].isEmpty()) continue;
            cs.append(d[1].substring(0, d[1].length() - 1));
        }
        put(adr.substring(1), cs.toString());
    }

    public static PluginLang loadLF(String pn, InputStream stream, String fn) {
        try {
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            load(new String(bytes, "UTF-8").replaceAll("&([0-9a-fk-or])", "§$1").split("\r?\n"));
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").replaceAll("&([0-9a-fk-or])", "§$1").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PluginLang loadLF(String pn, String fn) {
        try {
            load(new String(Files.readAllBytes(new File(fn).toPath()), "UTF-8").replaceAll("&([0-9a-fk-or])", "§$1").split("\r?\n"));
            return new PluginLang(pn);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void put(String adr, String value) {
        if (!adr.contains(".")) {
            if (!map.containsKey(adr)) {
                map.put(adr, new HashMap());
            }
        } else {
            HashMap<String, String> m = map.get(adr.substring(0, adr.indexOf('.')));
            m.put(adr.substring(adr.indexOf('.') + 1), value);
        }
    }

    public static void unloadLF(PluginLang lng) {
        for (HashMap<String, String> m : map.values()) {
            Iterator<Entry<String, String>> i = m.entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String> e = i.next();
                if (!e.getKey().matches(".*\\." + lng.pluginName + ".*")) continue;
                i.remove();
            }
        }
    }

    public static class PluginLang {
        public final String pluginName;

        private PluginLang(String plugin) {
            pluginName = plugin;
        }

        public String get(Player plr, String adr, String... repl) {
            return get(plr, adr, (Object[]) repl);
        }

        public String get(Player plr, String adr, Object... repl) {
            String msg = GlobalLangFile.get(SU.getPlayerConfig(plr).getString("lang"), pluginName + '.' + adr);
            Object key = null;
            for (Object o : repl) {
                if (key == null) {
                    key = o;
                    continue;
                }
                msg = msg.replace("<" + key + '>', String.valueOf(o));
                key = null;
            }
            return msg;
        }

        public void msg(String prefix, CommandSender sender, String msg, Object... repl) {
            Player plr = sender instanceof Player ? (Player) sender : null;
            msg = prefix + get(plr, msg, repl);
            if (plr == null || Reflection.ver.isBellow(ServerVersion.v1_7)) {
                sender.sendMessage(ChatTag.stripExtras(msg));
            } else {
                ChatAPI.sendJsonMsg(ChatMessageType.CHAT, msg, plr);
            }
        }

        public void msg(CommandSender sender, String msg, String... repl) {
            msg(sender, msg, (Object[]) repl);
        }

        public void msg(CommandSender sender, String msg, Object... repl) {
            Player plr = sender instanceof Player ? (Player) sender : null;
            msg(get(plr, "prefix"), sender, msg, repl);
        }

        public void msg(String prefix, CommandSender sender, String msg, String... repl) {
            msg(prefix, sender, msg, (Object[]) repl);
        }

    }

}

