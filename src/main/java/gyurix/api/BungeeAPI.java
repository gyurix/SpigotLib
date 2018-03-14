package gyurix.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gyurix.commands.Command;
import gyurix.json.JsonAPI;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static gyurix.spigotlib.Config.debug;

/**
 * BungeeAPI is the implementation of the
 * <a href=https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/>Spigot - Bungee communication protocol</a>
 */
public class BungeeAPI implements PluginMessageListener {
    public static boolean enabled, schedulePacketAPI;
    private static HashMap<UUID, String> ips = new HashMap<>();
    private static int playerCountRID, playerListRID, serversRID, currentServerRID, uuidAllRID, serverIPRID;
    private static HashMap<String, Integer> playerCounts = new HashMap<>();
    private static HashMap<String, String[]> players = new HashMap<>();
    private static HashMap<UUID, Integer> ports = new HashMap<>();
    private static HashMap<String, String> serverIps = new HashMap<>();
    private static String serverName = "N/A";
    private static HashMap<String, Short> serverPorts = new HashMap<>();
    private static String[] servers = new String[0];
    private static HashMap<String, UUID> uuids = new HashMap<>();

    public static boolean executeBungeeCommands(String[] commands, String... players) {
        if (!enabled)
            return false;
        String json = JsonAPI.serialize(commands);
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("BungeeCommand");
        out.writeUTF(StringUtils.join(players, ","));
        out.writeUTF(json);
        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        return true;
    }

    public static boolean executePlayerCommands(Command[] commands, String... players) {
        if (!enabled)
            return false;
        String json = JsonAPI.serialize(commands);
        return forwardToPlayer("CommandExecution", json.getBytes(), players);
    }

    public static boolean executeServerCommands(String[] commands, String... servers) {
        if (!enabled)
            return false;
        String json = JsonAPI.serialize(commands);
        return forwardToServer("CommandExecution", json.getBytes(), servers);
    }

    public static boolean executeServerCommands(Command[] commands, String... servers) {
        if (!enabled)
            return false;
        String json = JsonAPI.serialize(commands);
        return forwardToServer("CommandExecution", json.getBytes(), servers);
    }

