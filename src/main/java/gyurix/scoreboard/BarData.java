package gyurix.scoreboard;

import gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardObjective;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore;
import gyurix.scoreboard.team.TeamData;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore.ScoreAction.CHANGE;
import static gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore.ScoreAction.REMOVE;
import static gyurix.scoreboard.ScoreboardDisplayMode.INTEGER;

/**
 * Created by GyuriX on 2016. 12. 03..
 */
public class BarData {
    public final String barname;
    public final HashMap<String, Integer> scores = new HashMap<>();
    public final HashMap<String, TeamData> teams = new HashMap<>();
    protected ScoreboardDisplayMode displayMode = INTEGER;
    protected String title;
    protected boolean visible = true;

    public BarData(String barname, String title, ScoreboardDisplayMode displayMode, boolean visible) {
        this.barname = barname;
        this.title = title;
        this.displayMode = displayMode;
        this.visible = visible;
    }

    public BarData clone() {
        BarData out = new BarData(barname, title, displayMode, visible);
        for (Map.Entry<String, TeamData> e : teams.entrySet())
            out.teams.put(e.getKey(), e.getValue().clone());
        for (Map.Entry<String, Integer> e : scores.entrySet())
            out.scores.put(e.getKey(), e.getValue());
        return out;
    }

    public void load(Player plr) {
        SU.tp.sendPacket(plr, new PacketPlayOutScoreboardObjective(barname, title, displayMode, 0));
        for (Map.Entry<String, TeamData> e : teams.entrySet())
            SU.tp.sendPacket(plr, e.getValue().getCreatePacket());
        for (Map.Entry<String, Integer> e : scores.entrySet())
            SU.tp.sendPacket(plr, new PacketPlayOutScoreboardScore(CHANGE, barname, e.getKey(), e.getValue()));
    }

    public void unload(Player plr) {
        SU.tp.sendPacket(plr, new PacketPlayOutScoreboardObjective(barname, title, displayMode, 1));
        for (Map.Entry<String, TeamData> e : teams.entrySet())
            SU.tp.sendPacket(plr, e.getValue().getRemovePacket());
    }

    public void update(Player plr, BarData old) {
        if (!old.title.equals(title) || old.displayMode != displayMode)
            SU.tp.sendPacket(plr, new PacketPlayOutScoreboardObjective(barname, title, displayMode, 2));
        for (Map.Entry<String, Integer> e : old.scores.entrySet()) {
            String pln = e.getKey();
            int oldScore = e.getValue();
            Integer newscore = scores.get(pln);
            if (newscore == null)
                SU.tp.sendPacket(plr, new PacketPlayOutScoreboardScore(REMOVE, barname, pln, 0));
            else if (newscore != oldScore)
                SU.tp.sendPacket(plr, new PacketPlayOutScoreboardScore(CHANGE, barname, pln, newscore));
        }
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            String pln = e.getKey();
            if (!old.scores.containsKey(pln))
                SU.tp.sendPacket(plr, new PacketPlayOutScoreboardScore(CHANGE, barname, pln, e.getValue()));
        }
        for (Map.Entry<String, TeamData> e : old.teams.entrySet()) {
            String teamName = e.getKey();
            TeamData oldTeam = e.getValue();
            TeamData newTeam = teams.get(teamName);
            if (newTeam == null)
                SU.tp.sendPacket(plr, oldTeam.getRemovePacket());
            else
                newTeam.update(plr, oldTeam);
        }
        for (TeamData td : teams.values())
            if (!old.teams.containsKey(td.name))
                SU.tp.sendPacket(plr, td.getCreatePacket());
    }
}
