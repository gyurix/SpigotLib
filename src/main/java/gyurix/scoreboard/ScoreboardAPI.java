package gyurix.scoreboard;

import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ScoreboardAPI {
    public static long id = 1;
    public static HashMap<String, PlayerBars> nametags = new HashMap<>();
    public static HashMap<String, PlayerBars> sidebars = new HashMap<>();
    public static HashMap<String, PlayerBars> tabbars = new HashMap<>();

    public static void playerJoin(Player plr) {
        String pln = plr.getName();
        nametags.put(pln, new PlayerBars());
        sidebars.put(pln, new PlayerBars());
        tabbars.put(pln, new PlayerBars());
    }

    public static void playerLeave(Player plr) {
        String pln = plr.getName();
        for (ScoreboardBar sb : nametags.remove(pln).loaded)
            sb.drop(plr);
        for (ScoreboardBar sb : sidebars.remove(pln).loaded)
            sb.drop(plr);
        for (ScoreboardBar sb : tabbars.remove(pln).loaded)
            sb.drop(plr);
    }

    private static boolean set(Player plr, PlayerBars info, ScoreboardBar to) {
        if (plr == null || info == null)
            return false;
        ScoreboardBar from = info.active;
        info.active = to;
        if (from == to)
            return false;
        if (to == null) {
            from.unload(plr);
            return true;
        }
        if (!to.load(plr))
            to.activate(plr);
        return true;
    }

    public static void setNametagBar(Player plr, NametagBar bar) {
        set(plr, nametags.get(plr.getName()), bar);
    }

    public static void setSidebar(Player plr, Sidebar bar) {
        set(plr, sidebars.get(plr.getName()), bar);
    }

    public static void setTabbar(Player plr, Tabbar bar) {
        set(plr, tabbars.get(plr.getName()), bar);
    }

    public static String[] specialSplit(String in, char uniqueChar) {
        if ((in = SU.optimizeColorCodes(in)).length() < 17)
            return new String[]{in, "§" + uniqueChar, ""};
        String[] out = new String[3];
        out[0] = in.substring(0, 16);
        if (out[0].endsWith("§")) {
            out[0] = out[0].substring(0, 15);
            in = out[0] + ' ' + in.substring(15);
        }
        StringBuilder formats = new StringBuilder();
        int prev = 32;
        for (int i = 0; i < 16; ++i) {
            char c = in.charAt(i);
            if (prev == 167) {
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f')
                    formats.setLength(0);
                formats.append('§').append(c);
            }
            prev = c;
        }
        in = SU.setLength(formats + in.substring(16), 54);
        if (in.length() < 17) {
            out[1] = "§" + uniqueChar;
            out[2] = in;
        } else {
            int id = in.length() - 16;
            out[1] = "§" + uniqueChar + in.substring(0, id);
            out[2] = in.substring(id);
        }
        return out;
    }
}

