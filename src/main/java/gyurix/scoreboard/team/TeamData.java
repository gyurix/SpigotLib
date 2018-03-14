package gyurix.scoreboard.team;

import gyurix.protocol.wrappers.outpackets.PacketPlayOutScoreboardTeam;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by GyuriX on 2016. 12. 03..
 */
public class TeamData {
    public CollisionRule collisionRule;
    public int color;
    public boolean friendlyFire, seeInvisible;
    public String name, displayName, prefix, suffix;
    public NameTagVisibility nameTagVisibility;
    public ConcurrentSkipListSet<String> players = new ConcurrentSkipListSet<>();

    public TeamData(String name, String displayName, String prefix, String suffix, boolean friendlyFire, boolean seeInvisible, NameTagVisibility nameTagVisibility, CollisionRule collisionRule, int color, ConcurrentSkipListSet<String> players) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.friendlyFire = friendlyFire;
        this.seeInvisible = seeInvisible;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRule = collisionRule;
        this.color = color;
        this.players = players;
    }

    public TeamData(PacketPlayOutScoreboardTeam p) {
        apply(p);
    }

    public void apply(PacketPlayOutScoreboardTeam p) {
        switch (p.action) {
            case 0: //create team
                name = p.name;
                displayName = p.displayName;
                prefix = p.prefix;
                suffix = p.suffix;
                friendlyFire = (p.friendFlags & 1) == 1;
                seeInvisible = (p.friendFlags & 2) == 2;
                nameTagVisibility = p.nameTagVisibility;
                collisionRule = p.collisionRule;
                color = p.color;
                players.addAll(p.players);
                return;
            case 2: //update team info
                displayName = p.displayName;
                prefix = p.prefix;
                suffix = p.suffix;
                friendlyFire = (p.friendFlags & 1) == 1;
                seeInvisible = (p.friendFlags & 2) == 2;
                nameTagVisibility = p.nameTagVisibility;
                collisionRule = p.collisionRule;
                color = p.color;
                return;
            case 3: //add players to team
                players.addAll(p.players);
                return;
            case 4: //remove players from team
                players.removeAll(p.players);
        }
    }

    public TeamData clone() {
        return new TeamData(name, displayName, prefix, suffix, friendlyFire, seeInvisible, nameTagVisibility, collisionRule, color, new ConcurrentSkipListSet<>(players));
    }

    public PacketPlayOutScoreboardTeam getCreatePacket() {
        return new PacketPlayOutScoreboardTeam(name, displayName, prefix, suffix, nameTagVisibility, collisionRule, color,
                new ArrayList<>(players), 0, (friendlyFire ? 1 : 0) + (seeInvisible ? 2 : 0));
    }

    public PacketPlayOutScoreboardTeam getRemovePacket() {
        return new PacketPlayOutScoreboardTeam(name, 1);
    }

    public PacketPlayOutScoreboardTeam getUpdatePacket() {
        return new PacketPlayOutScoreboardTeam(name, displayName, prefix, suffix, nameTagVisibility, collisionRule, color,
                null, 2, (friendlyFire ? 1 : 0) + (seeInvisible ? 2 : 0));
    }

    public void update(Player plr, TeamData oldTeam) {
        if (oldTeam == null) {
            SU.tp.sendPacket(plr, getCreatePacket());
            return;
        }
        //Update info
        if (!oldTeam.displayName.equals(displayName) || !oldTeam.prefix.equals(prefix) || !oldTeam.suffix.equals(suffix) ||
                oldTeam.friendlyFire != friendlyFire || oldTeam.seeInvisible != seeInvisible || oldTeam.nameTagVisibility != nameTagVisibility ||
                oldTeam.collisionRule != collisionRule || oldTeam.color != color)
            SU.tp.sendPacket(plr, getUpdatePacket());
        //Remove players
        ArrayList<String> list = new ArrayList<>();
        for (String p : oldTeam.players)
            if (!players.contains(p))
                list.add(p);
        if (!list.isEmpty())
            SU.tp.sendPacket(plr, new PacketPlayOutScoreboardTeam(name, 4, list));
        //Add players
        list = new ArrayList<>();
        for (String p : players)
            if (!oldTeam.players.contains(p))
                list.add(p);
        if (!list.isEmpty())
            SU.tp.sendPacket(plr, new PacketPlayOutScoreboardTeam(name, 3, list));
    }
}
