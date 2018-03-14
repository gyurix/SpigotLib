package gyurix.api;

import gyurix.chat.ChatTag;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle;
import gyurix.spigotutils.NullUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

import static gyurix.protocol.wrappers.outpackets.PacketPlayOutTitle.TitleAction.*;
import static gyurix.spigotlib.SU.tp;

/**
 * TitleAPI - API for managing title, subtitle and actionbar messages for server versions 1.8.8 and later
 */
public class TitleAPI {

    /**
     * Clears the title message of the given collection of players
     *
     * @param players - The collection of players whose title bar should be cleared
     */
    public static void clear(Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(CLEAR, null, 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Clears the title message of the given players
     *
     * @param players - The players whose title bar should be cleared
     */
    public static void clear(Player... players) {
        Object packet = new PacketPlayOutTitle(CLEAR, null, 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Resets the title message of the given collection of players
     *
     * @param players - The players whose title bar should be reset
     */
    public static void reset(Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(RESET, null, 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Resets the title message of the given players
     *
     * @param players - The players whose title bar should be reset
     */
    public static void reset(Player... players) {
        Object packet = new PacketPlayOutTitle(RESET, null, 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Sends a title message to the given collection of players
     *
     * @param title    - Title text of the title message
     * @param subtitle - Subtitle text of the title message
     * @param fadeIn   - Fade in time in ticks (1 tick = 0.05 sec)
     * @param showtime - Show time in ticks
     * @param fadeOut  - Fade out time in ticks
     * @param players  - Receivers of the title message
     */
    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Collection<? extends Player> players) {
        setShowTime(fadeIn, showtime, fadeOut, players);
        setSubTitle(NullUtils.to0(subtitle), players);
        setTitle(title, players);
    }

    /**
     * Sends a title message to the given players
     *
     * @param title    - Title text of the title message
     * @param subtitle - Subtitle text of the title message
     * @param fadeIn   - Fade in time in ticks (1 tick = 0.05 sec)
     * @param showtime - Show time in ticks
     * @param fadeOut  - Fade out time in ticks
     * @param players  - Receivers of the title message
     */
    public static void set(String title, String subtitle, int fadeIn, int showtime, int fadeOut, Player... players) {
        setShowTime(fadeIn, showtime, fadeOut, players);
        setSubTitle(NullUtils.to0(subtitle), players);
        setTitle(title, players);
    }

    /**
     * Sets the title timings of the given collection of players
     *
     * @param fadein  - Fade in time in ticks (1 tick = 0.05 sec)
     * @param show    - Show time in ticks
     * @param fadeout - Fade out time in ticks
     * @param players - Players whose title timings should be changed
     */
    public static void setShowTime(int fadein, int show, int fadeout, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }


    /**
     * Sets the title timings of the given players
     *
     * @param fadein  - Fade in time in ticks (1 tick = 0.05 sec)
     * @param show    - Show time in ticks
     * @param fadeout - Fade out time in ticks
     * @param players - Players whose title timings should be changed
     */
    public static void setShowTime(int fadein, int show, int fadeout, Player... players) {
        Object packet = new PacketPlayOutTitle(TIMES, null, fadein, show, fadeout).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Sets the subtitle of the given collection of players.
     * Subtitle will only become visible when the title is shown using the set title method.
     * If you only want to show a subtitle without any title, you should call the setTitle method
     * with empty String passed as title argument
     *
     * @param subtitle - The new subtitle of the title message
     * @param players  - Players whose subtitle should be changed
     */
    public static void setSubTitle(String subtitle, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Sets the subtitle of the given players.
     * Subtitle will only become visible when the title is shown using the set title method.
     * If you only want to show a subtitle without any title, you should call the setTitle method
     * with empty String passed as title argument
     *
     * @param subtitle - The new subtitle of the title message
     * @param players  - Players whose subtitle should be changed
     */
    public static void setSubTitle(String subtitle, Player... players) {
        Object packet = new PacketPlayOutTitle(SUBTITLE, ChatTag.fromColoredText(subtitle), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }

    /**
     * Sets the title of the given collection of players and shows the title message.
     * Make sure to set the subtitle and the title timings before using this method.
     *
     * @param title   - The showable title message
     * @param players - Receivers of the title message
     */
    public static void setTitle(String title, Collection<? extends Player> players) {
        Object packet = new PacketPlayOutTitle(TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        players.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Sets the title of the given players and shows the title message.
     * Make sure to set the subtitle and the title timings before using this method.
     *
     * @param title   - The showable title message
     * @param players - Receivers of the title message
     */
    public static void setTitle(String title, Player... players) {
        Object packet = new PacketPlayOutTitle(TITLE, ChatTag.fromColoredText(title), 0, 0, 0).getVanillaPacket();
        for (Player p : players)
            tp.sendPacket(p, packet);
    }
}

