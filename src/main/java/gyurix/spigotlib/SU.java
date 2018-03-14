package gyurix.spigotlib;

import gyurix.commands.CustomCommandMap;
import gyurix.configfile.ConfigFile;
import gyurix.mojang.MojangAPI;
import gyurix.protocol.Protocol;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.GameProfile;
import gyurix.spigotlib.Config.PlayerFile;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.DualMap;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static gyurix.commands.CustomCommandMap.knownCommands;

/**
 * SpigotLib utilities class
 */
public final class SU {
    public static final Charset utf8 = Charset.forName("UTF-8");
    /**
     * The instance of current Chat provider in Vault
     */
    public static Chat chat;
    /**
     * Instance of the CustomCommandMap used by SpigotLibs CommandAPI
     */
    public static CustomCommandMap cm;
    /**
     * The main instance of the ConsoleCommandSender object.
     */
    public static ConsoleCommandSender cs;
    /**
     * The instance of current Economy provider in Vault
     */
    public static Economy econ;
    /**
     * An instance of the Javascript script engine, used for the eval variable
     */
    public static ScriptEngine js;
    public static HashSet<UUID> loadedPlayers = new HashSet<>();
    /**
     * The main instance of the Messenger object.
     */
    public static Messenger msg;
    /**
     * The instance of current Permission provider in Vault
     */
    public static Permission perm;
    /**
     * Player configuration file instance (players.yml file in the SpigotLib)
     */
    public static ConfigFile pf;
    /**
     * The main instance of the PluginManager object.
     */
    public static PluginManager pm;
    /**
     * An instance of the Random number generator
     */
    public static Random rand = new Random();
    /**
     * The main instance of the BukkitScheduler object.
     */
    public static BukkitScheduler sch;
    /**
     * The main instance of the ServicesManager object
     */
    public static ServicesManager sm;
    /**
     * The main instance of the CraftServer object.
     */
    public static Server srv;
    /**
     * PacketAPI instance
     */
    public static Protocol tp;
    /**
     * Name - UUID cache
     */
    public static DualMap<String, UUID> uuidCache = new DualMap<>();
    /**
     * True if Vault is found on the server
     */
    public static boolean vault;


    static Field pluginsF, lookupNamesF;
    private static Field entityF;
    private static Constructor entityPlayerC, playerInterractManagerC;
    private static Method getBukkitEntityM, loadDataM, saveDataM;
    private static Field pingF;
    private static Object worldServer, mcServer;

    /**
     * Sends an error report to the given sender and to console. The report only includes the stack trace parts, which
     * contains the authors name
     *
     * @param sender - The CommandSender who should receive the error report
     * @param err    - The error
     * @param plugin - The plugin where the error appeared
     * @param author - The author name, which will be searched in the error report
     */
    public static void error(CommandSender sender, Throwable err, String plugin, String author) {
        if (Config.silentErrors)
            return;
        StringBuilder report = new StringBuilder();
        report.append("§4§l").append(plugin).append(" - ERROR REPORT - ")
                .append(err.getClass().getSimpleName());
        if (err.getMessage() != null)
            report.append('\n').append(err.getMessage());
        int i = 0;
        boolean startrep = true;
        for (StackTraceElement el : err.getStackTrace()) {
            boolean force = el.getClassName() != null && el.getClassName().contains(author);
            if (force)
                startrep = false;
            if (startrep || force)
                report.append("\n§c #").append(++i)
                        .append(": §eLINE §a").append(el.getLineNumber())
                        .append("§e in FILE §6").append(el.getFileName())
                        .append("§e (§7").append(el.getClassName())
                        .append("§e.§b").append(el.getMethodName())
                        .append("§e)");
        }
        String rep = report.toString();
        if (cs == null) {
            System.err.println(ChatColor.stripColor(rep));
            return;
        }
        cs.sendMessage(rep);
        if (sender != null && sender != cs)
            sender.sendMessage(rep);
    }

