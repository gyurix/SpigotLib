package gyurix.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

import static gyurix.protocol.event.PacketOutType.PlayerListHeaderFooter;
import static gyurix.spigotlib.ChatAPI.TextToJson;
import static gyurix.spigotlib.ChatAPI.toICBC;
import static gyurix.spigotlib.SU.tp;

/**
 * TabAPI - Used for setting global and local player list headers and footers
 */
public class TabAPI {
    /**
     * Sets the player list header and footer for every online player
     *
     * @param header - The new header
     * @param footer - The new footer
     */
    public static void setGlobalHeaderFooter(String header, String footer) {
        setLocalHeaderFooter(header, footer, Bukkit.getOnlinePlayers());
    }

    /**
     * Sets the player list header and footer for the given collection of players
     *
     * @param header - The new header
     * @param footer - The new footer
     * @param plrs   - Target players
     */
    public static void setLocalHeaderFooter(String header, String footer, Collection<? extends Player> plrs) {
        Object headerComponent = toICBC(TextToJson(header));
        Object footerComponent = toICBC(TextToJson(footer));
        Object packet = PlayerListHeaderFooter.newPacket(headerComponent, footerComponent);
        plrs.forEach((p) -> tp.sendPacket(p, packet));
    }

    /**
     * Sets the player list header and footer for the given players
     *
     * @param header - The new header
     * @param footer - The new footer
     * @param plrs   - Target players
     */
    public static void setLocalHeaderFooter(String header, String footer, Player... plrs) {
        Object headerComponent = toICBC(TextToJson(header));
        Object footerComponent = toICBC(TextToJson(footer));
        Object packet = PlayerListHeaderFooter.newPacket(headerComponent, footerComponent);
        for (Player p : plrs)
            tp.sendPacket(p, packet);
    }
}

