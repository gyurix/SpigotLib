package gyurix.spigotlib;

import gyurix.animation.AnimationAPI;
import gyurix.api.BungeeAPI;
import gyurix.api.VariableAPI;
import gyurix.commands.CustomCommandMap;
import gyurix.commands.SpigotLibCommands;
import gyurix.configfile.ConfigFile;
import gyurix.configfile.ConfigSerialization;
import gyurix.economy.EconomyAPI;
import gyurix.inventory.CustomGUI;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.manager.ProtocolImpl;
import gyurix.protocol.manager.ProtocolLegacyImpl;
import gyurix.protocol.utils.WrapperFactory;
import gyurix.scoreboard.PlayerBars;
import gyurix.scoreboard.ScoreboardAPI;
import gyurix.scoreboard.ScoreboardBar;
import gyurix.sign.SignGUI;
import gyurix.spigotlib.Config.PlayerFile;
import gyurix.spigotlib.GlobalLangFile.PluginLang;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.TPSMeter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.collect.Lists.newArrayList;
import static gyurix.economy.EconomyAPI.VaultHookType.*;
import static gyurix.economy.EconomyAPI.vaultHookType;
import static gyurix.protocol.Reflection.ver;
import static gyurix.spigotlib.Config.PlayerFile.backend;
import static gyurix.spigotlib.Config.PlayerFile.mysql;
import static gyurix.spigotlib.Config.forceReducedMode;
import static gyurix.spigotlib.Items.enchants;
import static gyurix.spigotlib.SU.*;
import static gyurix.spigotutils.ServerVersion.UNKNOWN;
import static gyurix.spigotutils.ServerVersion.v1_8;

/**
 * Main SpigotLib plugin class containing all the command handlers, loaders and delegators needed for starting up the API
 */
public class Main extends JavaPlugin implements Listener {
    /**
     * The UUID of the plugins author for being able to grant him full plugin, if allowed in the config
     */
    public static final UUID author = UUID.fromString("877c9660-b0da-4dcb-8f68-9146340f2f68");
    /**
     * The list of SpigotLib subcommands used for tab completion.
     */
    public static final String[] commands = {"chm", "abm", "sym", "title", "vars", "perm", "lang", "save", "reload", "velocity", "setamount", "item"};
    /**
     * Current version of the plugin, stored here to not be able to be abused so easily by server owners, by changing the plugin.yml file
     */
    public static final String version = "7.1";
    /**
     * Data directory of the plugin (plugins/SpigotLib folder)
     */
    public static File dir;
    /**
     * Tells if the server was fully enabled, or not yet. If not yet, then the players are automatically kicked to prevent any damage caused by too early joins.
     */
    public static ConfigFile kf, itemf;
    public static PluginLang lang;
    public static Main pl;
    private static boolean schedulePacketAPI;