    /**
     * Escape multi line text to a single line one
     *
     * @param text multi line escapeable text input
     * @return The escaped text
     */
    public static String escapeText(String text) {
        return text.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("|", "\\|")
                .replace(" ", "_")
                .replace("\n", "|");
    }

    /**
     * Fills variables in a String
     *
     * @param s    - The String
     * @param vars - The variables and their values, which should be filled
     * @return The variable filled String
     */
    public static String fillVariables(String s, HashMap<String, Object> vars) {
        for (Entry<String, Object> v : vars.entrySet())
            s = s.replace('<' + v.getKey() + '>', String.valueOf(v.getValue()));
        return s;
    }

    /**
     * Fills variables in a String
     *
     * @param s    - The String
     * @param vars - The variables and their values, which should be filled
     * @return The variable filled String
     */
    public static String fillVariables(String s, Object... vars) {
        String last = null;
        for (Object v : vars) {
            if (last == null)
                last = (String) v;
            else {
                s = s.replace('<' + last + '>', String.valueOf(v));
                last = null;
            }
        }
        return s;
    }

    /**
     * Fills variables in an iterable
     *
     * @param iterable - The iterable
     * @param vars     - The variables and their values, which should be filled
     * @return The variable filled iterable converted to an ArrayList
     */
    public static ArrayList<String> fillVariables(Iterable<String> iterable, Object... vars) {
        ArrayList<String> out = new ArrayList<>();
        iterable.forEach((s) -> {
            String last = null;
            for (Object v : vars) {
                if (last == null)
                    last = (String) v;
                else {
                    s = s.replace('<' + last + '>', String.valueOf(v));
                    last = null;
                }
            }
            out.add(s);
        });
        return out;
    }

    /**
     * Filters the startings of the given data
     *
     * @param data  - The data to be filtered
     * @param start - Filter every string which starts with this one
     * @return The filtered Strings
     */
    public static ArrayList<String> filterStart(String[] data, String start) {
        start = start.toLowerCase();
        ArrayList<String> ld = new ArrayList<>();
        for (String s : data) {
            if (s.toLowerCase().startsWith(start))
                ld.add(s);
        }
        Collections.sort(ld);
        return ld;
    }

    /**
     * Filters the startings of the given data
     *
     * @param data  - The data to be filtered
     * @param start - Filter every string which starts with this one
     * @return The filtered Strings
     */
    public static ArrayList<String> filterStart(Iterable<String> data, String start) {
        start = start.toLowerCase();
        ArrayList<String> ld = new ArrayList<>();
        for (String s : data) {
            if (s.toLowerCase().startsWith(start))
                ld.add(s);
        }
        Collections.sort(ld);
        return ld;
    }

    /**
     * Get the name of an offline player based on it's UUID.
     *
     * @param id UUID of the target player
     * @return The name of the requested player or null if the name was not found.
     */
    public static String getName(UUID id) {
        Player plr = Bukkit.getPlayer(id);
        if (plr != null)
            return plr.getName();
        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
        if (op == null)
            return MojangAPI.getProfile(id.toString()).name;
        return op.getName();
    }