    public static boolean forwardToAllServer(String channel, byte[] message) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(channel);
        out.writeShort(message.length);
        out.write(message);
        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, String... players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean forwardToPlayer(String channel, byte[] message, Iterable<String> players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean forwardToServer(String channel, byte[] message, String... servers) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF(s);
            out.writeUTF(channel);
            out.writeShort(message.length);
            out.write(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static String getIp(Player plr) {
        return ips.get(plr.getUniqueId());
    }

    public static Integer getPort(Player plr) {
        return ports.get(plr.getUniqueId());
    }

    public static String getServerIp(String server) {
        return serverIps.get(server);
    }

    public static String getServerName() {
        return serverName;
    }

    public static Short getServerPort(String server) {
        return serverPorts.get(server);
    }

    public static UUID getUUID(Player plr) {
        return uuids.get(plr.getName());
    }

    public static UUID getUUID(String pln) {
        return uuids.get(pln);
    }

    public static boolean kick(String message, String... players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("KickPlayer");
            out.writeUTF(s);
            out.writeUTF(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean kick(String message, Iterable<String> players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("KickPlayer");
            out.writeUTF(s);
            out.writeUTF(message);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static Integer playerCount(String server) {
        return playerCounts.get(server);
    }

    public static String[] playerList(String server) {
        return players.get(server);
    }

    public static boolean requestCurrentServerName() {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());

        return true;
    }

    public static void requestIP(Player... players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("IP");
        byte[] data = out.toByteArray();
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", data);
        }
    }

    public static void requestIP(Iterable<Player> players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("IP");
        byte[] data = out.toByteArray();
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", data);
        }
    }

    public static boolean requestPlayerCount(Iterable<String> servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerCount");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestPlayerCount(String... servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerCount");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestPlayerList(Iterable<String> servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerList");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestPlayerList(String... servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerList");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestServerIP(Iterable<String> servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ServerIP");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestServerIP(String... servers) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ServerIP");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestServerNames() {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());

        return true;
    }

    public static boolean requestUUID(Iterable<String> players) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UUIDOther");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean requestUUID(String... players) {
        if (!enabled)
            return false;
        Collection<Player> pls = (Collection<Player>) SU.srv.getOnlinePlayers();
        if (pls.size() == 0)
            return false;
        Player p = pls.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("UUIDOther");
            out.writeUTF(s);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static void send(String server, Player... players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
    }

    public static void send(String server, Collection<Player> players) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        for (Player p : players) {
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
    }

    public static boolean send(String server, String... players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(s);
            out.writeUTF(server);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean send(String server, Iterable<String> players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(s);
            out.writeUTF(server);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean sendMessage(String msg, String... players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(s);
            out.writeUTF(msg);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static boolean sendMessage(String msg, Iterable<String> players) {
        if (!enabled)
            return false;
        Collection<Player> pc = (Collection<Player>) Bukkit.getOnlinePlayers();
        if (pc.isEmpty())
            return false;
        Player p = pc.iterator().next();
        for (String s : players) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(s);
            out.writeUTF(msg);
            p.sendPluginMessage(Main.pl, "BungeeCord", out.toByteArray());
        }
        return true;
    }

    public static String[] serverNames() {
        return servers;
    }

    public static boolean start() {
        if (!enabled)
            return false;
        if (Config.BungeeAPI.servers > 0) {
            serversRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, BungeeAPI::requestServerNames, 0, Config.BungeeAPI.servers);
        }
        if (Config.BungeeAPI.currentServerName > 0) {
            currentServerRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, BungeeAPI::requestCurrentServerName, 0, Config.BungeeAPI.currentServerName);
        }
        if (Config.BungeeAPI.playerCount > 0) {
            playerCountRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> {
                requestPlayerCount(servers);
                requestPlayerCount("ALL");
            }, 2, Config.BungeeAPI.playerCount);
        }
        if (Config.BungeeAPI.playerList > 0) {
            playerListRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> {
                requestPlayerList(servers);
                requestPlayerList("ALL");
            }, 2, Config.BungeeAPI.playerList);
        }
        if (Config.BungeeAPI.uuidAll > 0) {
            uuidAllRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> requestUUID(totalPlayerList()), 4, Config.BungeeAPI.uuidAll);
        }
        if (Config.BungeeAPI.serverIP > 0) {
            serverIPRID = SU.sch.scheduleSyncRepeatingTask(Main.pl, () -> requestServerIP(servers), 4, Config.BungeeAPI.serverIP);
        }
        if (Config.BungeeAPI.ipOnJoin) {
            SU.sch.scheduleSyncDelayedTask(Main.pl, () -> requestIP((Collection<Player>) Bukkit.getOnlinePlayers()), 4);
        }
        return true;
    }

    public static Integer totalPlayerCount() {
        return playerCounts.get("ALL");
    }

    public static String[] totalPlayerList() {
        return players.get("ALL");
    }

    @Override
    public void onPluginMessageReceived(String channel, final Player player, byte[] bytes) {
        try {
            if (!channel.equals("BungeeCord"))
                return;
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String sub = in.readUTF();
            UUID uid = player.getUniqueId();
            debug.msg("Bungee", "Received plugin message from player " + player.getName() + ": " + sub + " " + new String(bytes));
            switch (sub) {
                case "CommandExecution":
                    final Command[] commands = JsonAPI.deserialize(in.readUTF(), Command[].class);
                    SU.sch.scheduleSyncDelayedTask(Main.pl, () -> {
                        for (Command c : commands) {
                            c.execute(player);
                        }
                    });
                    return;
                case "IP":
                    ips.put(uid, in.readUTF());
                    ports.put(uid, in.readInt());
                    return;
                case "PlayerCount":
                    playerCounts.put(in.readUTF(), in.readInt());
                    return;
                case "PlayerList":
                    players.put(in.readUTF(), in.readUTF().split(", "));
                    return;
                case "GetServers":
                    servers = in.readUTF().split(", ");
                    return;
                case "GetServer":
                    serverName = in.readUTF();
                    return;
                case "UUID":
                    uuids.put(player.getName(), UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                    return;
                case "UUIDOther":
                    uuids.put(in.readUTF(), UUID.fromString(in.readUTF().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                    return;
                case "ServerIP":
                    String server = in.readUTF();
                    serverIps.put(server, in.readUTF());
                    serverPorts.put(server, in.readShort());
            }
        } catch (Throwable ignored) {
        }
    }
}
