package gyurix.spigotlib;

import gyurix.debug.Debugger;
import gyurix.economy.EconomyAPI;
import gyurix.mysql.MySQLDatabase;
import gyurix.spigotutils.BackendType;
import gyurix.spigotutils.TPSMeter;

/**
 * Memory represantation of the SpigotLib's configuration (config.yml)
 */
public class Config {
    /**
     * Allow access to every SpigotLib command for the plugins author,
     * so he could debug other plugins quickly without needing to ask you
     * for changing his perms all the time
     */
    public static boolean allowAllPermsForAuthor;
    /**
     * Amount of async threads used in AnimationAPI for running animations smoothly
     */
    public static int animationApiThreads;
    /**
     * BungeeAPI related configurations
     */
    public static BungeeAPI bungee;
    /**
     * Toggle debug messages in the plugin
     */
    public static Debugger debug = new Debugger();
    /**
     * Default server language used in language files
     */
    public static String defaultLang = "en";
    /**
     * Disable weather changes on the entire server, useful for testing
     */
    public static boolean disableWeatherChange;
    /**
     * EconomyAPI related configurations
     */
    public static EconomyAPI economy;
    /**
     * Disable version sensitive features of SpigotLib, like PacketAPI, ScoreboardAPI for
     * fixing compatibility issues with older server versions
     */
    public static boolean forceReducedMode;
    /**
     * Automatically hook to PlaceholderAPI if it's added to the server
     */
    public static boolean phaHook;
    /**
     * Allow using Javascript engine for players using /sl commands.
     * This feature might be abused by players, and give them unpredictable amount of power,
     * so it's default by default for security
     */
    public static boolean playerEval;
    /**
     * Player data storage related settings
     */
    public static PlayerFile playerFile;
    /**
     * Purge player data storage in the next server restart
     */
    public static boolean purgePF;
    /**
     * Hide every error handled through the SpigotLib error handler
     */
    public static boolean silentErrors;
    /**
     * TPS meter related settings
     */
    public static TPSMeter tpsMeter;

    /**
     * BungeeAPI related settings.
     */
    public static class BungeeAPI {
        /**
         * Amount of ticks how often the current server name should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int currentServerName;
        /**
         * Force enable BungeeAPI, without checking if the Spigot is connected to BungeeCord or not.
         * Use this option only if the automatic BungeeCord detection does not detect the BungeeCord.
         */
        public static boolean forceEnable;
        /**
         * Querry players real IP address from the Bungee, when he joins
         */
        public static boolean ipOnJoin;
        /**
         * Amount of ticks how often the player counts should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int playerCount;
        /**
         * Amount of ticks how often the player counts should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int playerList;
        /**
         * Amount of ticks how often the IPs of every server should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int serverIP;
        /**
         * Amount of ticks how often the server list should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int servers;
        /**
         * Amount of ticks how often the uuid of every player should be querried from Bungee automatically.
         * 0 = disable feature
         */
        public static int uuidAll;
    }

    public static class PlayerFile {
        public static BackendType backend;
        public static String file = "players.yml";
        public static MySQLDatabase mysql;
    }
}