    public static UUID getOfflineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(utf8));
    }

    public static UUID getOnlineUUID(String name) {
        name = name.toLowerCase();
        UUID uid = uuidCache.get(name);
        if (uid == null) {
            GameProfile prof = MojangAPI.getProfile(name);
            if (prof == null) {
                cs.sendMessage("§cInvalid online player name: §e" + name + "§c. Using offline UUID.");
                return getOfflineUUID(name);
            }
            uid = prof.id;
            uuidCache.put(name, uid);
        }
        return uid;
    }

    /**
     * Get the ping of a player in milliseconds
     *
     * @param plr target player
     * @return The ping of the given player in milliseconds.
     */
    public static int getPing(Player plr) {
        try {
            return pingF.getInt(entityF.get(plr));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get an online player or optionally load an offline player based on its name
     *
     * @param name name of the player, which should be got / active.
     * @return The online player / active offline player who has the given name, or null if no such player have found.
     */
    public static Player getPlayer(String name) {
        if (name.length() > 16) {
            UUID uuid = UUID.fromString(name);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null)
                p = loadPlayer(uuid);
            return p;
        }
        Player p = Bukkit.getPlayerExact(name);
        if (p == null)
            p = loadPlayer(getUUID(name));
        return p;
    }

    public static Player getPlayer(UUID uid) {
        Player plr = Bukkit.getPlayer(uid);
        if (plr != null)
            return plr;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uid);
        if (op != null)
            return loadPlayer(uid);
        return null;
    }

    /**
     * Get the configuration part of a player or the CONSOLE
     *
     * @param plr the player, whos configuration part will be returned
     * @return the configuration part of the given player, or the configuration part of the CONSOLE, if the given player
     * is null.
     */
    public static ConfigFile getPlayerConfig(Player plr) {
        return getPlayerConfig(plr == null ? null : plr.getUniqueId());
    }

    /**
     * Get the configuration part of an online/offline player using based on his UUID, or the
     * configuration part of the CONSOLE, if the given UUID is null.
     *
     * @param plr the UUID of the online/offline player
     * @return the configuration part of the given player, or the configuration part of the CONSOLE, if the given player
     * UUID is null.
     */
    public static ConfigFile getPlayerConfig(final UUID plr) {
        String pln = plr == null ? "CONSOLE" : plr.toString();
        if (pf.data.mapData == null)
            pf.data.mapData = new LinkedHashMap();
        if (PlayerFile.backend == BackendType.MYSQL && !loadedPlayers.contains(plr)) {
            loadPlayerConfig(plr);
            sch.scheduleSyncDelayedTask(Main.pl, () -> {
                savePlayerConfig(plr);
                unloadPlayerConfig(plr);
            }, 3);
        }
        return pf.subConfig(pln);
    }

    /**
     * Get GameProfile of the given player. The GameProfile contains the players name, UUID and skin.
     *
     * @param plr target player
     * @return the GameProfile of the target player
     */
    public static GameProfile getProfile(Player plr) {
        try {
            return new GameProfile(plr.getClass().getMethod("getProfile").invoke(plr));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the UUID of an offline player based on his name.
     *
     * @param name name of the target player
     * @return The UUID of the requested player, or null if it was not found.
     */
    public static UUID getUUID(String name) {
        Player plr = Bukkit.getPlayer(name);
        if (plr != null)
            return plr.getUniqueId();
        OfflinePlayer[] offlinePls = Bukkit.getOfflinePlayers();
        for (OfflinePlayer p : offlinePls) {
            if (p.getName() != null && p.getName().equals(name))
                return p.getUniqueId();
        }
        name = name.toLowerCase();
        for (OfflinePlayer p : offlinePls) {
            if (p.getName() != null && p.getName().toLowerCase().equals(name))
                return p.getUniqueId();
        }
        for (OfflinePlayer p : offlinePls) {
            if (p.getName() != null && p.getName().toLowerCase().contains(name))
                return p.getUniqueId();
        }
        return getOnlineUUID(name);
    }

    /**
     * Get the UUID of an offline player based on his name.
     *
     * @param name name of the target player
     * @return The UUID of the requested player, or null if it was not found.
     */
    public static UUID getUUIDExact(String name) {
        Player plr = Bukkit.getPlayer(name);
        if (plr != null)
            return plr.getUniqueId();
        OfflinePlayer[] offlinePls = Bukkit.getOfflinePlayers();
        for (OfflinePlayer p : offlinePls) {
            if (p.getName() != null && p.getName().equals(name))
                return p.getUniqueId();
        }
        return MojangAPI.getProfile(name).id;
    }

    static void initOfflinePlayerManager() {
        try {
            Class mcServerClass = Reflection.getNMSClass("MinecraftServer");
            Class entityPlayerClass = Reflection.getNMSClass("EntityPlayer");
            Class craftPlayerClass = Reflection.getOBCClass("entity.CraftPlayer");
            Class pIMClass = Reflection.getNMSClass("PlayerInteractManager");
            Class worldServerClass = Reflection.getNMSClass("WorldServer");

            entityF = Reflection.getField(Reflection.getOBCClass("entity.CraftEntity"), "entity");
            pingF = Reflection.getNMSClass("EntityPlayer").getField("ping");
            mcServer = mcServerClass.getMethod("getServer").invoke(null);
            playerInterractManagerC = pIMClass.getConstructor(Reflection.getNMSClass("World"));
            worldServer = mcServerClass.getMethod("getWorldServer", Integer.TYPE).invoke(mcServer, 0);
            entityPlayerC = entityPlayerClass.getConstructor(mcServerClass, worldServerClass, Reflection.getUtilClass("com.mojang.authlib.GameProfile"), pIMClass);
            getBukkitEntityM = entityPlayerClass.getMethod("getBukkitEntity");
            loadDataM = craftPlayerClass.getMethod("loadData");
            saveDataM = craftPlayerClass.getMethod("saveData");
        } catch (Throwable e) {
            log(Main.pl, "§cError in initializing offline player manager.");
            error(cs, e, "SpigotLib", "gyurix");
        }
    }

    /**
     * Load an offline player to be handleable like an online one.
     *
     * @param uuid uuid of the loadable offline player
     * @return the active Player object, or null if the player was not found.
     */
    public static Player loadPlayer(UUID uuid) {
        try {
            if (uuid == null) {
                return null;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null || !player.hasPlayedBefore()) {
                return null;
            }

            Player plr = (Player) getBukkitEntityM.invoke(entityPlayerC.newInstance(mcServer, worldServer, new GameProfile(player.getName(), uuid).toNMS(), playerInterractManagerC.newInstance(worldServer)));
            if (plr != null) {
                loadDataM.invoke(plr);
                return plr;
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static void loadPlayerConfig(UUID uid) {
        if (PlayerFile.backend == BackendType.MYSQL) {
            String key = uid == null ? "CONSOLE" : uid.toString();
            pf.mysqlLoad(key, "uuid='" + key + '\'');
            loadedPlayers.add(uid);
        }
    }

    /**
     * Logs messages from the given plugin. You can use color codes in the msg.
     *
     * @param pl  - The plugin who wants to log the message
     * @param msg - The message which should be logged
     */
    public static void log(Plugin pl, Object... msg) {
        String s = pl == null ? "" : ('[' + pl.getName() + "] ") + StringUtils.join(msg, ", ");
        if (cs != null)
            cs.sendMessage(s);
        else
            System.out.println(ChatColor.stripColor(s));
    }

    /**
     * Logs messages from the given plugin. You can use color codes in the msg.
     *
     * @param pl  - The plugin who wants to log the message
     * @param msg - The message which should be logged
     */
    public static void log(Plugin pl, Iterable<Object>... msg) {
        cs.sendMessage('[' + pl.getName() + "] " + StringUtils.join(msg, ", "));
    }

    /**
     * Convertion of a collection of player UUIDs to the Arraylist containing the player names matching with the UUIDs.
     *
     * @param uuids collection of player uuids which will be converted to names
     * @return the convertion result, which is an ArrayList of player names
     */
    public static ArrayList<String> namesFromUUIDs(Collection<UUID> uuids) {
        ArrayList<String> out = new ArrayList<>();
        for (UUID id : uuids) {
            out.add(getName(id));
        }
        return out;
    }

    /**
     * Convertion of a collection of player names to the Arraylist containing the player UUIDs matching with the names.
     *
     * @param names collection of player names which will be converted to UUIDs
     * @return the convertion result, which is an ArrayList of player UUIDs
     */
    public static ArrayList<UUID> namesToUUIDs(Collection<String> names) {
        ArrayList<UUID> out = new ArrayList<>();
        for (String s : names) {
            out.add(getUUID(s));
        }
        return out;
    }

    /**
     * Optimizes color and formatting code usage in a string by removing redundant color/formatting codes
     *
     * @param in input message containing color and formatting codes
     * @return The color and formatting code optimized string
     */
    public static String optimizeColorCodes(String in) {
        StringBuilder out = new StringBuilder();
        StringBuilder oldFormat = new StringBuilder("§r");
        StringBuilder newFormat = new StringBuilder("§r");
        StringBuilder formatChange = new StringBuilder();
        String formatArchive = "";
        boolean color = false;
        for (char c : in.toCharArray()) {
            if (color) {
                color = false;
                if (c >= 'k' && c <= 'o') {
                    int max = newFormat.length();
                    boolean add = true;
                    for (int i = 1; i < max; i += 2) {
                        if (newFormat.charAt(i) == c) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        newFormat.append('§').append(c);
                        formatChange.append('§').append(c);
                    }
                    continue;
                }
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')))
                    c = 'f';
                newFormat.setLength(0);
                newFormat.append('§').append(c);
                formatChange.setLength(0);
                formatChange.append('§').append(c);
            } else if (c == '§')
                color = true;
            else if (c == '\u7777') {
                formatArchive = newFormat.toString();
            } else if (c == '\u7778') {
                oldFormat.setLength(0);
                newFormat.setLength(0);
                newFormat.append(formatArchive);
                formatChange.setLength(0);
                formatChange.append(formatArchive);
            } else {
                if (!newFormat.toString().equals(oldFormat.toString())) {
                    out.append(formatChange);
                    formatChange.setLength(0);
                    oldFormat.setLength(0);
                    oldFormat.append(newFormat);
                }
                out.append(c);
                if (c == '\n') {
                    formatChange.insert(0, oldFormat);
                    oldFormat.setLength(0);
                    newFormat.append(formatChange.toString());
                }
            }
        }
        return out.toString();
    }

    /**
     * Generates a random number between min (inclusive) and max (exclusive)
     *
     * @param min - Minimal value of the random number
     * @param max - Maximal value of the random number
     * @return A random double between min and max
     */
    public static double rand(double min, double max) {
        return rand.nextDouble() * Math.abs(max - min) + min;
    }

    /**
     * Generate a configurable random color
     *
     * @param minSaturation - Minimal saturation (0-1)
     * @param maxSaturation - Maximal saturation (0-1)
     * @param minLuminance  - Minimal luminance (0-1)
     * @param maxLuminance  - Maximal luminance (0-1)
     * @return The generated random color
     */
    public static Color randColor(double minSaturation, double maxSaturation, double minLuminance, double maxLuminance) {
        float hue = rand.nextFloat();
        float saturation = (float) rand(minSaturation, maxSaturation);
        float luminance = (float) rand(minLuminance, maxLuminance);
        java.awt.Color color = java.awt.Color.getHSBColor(hue, saturation, luminance);
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Save a active offline player. You should use this method when you have active an offline player
     * and you have changed some of it's data
     *
     * @param plr Loaded offline players Player object
     */
    public static void savePlayer(Player plr) {
        try {
            saveDataM.invoke(plr);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerConfig(UUID uid) {
        String key = uid == null ? "CONSOLE" : uid.toString();
        switch (PlayerFile.backend) {
            case FILE:
                pf.data.unWrapAll();
                pf.save();
                return;
            case MYSQL:
                ArrayList<String> list = new ArrayList<>();
                ConfigFile kf = pf.subConfig(key, "uuid='" + key + '\'');
                kf.data.unWrapAll();
                kf.mysqlUpdate(list, null);
                pf.db.batch(list, null);
        }
    }

    /**
     * Save files from the given plugins jar file to its subfolder in the plugins folder. The files will only be saved
     * if they doesn't exists in the plugins subfolder.
     *
     * @param pl        instane of the plugin
     * @param fileNames names of the saveable files
     */
    public static void saveResources(Plugin pl, String... fileNames) {
        Logger log = pl.getLogger();
        File df = pl.getDataFolder();
        ClassLoader cl = pl.getClass().getClassLoader();
        df.mkdir();
        for (String fn : fileNames) {
            try {
                File f = new File(df + File.separator + fn);
                if (!f.exists()) {
                    if (fn.contains(File.separator)) {
                        new File(fn.substring(0, fn.lastIndexOf(File.separatorChar))).mkdirs();
                    }
                    InputStream is = cl.getResourceAsStream(fn);
                    if (is == null) {
                        log.severe("Error, the requested file (" + fn + ") is missing from the plugins jar file.");
                    } else
                        Files.copy(is, f.toPath());
                }
            } catch (Throwable e) {
                log.severe("Error, on copying file (" + fn + "): ");
                e.printStackTrace();
            }
        }
    }

    /**
     * Set maximum length of a String by cutting the redundant characters off from it
     *
     * @param in  input String
     * @param len maximum length
     * @return The cutted String, which will maximally len characters.
     */
    public static String setLength(String in, int len) {
        return in.length() > len ? in.substring(0, len) : in;
    }

    public static String[] splitPage(String text, int lines) {
        String[] d = text.split("\n");
        String[] out = new String[(d.length + (lines - 1)) / lines];
        for (int i = 0; i < d.length; i++) {
            if (i % lines == 0)
                out[i / lines] = d[i];
            else
                out[i / lines] += "\n" + d[i];
        }
        return out;
    }

    /**
     * Unescape multi line to single line escaped text
     *
     * @param text multi line escaped text input
     * @return The unescaped text
     */

    public static String unescapeText(String text) {
        return (' ' + text).replaceAll("([^\\\\])_", "$1 ")
                .replaceAll("([^\\\\])\\|", "$1\n")
                .replaceAll("([^\\\\])\\\\([_\\|])", "$1$2")
                .replace("\\\\", "\\").substring(1);
    }

    /**
     * Unloads the configuration of the given player or of the console if
     * uid = null
     *
     * @param uid - The UUID of the player, or null for console
     * @return True if the unload was successful, false otherwise
     */
    public static boolean unloadPlayerConfig(UUID uid) {
        if (PlayerFile.backend == BackendType.MYSQL) {
            String key = uid == null ? "CONSOLE" : uid.toString();
            loadedPlayers.remove(uid);
            return pf.removeData(key);
        }
        return false;
    }

    /**
     * Unloads a plugin
     *
     * @param p - The unloadable plugin
     */
    public static void unloadPlugin(Plugin p) {
        try {
            if (!p.isEnabled())
                return;
            String pn = p.getName();
            for (Plugin p2 : pm.getPlugins()) {
                PluginDescriptionFile pdf = p2.getDescription();
                if (pdf.getDepend() != null && pdf.getDepend().contains(pn) || pdf.getSoftDepend() != null && pdf.getSoftDepend().contains(pn))
                    unloadPlugin(p2);
            }
            pm.disablePlugin(p);
            ((List) pluginsF.get(pm)).remove(p);
            ((Map) lookupNamesF.get(pm)).remove(pn);
            for (Iterator it = knownCommands.entrySet().iterator(); it.hasNext(); ) {
                Entry entry = (Entry) it.next();
                if ((entry.getValue() instanceof PluginCommand)) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == p)
                        it.remove();
                }
            }
            ((URLClassLoader) p.getClass().getClassLoader()).close();
            System.gc();
        } catch (Throwable e) {
            error(cs, e, "SpigotLib", "gyurix");
        }
    }
}