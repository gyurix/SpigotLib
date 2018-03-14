package gyurix.sign;

import gyurix.spigotlib.SU;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;

/**
 * Created by GyuriX on 2016. 07. 13..
 */
public class SignConfig {
    public ArrayList<String> lines = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj.toString().equals(toString());
    }

    @Override
    public String toString() {
        return "- " + lines.get(0) + "\n- " + lines.get(1) + "\n- " + lines.get(2) + "\n- " + lines.get(3);
    }

    public String[] getLinesArray(Object... vars) {
        String[] out = new String[4];
        for (int i = 0; i < 4; i++)
            out[i] = SU.fillVariables(lines.get(i), vars);
        return out;
    }

    public boolean linesEqual(String[] lines) {
        for (int i = 0; i < 4; i++)
            if (!lines[i].equals(this.lines.get(i)))
                return false;
        return true;
    }

    public boolean set(Block b, Object... vars) {
        try {
            return set((Sign) b.getState(), vars);
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean set(Sign s, Object... vars) {
        try {
            String[] newLines = new String[4];
            for (int i = 0; i < 4; i++)
                newLines[i] = SU.fillVariables(lines.get(i), vars);
            PluginSignChangeEvent e = new PluginSignChangeEvent(s.getBlock(), newLines);
            SU.pm.callEvent(e);
            if (e.isCancelled())
                return false;
            for (int i = 0; i < 4; i++)
                s.setLine(i, newLines[i]);
            s.update(true, false);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean setNoEvent(Block b, Object... vars) {
        try {
            return setNoEvent((Sign) b.getState(), vars);
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean setNoEvent(Sign s, Object... vars) {
        try {
            for (int i = 0; i < 4; i++) {
                s.setLine(i, SU.fillVariables(lines.get(i), vars));
            }
            s.update(true, false);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
