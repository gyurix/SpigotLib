package gyurix.animation;

import gyurix.animation.Animation.AnimationSerializer;
import gyurix.animation.effects.*;
import gyurix.api.VariableAPI;
import gyurix.api.VariableAPI.VariableHandler;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An API used for running Animations
 */
public final class AnimationAPI {
    protected static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(Config.animationApiThreads);
    /**
     * Custom effect name - Custom effect class mapping
     */
    public static HashMap<String, Class> effects = new HashMap();
    private static HashMap<Plugin, HashMap<String, HashSet<AnimationRunnable>>> runningAnimations = new HashMap();

    /**
     * Initializes the AnimationAPI:
     * - registers built in custom effects
     * - registers a CustomEffectHandler for every custom effect
     * - registers the AnimationSerializer
     */
    public static void init() {
        effects.put("scroller", ScrollerEffect.class);
        effects.put("blink", BlinkEffect.class);
        effects.put("frame", FramesEffect.class);
        effects.put("flame", FlameEffect.class);
        effects.put("rainbow", RainbowEffect.class);
        for (String key : effects.keySet())
            VariableAPI.handlers.put(key, new CustomEffectHandler(key));
        ConfigSerialization.getSerializers().put(Animation.class, new AnimationSerializer());
    }

    /**
     * Starts the execution of an Animation
     *
     * @param pl       - Plugin which wants to start the Animation, used for auto stopping it, when the plugin is unloaded
     * @param a        - The runnable Animation
     * @param name     - The name under which the Animation should be executed, this name can help for the plugin
     *                 to determine what kind of animation is actually running
     * @param plr      - The player to who this Animation should be linked. Every placeholder in the Animation will
     *                 be filled with his data
     * @param listener - The AnimationUpdateListener, which should handle every state change in the animation
     * @return The AnimationRunnable, usable for cancelling the execution of the Animation at any time
     */
    public static AnimationRunnable runAnimation(Plugin pl, Animation a, String name, Player plr, AnimationUpdateListener listener) {
        if (pl == null || a == null || plr == null || listener == null)
            return null;
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.computeIfAbsent(pl, k -> new HashMap<>());
        HashSet<AnimationRunnable> ars = map.computeIfAbsent(plr.getName(), k -> new HashSet<>());
        AnimationRunnable ar = new AnimationRunnable(pl, a, name, plr, listener);
        ars.add(ar);
        return ar;
    }

    /**
     * Force stops the given AnimationRunnable
     *
     * @param ar - The stoppable AnimationRunnable
     */
    public static void stopRunningAnimation(AnimationRunnable ar) {
        if (ar == null)
            return;
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.get(ar.pl);
        if (map == null || map.isEmpty())
            return;
        HashSet<AnimationRunnable> ars = map.get(ar.plr.getName());
        ars.remove(ar);
        ar.stop();
    }

    /**
     * Force stops every AnimationRunnable linked to the given player.
     * This method is automatically executed by SpigotLib, when player logs out.
     *
     * @param plr - Target player
     */
    public static void stopRunningAnimations(Player plr) {
        if (plr == null)
            return;
        for (HashMap<String, HashSet<AnimationRunnable>> map : runningAnimations.values()) {
            HashSet<AnimationRunnable> ars = map.remove(plr.getName());
            if (ars != null)
                for (AnimationRunnable ar : ars)
                    if (ar.future != null)
                        ar.future.cancel(true);
        }
    }

    /**
     * Force stops every AnimationRunnable linked to the given plugin.
     * This method is automatically executed by SpigotLib, when a plugin is unloaded.
     *
     * @param pl - Target plugin
     */
    public static void stopRunningAnimations(Plugin pl) {
        if (pl == null)
            return;
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.remove(pl);
        if (map == null || map.isEmpty())
            return;
        for (HashSet<AnimationRunnable> ars : map.values()) {
            for (AnimationRunnable ar : ars)
                ar.stop();
        }
    }

    /**
     * Force stops every AnimationRunnable linked to the given plugin and to the given player.
     * This method helps plugins to stop their AnimationRunnables without keeping their instances.
     *
     * @param pl  - Target plugin
     * @param plr - Target Player
     */
    public static void stopRunningAnimations(Plugin pl, Player plr) {
        if (pl == null || plr == null)
            return;
        HashMap<String, HashSet<AnimationRunnable>> map = runningAnimations.get(pl);
        if (map == null || map.isEmpty())
            return;
        HashSet<AnimationRunnable> ars = map.remove(plr.getName());
        if (ars != null)
            for (AnimationRunnable ar : ars)
                ar.stop();
    }

    /**
     * The CustomEffectHandler class is used for making custom effects in Animations work as VariableAPI variables
     * for easier usage
     */
    public static class CustomEffectHandler implements VariableHandler {
        public final String name;

        public CustomEffectHandler(String name) {
            this.name = name;
        }

        @Override
        public Object getValue(Player plr, ArrayList<Object> inside, Object[] oArgs) {
            AnimationRunnable ar = (AnimationRunnable) oArgs[0];
            String[] d = StringUtils.join(inside, "").split(":", 2);
            CustomEffect effect = ar.effects.get(name).get(d[0]);
            if (effect != null) {
                String text = d.length <= 1 ? effect.getText() : d[1];
                return effect.next(VariableAPI.fillVariables(text, plr, oArgs));
            }
            SU.log(Main.pl, "The given " + name + " name (" + d[0] + ") is invalid " + name + " name in animation ");
            return "?";
        }
    }

}

