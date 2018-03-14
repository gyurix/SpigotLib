package gyurix.animation;

import gyurix.animation.effects.FramesEffect;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static gyurix.api.VariableAPI.fillVariables;

public class AnimationRunnable implements Runnable {
    public final Animation a;
    public final String name;
    public final Plugin pl;
    public final Player plr;
    protected final HashMap<String, HashMap<String, CustomEffect>> effects = new HashMap();
    private final FramesEffect f;
    private final AnimationUpdateListener listener;
    public String text = "§cERROR";
    protected ScheduledFuture future;

    protected AnimationRunnable(Plugin pl, Animation a, String name, Player plr, AnimationUpdateListener listener) {
        this.pl = pl;
        this.a = a;
        this.name = name;
        this.plr = plr;
        this.listener = listener;
        for (Entry<String, HashMap<String, CustomEffect>> e : a.effects.entrySet()) {
            HashMap<String, CustomEffect> map = new HashMap<>();
            effects.put(e.getKey(), map);
            for (Entry<String, CustomEffect> e2 : e.getValue().entrySet())
                map.put(e2.getKey(), e2.getValue().clone());
        }
        f = (FramesEffect) effects.get("frame").get("main").clone();
        run();
    }

    public boolean isRunning() {
        return future != null;
    }

    @Override
    public void run() {
        future = null;
        try {
            text = fillVariables(SU.optimizeColorCodes(f.next("")), plr, this);
            if (!listener.onUpdate(this, text) || f.delay >= Integer.MAX_VALUE)
                return;
        } catch (Throwable e) {
            String main = pl.getDescription().getMain();
            SU.error(SU.cs, e, pl.getName(), main.substring(0, main.lastIndexOf(".")));
        }
        if (f.delay < 1)
            f.delay = 1;
        future = AnimationAPI.pool.schedule(this, f.delay, TimeUnit.MILLISECONDS);
    }

    public boolean stop() {
        if (future == null)
            return false;
        future.cancel(true);
        future = null;
        return true;
    }
}

