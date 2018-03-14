package gyurix.scoreboard;

import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static gyurix.scoreboard.ScoreboardDisplayMode.INTEGER;

public abstract class ScoreboardBar {
    public final HashMap<String, BarData> active = new HashMap<>();
    public final BarData currentData;
    public final HashMap<String, BarData> loaded = new HashMap<>();
    public final Object showPacket, hidePacket;
    public final String teamNamePrefix;

    /**
     * Construct new ScoreboardBar
     *
     * @param barname        - The name of this ScoreboardBar
     * @param teamNamePrefix - The prefix of the Scoreboard team packets
     * @param displaySlot    - The slot of this ScoreboardBar (0: list, 1: sidebar, 2: below name)
     */
    public ScoreboardBar(String barname, String teamNamePrefix, int displaySlot) {
        this.currentData = new BarData(barname, barname, INTEGER, true);
        this.teamNamePrefix = teamNamePrefix;
        showPacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, barname);
        hidePacket = PacketOutType.ScoreboardDisplayObjective.newPacket(displaySlot, "");
    }

    /**
     * Reactivates this ScoreboardBar for the given player, if it is loaded, but deactivated currently
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar was not active for the given player before
     */
    public boolean activate(Player plr) {
        if (active.containsKey(plr.getName()))
            return false;
        BarData bd = loaded.get(plr.getName());
        if (bd == null)
            return false;
        BarData newBd = currentData.clone();
        newBd.update(plr, bd);
        loaded.put(plr.getName(), newBd);
        active.put(plr.getName(), newBd);
        if (currentData.visible)
            SU.tp.sendPacket(plr, showPacket);
        return true;
    }

    /**
     * Deactivates this ScoreboardBar for the given player, but keeps it loaded and ready to fast switch back
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar was active for the given player before
     */
    public boolean deActivate(Player plr) {
        return active.remove(plr.getName()) != null;
    }

    /**
     * Removes the data stored about the given players loaded state (useful for handling player quits)
     *
     * @param plr - Target player
     * @return True if this ScoreboardBar was loaded for the player before
     */
    public boolean drop(Player plr) {
        if (loaded.remove(plr.getName()) == null)
            return false;
        active.remove(plr.getName());
        return true;
    }

    /**
     * Get the display mode of the Scoreboard
     *
     * @return The scoreboards display mode
     */
    public ScoreboardDisplayMode getDisplayMode() {
        return currentData.displayMode;
    }

    /**
     * Sets the display mode of this Scoreboard, it has no effect on sidebar.
     *
     * @param mode - The new displaymode
     */
    public void setDisplayMode(ScoreboardDisplayMode mode) {
        if (currentData.displayMode == mode)
            return;
        currentData.displayMode = mode;
    }

    /**
     * Gets the title of this scoreboard bar
     *
     * @return - The title of this scoreboard bar
     */
    public String getTitle() {
        return currentData.title;
    }

    /**
     * Sets the title of this scoreboard bar.
     *
     * @param newtitle - The new title
     */
    public void setTitle(String newtitle) {
        newtitle = SU.setLength(newtitle, 32);
        if (currentData.title.equals(newtitle))
            return;
        currentData.title = newtitle;
        update();
    }

    /**
     * Checks if this ScoreboardBar is active for the given player or not
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar is active for the given player
     */
    public boolean isActive(Player plr) {
        return active.containsKey(plr.getName());
    }

    /**
     * Checks if this ScoreboardBar is already loaded for the given player or not
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar is already loaded for the given player
     */
    public boolean isLoaded(Player plr) {
        return loaded.containsKey(plr.getName());
    }

    /**
     * Check if this scoreboard bar is visible or not
     *
     * @return The scoreboard bars visibility
     */
    public boolean isVisible() {
        return currentData.visible;
    }

    /**
     * Toggles the visibility of this scoreboard bar
     *
     * @param visible - The new visibility state
     */
    public void setVisible(boolean visible) {
        if (visible != currentData.visible) {
            currentData.visible = visible;
            update();
        }
    }

    /**
     * Attempts to load this ScoreboardBar for the given player
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar hasn't been loaded for the player before
     */
    public boolean load(Player plr) {
        if (loaded.containsKey(plr.getName()))
            return false;
        BarData bd = currentData.clone();
        bd.load(plr);
        loaded.put(plr.getName(), bd);
        if (currentData.visible)
            SU.tp.sendPacket(plr, showPacket);
        active.put(plr.getName(), bd);
        return true;
    }

    /**
     * Attempts to unload this ScoreboardBar for the given player
     *
     * @param plr - Target Player
     * @return True if this ScoreboardBar has been loaded for the player before
     */
    public boolean unload(Player plr) {
        BarData bd = loaded.remove(plr.getName());
        if (bd == null)
            return false;
        active.remove(plr.getName());
        bd.unload(plr);
        return true;
    }

    public void update() {
        Iterator<Map.Entry<String, BarData>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BarData> e = it.next();
            BarData newBd = currentData.clone();
            Player plr = Bukkit.getPlayer(e.getKey());
            if (plr == null)
                it.remove();
            newBd.update(plr, e.getValue());
            if (!e.getValue().visible && newBd.visible)
                SU.tp.sendPacket(plr, showPacket);
            if (e.getValue().visible && !newBd.visible)
                SU.tp.sendPacket(plr, hidePacket);
            e.setValue(newBd);
        }
    }
}