    public static ArrayList<Class> getClasses(String packageName) {
        ArrayList<Class> classes = new ArrayList();
        try {
            String packagePrefix = packageName.replace(".", "/");
            File f = new File(Material.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6));
            ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                if (name.startsWith(packagePrefix) && name.endsWith(".class") && !name.contains("$"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6).replace("/", ".")));
                ze = zis.getNextEntry();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void load() throws Throwable {
        cs.sendMessage("§2[§aStartup§2]§e Loading §aconfiguration§e and §alanguage file§e...");
        saveResources(this, "lang.yml", "config.yml", "items.yml");
        kf = new ConfigFile(getResource("config.yml"));
        kf.load(new File(dir + File.separator + "config.yml"));
        kf.data.deserialize(Config.class);
        Config.debug.setPlugin(this);
        kf.save();
        lang = GlobalLangFile.loadLF("spigotlib", getResource("lang.yml"), dir + File.separator + "lang.yml");

        cs.sendMessage("§2[§aStartup§2]§e Loading §aenchants file§e...");
        itemf = new ConfigFile(new File(dir + File.separator + "items.yml"));
        itemf.data.deserialize(Items.class);
        boolean saveIf = false;
        for (Enchantment e : Enchantment.values()) {
            if (!enchants.containsKey(e.getName())) {
                enchants.put(e.getName(), newArrayList(e.getName().toLowerCase().replace("_", "")));
                saveIf = true;
            }
        }
        if (saveIf)
            itemf.save();
        if (backend == BackendType.FILE) {
            cs.sendMessage("§2[§aStartup§2]§e Loading §aFILE§e backend for §aplayer data storage§e...");
            if (Config.purgePF) {
                Config.purgePF = false;
                kf.save();
                if (new File(dir + File.separator + PlayerFile.file).delete())
                    cs.sendMessage("§2[§aStartup§2]§b Purged player file.");
                else
                    cs.sendMessage("§2[§aStartup§2]§c Failed to purge player file.");
            }
            pf = new ConfigFile(new File(dir + File.separator + PlayerFile.file));
        } else if (backend == BackendType.MYSQL) {
            cs.sendMessage("§2[§aStartup§2]§e Loading §aMySQL§e backend for §aplayer data storage§e...");
            if (Config.purgePF) {
                Config.purgePF = false;
                kf.save();
                if (mysql.command("DROP TABLE " + mysql.table))
                    cs.sendMessage("§2[§aStartup§2]§b Dropped " + mysql.table + " table.");
                else
                    cs.sendMessage("§2[§aStartup§2]§c Failed to drop " + mysql.table + " table.");
            }
            mysql.command("CREATE TABLE IF NOT EXISTS " + mysql.table + " (uuid TEXT, `key` LONGTEXT, `value` LONGTEXT)");
            pf = new ConfigFile(mysql, mysql.table, "key", "value");
            loadPlayerConfig(null);
            Bukkit.getOnlinePlayers().forEach((p) -> loadPlayerConfig(p.getUniqueId()));
        }
        cs.sendMessage("§2[§aStartup§2]§e Loading §aReflectionAPI§e...");
        Reflection.init();
        if (Reflection.ver.isAbove(v1_8))
            tp = new ProtocolImpl();
        else if (Reflection.ver != UNKNOWN)
            tp = new ProtocolLegacyImpl();
        else
            Config.forceReducedMode = true;
        if (!Config.forceReducedMode)
            startPacketAPI();
        cs.sendMessage("§2[§aStartup§2]§e Loading §aAnimationAPI§e...");
        AnimationAPI.init();
        ConfigSerialization.getInterfaceBasedClasses().put(ItemStack.class, Reflection.getOBCClass("inventory.CraftItemStack"));
        if (!forceReducedMode) {
            cs.sendMessage("§2[§aStartup§2]§e Starting SpigotLib in §afully compatible§e mode, starting " +
                    "Offline player management, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI...");
            WrapperFactory.init();
            PacketInType.init();
            PacketOutType.init();
            ChatAPI.init();
            if (ver.isAbove(v1_8))
                for (Player p : Bukkit.getOnlinePlayers())
                    ScoreboardAPI.playerJoin(p);
        } else {
            cs.sendMessage("§2[§aStartup§2]§e Starting SpigotLib in §csemi compatible mode§e, skipping the load of " +
                    "PacketAPI, Offline player management, ChatAPI, TitleAPI, NBTApi, ScoreboardAPI.");
        }
        cs.sendMessage("§2[§aStartup§2]§e Preparing §aPlaceholderAPI§e and §aVault§e hooks...");
        VariableAPI.phaHook = pm.getPlugin("PlaceholderAPI") != null && Config.phaHook;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top == null || top.getHolder() == null || !(top.getHolder() instanceof CustomGUI))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() == top)
            try {
                ((CustomGUI) top.getHolder()).onClick(e.getSlot(), e.isRightClick(), e.isShiftClick());
            } catch (Throwable err) {
                Player plr = (Player) e.getWhoClicked();
                error(plr.hasPermission("spigotlib.debug") ? plr : cs, err, "SpigotLib", "gyurix");
            }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top == null || top.getHolder() == null || !(top.getHolder() instanceof CustomGUI))
            return;
        try {
            ((CustomGUI) top.getHolder()).onClose();
        } catch (Throwable err) {
            Player plr = (Player) e.getPlayer();
            error(plr.hasPermission("spigotlib.debug") ? plr : cs, err, "SpigotLib", "gyurix");
        }

    }

    public void onLoad() {
        pl = this;
        try {
            srv = getServer();
            cs = srv.getConsoleSender();
            if (cs == null)
                return;
            pm = srv.getPluginManager();
            msg = srv.getMessenger();
            sm = srv.getServicesManager();
            sch = srv.getScheduler();
            js = new ScriptEngineManager().getEngineByName("JavaScript");
            dir = getDataFolder();
            pluginsF = Reflection.getField(pm.getClass(), "plugins");
            lookupNamesF = Reflection.getField(pm.getClass(), "lookupNames");
        } catch (Throwable e) {
            log(this, "§cFailed to get default Bukkit managers :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            ConfigHook.registerSerializers();
            ConfigHook.registerVariables();
        } catch (Throwable e) {
            log(this, "§cFailed to load config hook :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            load();
        } catch (Throwable e) {
            log(this, "Failed to load plugin, trying to reset the config...");
            error(cs, e, "SpigotLib", "gyurix");
            resetConfig();
        }
    }

    public void onDisable() {
        log(this, "§4[§cShutdown§4]§e Collecting plugins depending on SpigotLib...");
        ArrayList<Plugin> depend = new ArrayList<>();
        for (Plugin p : pm.getPlugins()) {
            PluginDescriptionFile pdf = p.getDescription();
            if (pdf.getDepend() != null && pdf.getDepend().contains("SpigotLib") || pdf.getSoftDepend() != null && pdf.getSoftDepend().contains("SpigotLib"))
                depend.add(p);
        }
        log(this, "§4[§cShutdown§4]§e Saving players...");
        if (backend == BackendType.FILE)
            pf.saveNoAsync();
        else if (backend == BackendType.MYSQL) {
            ArrayList<String> list = new ArrayList<>();
            for (String s : pf.getStringKeyList()) {
                pf.subConfig(s, "uuid='" + s + "'").mysqlUpdate(list, null);
            }
            pf.db.batchNoAsync(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        log(this, "§4[§cShutdown§4]§e Unloading plugins depending on SpigotLib...");
        for (Plugin p : depend) {
            log(this, "§4[§cShutdown§4]§e Unloading plugin §f" + p.getName() + "§e...");
            unloadPlugin(p);
        }
        pf = null;
        if (TPSMeter.meter != null) {
            log(this, "§4[§cShutdown§4]§e Stopping TPSMeter...");
            TPSMeter.meter.cancel(true);
        }
        if (!forceReducedMode) {
            log(this, "§4[§cShutdown§4]§e Stopping PacketAPI...");
            try {
                tp.close();
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }
        log(this, "§4[§cShutdown§4]§e Stopping AnimationAPI...");
        AnimationAPI.stopRunningAnimations(this);
        if (!forceReducedMode && ver.isAbove(v1_8)) {
            log(this, "§4[§cShutdown§4]§e Stopping ScoreboardAPI...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerBars pbs = ScoreboardAPI.sidebars.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
                pbs = ScoreboardAPI.nametags.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
                pbs = ScoreboardAPI.tabbars.remove(p.getName());
                for (ScoreboardBar sb : pbs.loaded)
                    sb.unload(p);
            }
        }
        log(this, "§4[§cShutdown§4]§e Stopping CommandAPI...");
        CustomCommandMap.unhook();
        log(this, "§4[§cShutdown§4]§a The SpigotLib has shutted down properly.");
    }

    public void onEnable() {
        PluginCommand cmd = getCommand("sl");
        SpigotLibCommands exec = new SpigotLibCommands();
        cmd.setExecutor(exec);
        cmd.setTabCompleter(exec);
        if (cs == null)
            onLoad();
        else
            cm = new CustomCommandMap();
        if (!forceReducedMode) {
            cs.sendMessage("§2[§aStartup§2]§e Initializing §aoffline player manager§e...");
            SU.pm.registerEvents(SU.tp, this);
            initOfflinePlayerManager();
        }
        pm.registerEvents(this, this);
        BungeeAPI.enabled = Config.BungeeAPI.forceEnable || srv.spigot().getConfig().getBoolean("settings.bungeecord");
        if (BungeeAPI.enabled) {
            cs.sendMessage("§2[§aStartup§2]§e Starting §aBungeeAPI§e...");
            msg.registerOutgoingPluginChannel(this, "BungeeCord");
            msg.registerIncomingPluginChannel(this, "BungeeCord", new BungeeAPI());
        } else {
            cs.sendMessage("§2[§aStartup§2]§e Your server is §cnot connected§e to a BungeeCord server, §cskipping BungeeAPI§e load...");
        }
        if (backend == BackendType.MYSQL) {
            cs.sendMessage("§2[§aStartup§2]§e Loading player data of online players from the MySQL...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                loadPlayerConfig(p.getUniqueId());
            }
        }
        vault = pm.getPlugin("Vault") != null;
        if (!vault)
            cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is not present, skipping hook...");
        else {
            if (vaultHookType == NONE) {
                cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is present, but the hook is disabled in config, so skipping hook...");
            }
            if (vaultHookType == USER) {
                cs.sendMessage("§2[§aStartup§2]§e The plugin §aVault§e is present, hooking to it as §aEconomy USER§e...");
                RegisteredServiceProvider<Economy> rspEcon = srv.getServicesManager().getRegistration(Economy.class);
                if (rspEcon != null)
                    econ = rspEcon.getProvider();
                if (EconomyAPI.migrate) {
                    SU.cs.sendMessage("§2[§aStartup§2]§e Migrating economy data from old Economy " + econ.getName() + "... ");
                    vaultHookType = NONE;
                    for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                        EconomyAPI.setBalance(op.getUniqueId(), new BigDecimal(econ.getBalance(op)));
                        log(this, "Done player " + op.getName());
                    }
                    vaultHookType = PROVIDER;
                    EconomyAPI.migrate = false;
                    log(this, "Finished data migration, please restart the server!");
                    setEnabled(false);
                    return;
                }
            }
        }
        sch.scheduleSyncDelayedTask(this, () -> {
            if (!Config.forceReducedMode && schedulePacketAPI)
                startPacketAPI();
            if (vault) {
                if (vaultHookType == USER) {
                    RegisteredServiceProvider<Economy> rspEcon = srv.getServicesManager().getRegistration(Economy.class);
                    if (rspEcon != null)
                        econ = rspEcon.getProvider();
                }
                RegisteredServiceProvider rspPerm = srv.getServicesManager().getRegistration(Permission.class);
                if (rspPerm != null)
                    perm = (Permission) rspPerm.getProvider();
                RegisteredServiceProvider rspChat = srv.getServicesManager().getRegistration(Chat.class);
                if (rspChat != null)
                    chat = (Chat) rspChat.getProvider();
            }
            if (TPSMeter.enabled) {
                cs.sendMessage("§2[§aStartup§2]§e Starting TPSMeter...");
                Config.tpsMeter.start();
            }
            cs.sendMessage("§2[§aStartup§2]§e Starting PlaceholderAPI hook...");
            VariableAPI.init();
            cs.sendMessage("§2[§aStartup§2]§a Started SpigotLib §e" + version + "§a properly.");
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player plr = e.getPlayer();
        UUID uid = plr.getUniqueId();
        savePlayerConfig(uid);
        unloadPlayerConfig(uid);
        AnimationAPI.stopRunningAnimations(plr);
        if (!forceReducedMode && ver.isAbove(v1_8))
            ScoreboardAPI.playerLeave(plr);
        SignGUI sg = SignGUI.openSignGUIs.remove(plr.getName());
        if (sg != null)
            sg.cancel();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player plr = e.getPlayer();

        if (!forceReducedMode && ver.isAbove(v1_8))
            ScoreboardAPI.playerJoin(plr);
    }

    @EventHandler
    public void onPluginUnload(PluginDisableEvent e) {
        Plugin pl = e.getPlugin();
        AnimationAPI.stopRunningAnimations(pl);
        if (tp != null) {
            tp.unregisterIncomingListener(pl);
            tp.unregisterOutgoingListener(pl);
        }
        SU.pf.data.unWrapAll();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (ver != UNKNOWN) {
            UUID id = e.getUniqueId();
            if (backend == BackendType.MYSQL) {
                loadPlayerConfig(id);
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (Config.disableWeatherChange)
            e.setCancelled(true);
    }

    @EventHandler
    public void registerServiceEvent(ServiceRegisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        log(this, "Register service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                chat = (Chat) p.getProvider();
                break;
            case "net.milkbowl.vault.economy.Economy":
                econ = (Economy) p.getProvider();
                break;
            case "net.milkbowl.vault.permission.Permission":
                perm = (Permission) p.getProvider();
                break;
        }
    }

    public void resetConfig() {
        try {
            File oldConf = new File(dir + File.separator + "config.yml");
            File backupConf = new File(dir + File.separator + "config.yml.bak");
            if (backupConf.exists())
                backupConf.delete();
            oldConf.renameTo(backupConf);
            File oldLang = new File(dir + File.separator + "lang.yml");
            File backupLang = new File(dir + File.separator + "lang.yml.bak");
            if (backupLang.exists()) {
                backupLang.delete();
            }
            oldLang.renameTo(backupLang);
        } catch (Throwable e) {
            log(this, "§cFailed to reset the config :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
            return;
        }
        try {
            load();
        } catch (Throwable e) {
            log(this, "§cFailed to load plugin after config reset :-( The plugin is shutting down...");
            error(cs, e, "SpigotLib", "gyurix");
            pm.disablePlugin(this);
        }
    }

    public void startPacketAPI() {
        cs.sendMessage("§2[§aStartup§2]§e Starting PacketAPI...");
        try {
            tp.init();
        } catch (Throwable e) {
            if (schedulePacketAPI) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
                cs.sendMessage("§2[§aStartup§2]§c Failed to start PacketAPI.");
            }
            schedulePacketAPI = true;
            cs.sendMessage("§2[§aStartup§2]§e Scheduled PacketAPI startup.");
        }
    }

    @EventHandler
    public void unregisterServiceEvent(ServiceUnregisterEvent e) {
        RegisteredServiceProvider p = e.getProvider();
        String sn = p.getService().getName();
        log(this, "Unregister service - " + sn);
        switch (sn) {
            case "net.milkbowl.vault.chat.Chat":
                chat = null;
                break;
            case "net.milkbowl.vault.economy.Economy":
                econ = null;
                break;
            case "net.milkbowl.vault.permission.Permission":
                perm = null;
                break;
        }
    }
}

