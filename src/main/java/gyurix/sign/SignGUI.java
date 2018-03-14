package gyurix.sign;

import gyurix.chat.ChatTag;
import gyurix.protocol.Protocol.PacketInListener;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.inpackets.PacketPlayInUpdateSign;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutBlockChange;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutOpenSignEditor;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutUpdateSign;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static gyurix.chat.ChatTag.fromColoredText;
import static gyurix.spigotlib.Main.pl;
import static gyurix.spigotlib.SU.tp;

/**
 * SignGUI, which can be used
 */
public class SignGUI implements PacketInListener {
    public static final HashMap<String, SignGUI> openSignGUIs = new HashMap<>();
    public final String[] initialLines;
    public final String[] result = new String[]{"", "", "", ""};
    private final BlockLocation bl;
    public SignDataReader dr;
    public Player plr;

    /**
     * Opens a new SignGUI to the given player with empty lines
     *
     * @param plr - Player to which the SignGUI should be opened
     * @param dr  - The DataReader to which the result should be passed
     */
    public SignGUI(Player plr, SignDataReader dr) {
        this(plr, dr, new String[]{"", "", "", ""});
    }

    /**
     * Opens a new SignGUI to the given player with the given default lines
     *
     * @param plr          - Player to which the SignGUI should be opened
     * @param dr           - The DataReader to which the result should be passed
     * @param initialLines - Default lines of the sign
     */
    public SignGUI(Player plr, SignDataReader dr, String[] initialLines) {
        this.plr = plr;
        this.dr = dr;
        this.initialLines = initialLines;
        openSignGUIs.put(plr.getName(), this);
        Location loc = plr.getLocation();
        bl = new BlockLocation(loc.getBlockX(), loc.getBlockY() > 128 ? 0 : 255, loc.getBlockZ());
        tp.sendPacket(plr, new PacketPlayOutBlockChange(bl, 63, (byte) 0));
        tp.sendPacket(plr, new PacketPlayOutUpdateSign(bl, new ChatTag[]{
                fromColoredText(initialLines[0]), fromColoredText(initialLines[1]),
                fromColoredText(initialLines[2]), fromColoredText(initialLines[3])}));
        tp.registerIncomingListener(pl, this, PacketInType.UpdateSign);
        tp.sendPacket(plr, new PacketPlayOutOpenSignEditor(bl));
    }

    public void cancel() {
        tp.unregisterIncomingListener(this);
        openSignGUIs.remove(plr.getName());
    }

    @Override
    public void onPacketIN(PacketInEvent e) {
        PacketPlayInUpdateSign packet = new PacketPlayInUpdateSign();
        packet.loadVanillaPacket(e.getPacket());
        if (e.getPlayer() == plr) {
            e.setCancelled(true);
            for (int i = 0; i < 4; ++i)
                result[i] = packet.lines[i].toColoredString();
            SU.sch.scheduleSyncDelayedTask(pl, () -> {
                try {
                    cancel();
                    Block b = plr.getWorld().getBlockAt(bl.x, bl.y, bl.z);
                    tp.sendPacket(plr, new PacketPlayOutBlockChange(bl, b.getTypeId(), b.getData()));
                    dr.done(SignGUI.this);
                } catch (Throwable e1) {
                    SU.error(SU.cs, e1, "SignGUI", "gyurix");
                }
            });
        }
    }
}
