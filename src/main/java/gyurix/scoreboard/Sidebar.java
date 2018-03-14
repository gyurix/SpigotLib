package gyurix.scoreboard;

import java.util.ArrayList;

import static gyurix.scoreboard.ScoreboardAPI.id;

public class Sidebar extends ScoreboardBar {
    /**
     * The lines of the Sidebar (0-14)
     */
    public final ArrayList<SidebarLine> lines = new ArrayList<>();

    /**
     * Default sidebar constructor
     */
    public Sidebar() {
        super("SB" + id, "SB" + id++, 1);
        for (int i = 1; i < 16; ++i)
            lines.add(new SidebarLine(this, (char) (280 + i + id % 30000), "§6§lLine - §e§l" + i, 100 - i));
    }

    public void hideLine(int line) {
        if (line < 1 || line > 15)
            return;
        lines.get(line - 1).hide();
    }

    public boolean isShown(int line) {
        if (line < 1 || line > 15)
            return false;
        return !lines.get(line - 1).hidden;
    }

    public void setLine(int line, String text) {
        if (line < 1 || line > 15)
            return;
        lines.get(line - 1).setText(text);
    }

    public void setNumber(int line, int number) {
        if (line < 1 || line > 15)
            return;
        lines.get(line - 1).setNumber(number);
    }

    public void showLine(int line) {
        if (line < 1 || line > 15)
            return;
        lines.get(line - 1).show();
    }
}


