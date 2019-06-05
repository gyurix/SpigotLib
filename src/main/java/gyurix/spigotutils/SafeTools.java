package gyurix.spigotutils;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class SafeTools {
  public static void async(SafeRunnable sr) {
    async(detectPlugin(sr), sr);
  }

  private static void async(Plugin pl, SafeRunnable sr) {
    SU.sch.runTaskAsynchronously(pl, of(sr));
  }

  public static <T> void async(SafeCallable<T> callable, SafeConsumer<T> consumer) {
    async(detectPlugin(callable), () -> consumer.accept(of(callable).call()));
  }

  public static <T> void async(SafeConsumer<T> consumer, T data) {
    async(detectPlugin(consumer), () -> consumer.accept(data));
  }

  private static Plugin detectPlugin(Object o) {
    return o == null ? Main.pl : SU.getPlugin(o.getClass());
  }

  public static Runnable of(SafeRunnable sr) {
    return () -> {
      try {
        sr.run();
      } catch (Throwable e) {
        Plugin p = SU.getPlugin(sr.getClass());
        String pln = p == null ? "Server" : p.getName();
        SU.error(SU.cs, e, pln, "gyurix");
      }
    };
  }

  public static <T> CleanCallable<T> of(SafeCallable<T> sc) {
    return () -> {
      try {
        return sc.call();
      } catch (Throwable e) {
        Plugin p = SU.getPlugin(sc.getClass());
        String pln = p == null ? "Server" : p.getName();
        SU.error(SU.cs, e, pln, "gyurix");
      }
      return null;
    };
  }

  public <T> Consumer<T> of(SafeConsumer<T> sc) {
    return (data) -> {
      try {
        sc.accept(data);
      } catch (Throwable e) {
        Plugin p = SU.getPlugin(sc.getClass());
        String pln = p == null ? "Server" : p.getName();
        SU.error(SU.cs, e, pln, "gyurix");
      }
    };
  }

  public interface CleanCallable<T> {
    T call();
  }

  public interface SafeCallable<T> {
    T call() throws Throwable;
  }

  public interface SafeConsumer<T> {
    void accept(T data) throws Throwable;
  }

  public interface SafeRunnable {
    void run() throws Throwable;
  }
}
