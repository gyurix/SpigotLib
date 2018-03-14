package gyurix.scoreboard;

import gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore.ScoreAction;
import gyurix.scoreboard.team.CollisionRule;
import gyurix.scoreboard.team.NameTagVisibility;
import gyurix.scoreboard.team.TeamData;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore.ScoreAction.CHANGE;
import static gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardScore.ScoreAction.REMOVE;

public class SidebarLine {
    public final Sidebar bar;
    public final char uniqueChar;
    public boolean hidden;
    public int number;
    public TeamData team, oldTeam;
    public String teamName;

    public SidebarLine(Sidebar bar, char ch, String text, int number) {
        this.bar = bar;
        uniqueChar = ch;
        teamName = bar.teamNamePrefix + "§" + uniqueChar;
        String[] set = ScoreboardAPI.specialSplit(text, uniqueChar);
        ConcurrentSkipListSet cset = new ConcurrentSkipListSet();
        cset.add(set[1]);
        oldTeam = team = new TeamData(teamName, teamName, set[0], set[2], false, false, NameTagVisibility.always, CollisionRule.always, 0, cset);
        this.number = number;
        bar.currentData.scores.put(set[1], number);
        bar.currentData.teams.put(team.name, team);
    }

    public String getText() {
        return team.prefix + team.players.iterator().next() + team.suffix;
    }

    public void setText(String text) {
        oldTeam = team.clone();
        String[] set = ScoreboardAPI.specialSplit(text, uniqueChar);
        team.prefix = set[0];
        team.players.clear();
        team.players.add(set[1]);
        team.suffix = set[2];
        String oldUser = oldTeam.players.iterator().next();
        if (!set[1].equals(oldUser))
            bar.currentData.scores.remove(oldUser);
        if (hidden)
            return;
        Iterator<Map.Entry<String, BarData>> it = bar.active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BarData> e = it.next();
            String pln = e.getKey();
            BarData bd = e.getValue();
            TeamData td = bd.teams.get(team.name);
            Player plr = Bukkit.getPlayer(pln);
            if (plr == null) {
                it.remove();
                continue;
            }
            team.update(plr, td);
            td.prefix = set[0];
            td.players.clear();
            td.players.add(set[1]);
            td.suffix = set[2];
        }
        if (!set[1].equals(oldUser)) {
            bar.currentData.scores.put(set[1], number);
            Object packet = new PacketPlayOutScoreboardScore(ScoreAction.REMOVE, bar.currentData.barname, oldUser, number).getVanillaPacket();
            Object packet2 = new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.ScoreAction.CHANGE, bar.currentData.barname, set[1], number).getVanillaPacket();
            for (Map.Entry<String, BarData> e : bar.active.entrySet()) {
                e.getValue().scores.remove(oldUser);
                e.getValue().scores.put(set[1], number);
                SU.tp.sendPacket(Bukkit.getPlayer(e.getKey()), packet2);
                SU.tp.sendPacket(Bukkit.getPlayer(e.getKey()), packet);
            }
        }
    }

    public boolean hide() {
        if (hidden)
            return false;
        String user = team.players.iterator().next();
        bar.currentData.scores.remove(user);
        for (Map.Entry<String, BarData> e : bar.active.entrySet()) {
            e.getValue().scores.remove(user);
            SU.tp.sendPacket(Bukkit.getPlayer(e.getKey()), new PacketPlayOutScoreboardScore(REMOVE, bar.currentData.barname, user, number));
        }
        hidden = true;
        return true;
    }

    public void setNumber(int value) {
        if (number == value)
            return;
        number = value;
        if (hidden)
            return;
        String user = team.players.iterator().next();
        bar.currentData.scores.put(user, number);
        Object packet = new PacketPlayOutScoreboardScore(PacketPlayOutScoreboardScore.ScoreAction.CHANGE, bar.currentData.barname, user, number).getVanillaPacket();
        for (Map.Entry<String, BarData> e : bar.active.entrySet()) {
            e.getValue().scores.put(user, number);
            SU.tp.sendPacket(Bukkit.getPlayer(e.getKey()), packet);
        }
    }

    public boolean show() {
        if (!hidden)
            return false;
        String user = team.players.iterator().next();
        bar.currentData.scores.put(user, number);
        for (Map.Entry<String, BarData> e : bar.active.entrySet()) {
            e.getValue().scores.put(user, number);
            SU.tp.sendPacket(Bukkit.getPlayer(e.getKey()), new PacketPlayOutScoreboardScore(CHANGE, bar.currentData.barname, user, number));
        }
        hidden = false;
        return true;
    }
}

