package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TPSMeter implements Runnable {
    /**
     * Update the servers tps metrics result in every here configured milliseconds
     */
    public static long checkTime = 10000;
    /**
     * Make TPS meter toggleable in the config
     */
    public static boolean enabled = true;
    /**
     * The tps limit, if the servers tps is bellow this value, then you will see a warning in the console
     */
    public static int limit = 15;
    /**
     * The instance of the TPS meter future, it is stopped on SpigotLib shutdown
     */
    @ConfigOptions(serialize = false)
    public static ScheduledFuture meter;
    /**
     * Amount of detected 0 TPS before crash is reported
     */
    public static int reportCrashAfter = 4;
    /**
     * Ticks elapsed from the last tps metrics result update
     */
    @ConfigOptions(serialize = false)
    public static int ticks;
    /**
     * The current tps value of the server
     */
    @ConfigOptions(serialize = false)
    public static double tps = 20.0;
    /**
     * Amount of detected 0 TPS in a row.
     */
    @ConfigOptions(serialize = false)
    public static int zeroTpsCount = 0;

    @Override
    public void run() {
        tps = ticks * 1000.0 / checkTime;
        ticks = 0;
        if (tps < limit)
            SU.cs.sendMessage("§9[§b TPS Meter §9]§e The servers TPS is bellow §c" + tps + "§e, is it lagging or crashed?");
        if (tps == 0) {
            ++zeroTpsCount;
            if (zeroTpsCount == reportCrashAfter) {
                StringBuilder sb = new StringBuilder();
                sb.append("============== SpigotLib Crash Reporter ==============\n" +
                        "Your server has been detected having 0 TPS " + reportCrashAfter + " times a row, which means that with high probability it crashed / frozen down. " +
                        "This crash report will help for your developers to detect what's causing the crash. This is NOT an error in SpigotLib, but it's one of it's " +
                        "features, so only contact gyuriX for fixing the error caused this crash if you have budget for it.\n" +
                        "\n");
                TreeMap<Thread, StackTraceElement[]> map = new TreeMap<>(new java.util.Comparator<Thread>() {
                    @Override
                    public int compare(Thread o1, Thread o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                map.putAll(Thread.getAllStackTraces());
                for (Map.Entry<Thread, StackTraceElement[]> e : map.entrySet()) {
                    sb.append("\n \n \n§e=====@ §bTHREAD " + e.getKey().getName() + "(§fstate=" + e.getKey().getState() + ", priority=" + e.getKey().getPriority() + "§b)" + " §e@=====");
                    int i = 0;
                    for (StackTraceElement el : e.getValue()) {
                        sb.append("\n§c #").append(++i)
                                .append(": §eLINE §a").append(el.getLineNumber())
                                .append("§e in FILE §6").append(el.getFileName())
                                .append("§e (§7").append(el.getClassName())
                                .append("§e.§b").append(el.getMethodName())
                                .append("§e)");
                    }
                }
                sb.append("\n======================================================");
                SU.cs.sendMessage(sb.toString());
                for (Plugin p : SU.pm.getPlugins()) {
                    p.onDisable();
                }
            }
        } else
            zeroTpsCount = 0;

    }

    public void start() {
        meter = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, checkTime, checkTime, TimeUnit.MILLISECONDS);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, () -> ++ticks, 0, 1);
    }

}

