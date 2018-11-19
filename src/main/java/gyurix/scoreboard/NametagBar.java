package gyurix.scoreboard;

import gyurix.scoreboard.team.CollisionRule;
import gyurix.scoreboard.team.NameTagVisibility;
import gyurix.scoreboard.team.TeamData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static gyurix.scoreboard.ScoreboardAPI.id;

public class NametagBar extends ScoreboardBar {
    int nextId = 0;
    @Getter
    @Setter
    private boolean useRealTeamNames;

    public NametagBar() {
        super("NB" + id, "NB" + id++, 2);
    }

    public void createTeam(String team, String prefix, String suffix, int color, String... players) {
        TeamData td = getTeam(team);
        td.prefix = prefix;
        td.suffix = suffix;
        td.color = color;
        td.players.addAll(Arrays.asList(players));
        updateTeam(team, td);
    }

    public void addPlayers(String team, String... players) {
        TeamData td = getTeam(team);
        td.players.addAll(Arrays.asList(players));
        updateTeam(team, td);
    }

    public void addPlayers(String team, Collection<String> players) {
        TeamData td = getTeam(team);
        td.players.addAll(players);
        updateTeam(team, td);
    }

    public TeamData getTeam(String team) {
        TeamData curTeam = currentData.teams.get(team);
        if (curTeam == null) {
            curTeam = new TeamData(useRealTeamNames ? team : (currentData.barname + "-" + ++nextId), team, "", "", false, true, NameTagVisibility.always, CollisionRule.always, 0, new ConcurrentSkipListSet<>());
            currentData.teams.put(team, curTeam);
        }
        return curTeam;
    }

    public void removePlayers(String team, String... players) {
        TeamData td = getTeam(team);
        td.players.removeAll(Arrays.asList(players));
        updateTeam(team, td);
    }

    public void removePlayers(String team, Collection<String> players) {
        TeamData td = getTeam(team);
        td.players.removeAll(players);
        updateTeam(team, td);
    }

    public void setCollisionRule(String team, CollisionRule value) {
        TeamData td = getTeam(team);
        td.collisionRule = value;
        updateTeam(team, td);
    }

    public void setColor(String team, int color) {
        TeamData td = getTeam(team);
        td.color = color;
        updateTeam(team, td);
    }

    public void setFriendlyFire(String team, boolean value) {
        TeamData td = getTeam(team);
        td.friendlyFire = value;
        updateTeam(team, td);
    }

    public void setNametagVisibility(String team, NameTagVisibility value) {
        TeamData td = getTeam(team);
        td.nameTagVisibility = value;
        updateTeam(team, td);
    }

    public void setPrefix(String team, String value) {
        TeamData td = getTeam(team);
        td.prefix = value;
        updateTeam(team, td);
    }

    public void setSeeInvisible(String team, boolean value) {
        TeamData td = getTeam(team);
        td.seeInvisible = value;
        updateTeam(team, td);
    }

    public void setSuffix(String team, String value) {
        TeamData td = getTeam(team);
        td.suffix = value;
        updateTeam(team, td);
    }

    private void updateTeam(String team, TeamData td) {
        active.keySet().removeIf((p) -> Bukkit.getPlayerExact(p) == null);
        loaded.keySet().removeIf((p) -> Bukkit.getPlayerExact(p) == null);
        for (Map.Entry<String, BarData> e : active.entrySet()) {
            BarData oldBar = e.getValue();
            TeamData old = oldBar.teams.get(team);
            td.update(Bukkit.getPlayerExact(e.getKey()), old);
            oldBar.teams.put(team, td.clone());
        }
    }
}

