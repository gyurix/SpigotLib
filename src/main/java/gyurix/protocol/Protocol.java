package gyurix.protocol;

import com.google.common.collect.Lists;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutEvent;
import gyurix.protocol.event.PacketOutType;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Protocol implements Listener {
    private static final HashMap<PacketInListener, PacketInType> inListenerTypes = new HashMap<>();
    private static final HashMap<PacketInType, ArrayList<PacketInListener>> inListeners = new HashMap<>();
    private static final HashMap<PacketOutListener, PacketOutType> outListenerTypes = new HashMap<>();
    private static final HashMap<PacketOutType, ArrayList<PacketOutListener>> outListeners = new HashMap<>();
    private static final String pa = "§5[§dPacketAPI§5] §e";
    private static final HashMap<Plugin, ArrayList<PacketInListener>> pluginInListeners = new HashMap<>();
    private static final HashMap<Plugin, ArrayList<PacketOutListener>> pluginOutListeners = new HashMap<>();

    /**
     * Dispatches an incoming packet event
     *
     * @param event - The packet event
     */
    public static void dispatchPacketInEvent(PacketInEvent event) {
        if (event.getType() == null) {
            SU.cs.sendMessage(pa + "Missing in packet type:§c " + event.getPacket().getClass().getName() + "§e.");
            return;
        }
        ArrayList<PacketInListener> ll = inListeners.get(event.getType());
        if (ll != null)
            for (PacketInListener l : ll) {
                try {
                    l.onPacketIN(event);
                } catch (Throwable e) {
                    SU.cs.sendMessage(pa + "Error on dispatching PacketInEvent for packet type:§c " + event.getType() + "§e in listener §c" + l.getClass().getName() + "§e:");
                    SU.error(SU.cs, e, "SpigotLib", "gyurix");
                }
            }
    }

    /**
     * Dispatches an outgoing packet event
     *
     * @param event - The packet event
     */
    public static void dispatchPacketOutEvent(PacketOutEvent event) {
        if (event.getType() == null) {
            SU.cs.sendMessage(pa + "Missing out packet type:§c " + event.getPacket().getClass().getName() + "§e.");
            return;
        }
        ArrayList<PacketOutListener> ll = outListeners.get(event.getType());
        if (ll != null)
            for (PacketOutListener l : ll) {
                try {
                    l.onPacketOUT(event);
                } catch (Throwable e) {
                    SU.cs.sendMessage(pa + "Error on dispatching PacketOutEvent for packet type:§c " + event.getType() + "§e in listener §c" + l.getClass().getName() + "§e:");
                    SU.error(SU.cs, e, "SpigotLib", "gyurix");
                }
            }
    }

    /**
     * Closes the PacketAPI
     */
    public void close() throws Throwable {
        HandlerList.unregisterAll(this);
        unregisterServerChannelHandler();
        SU.srv.getOnlinePlayers().forEach(this::uninjectPlayer);
    }

    /**
     * Returns the channel of a Player
     *
     * @param plr - The target Player
     * @return The channel of the target Player
     */
    public abstract Object getChannel(Player plr);

    /**
     * Returns the Player belonging to the given channel
     *
     * @param channel - The target Player
     * @return The Player for who is the given channel belongs to, or null if the Channel and the Player object is not yet matched.
     */
    public abstract Player getPlayer(Object channel);

    /**
     * Initializes the PacketAPI
     *
     * @throws Throwable if something failed in the initialization
     */
    public abstract void init() throws Throwable;

    public abstract void injectPlayer(Player plr);

    public abstract void printPipeline(Iterable<Map.Entry<String, ?>> pipeline);

    /**
     * Simulates receiving the given vanilla packet from a player
     *
     * @param player - The sender player
     * @param packet - The sendable packet
     */
    public void receivePacket(Player player, Object packet) {
        Object channel = getChannel(player);
        if (channel == null || packet == null) {
            SU.error(SU.cs, new RuntimeException("§cFailed to receive packet " + packet + " from player " + (player == null ? "null" : player.getName())), "SpigotLib", "gyurix");
            return;
        }
        receivePacket(channel, packet);
    }

    /**
     * Simulates receiving the given vanilla packet from a channel
     *
     * @param channel - The sender players channel
     * @param packet  - The sendable packet
     */
    public abstract void receivePacket(Object channel, Object packet);

    /**
     * Registers an incoming packet listener
     *
     * @param plugin     - The plugin for which the listener belongs to
     * @param listener   - The packet listener
     * @param packetType - The listenable packet type
     */
    public void registerIncomingListener(Plugin plugin, PacketInListener listener, PacketInType packetType) {
        if (inListenerTypes.containsKey(listener))
            throw new RuntimeException("The given listener is already registered.");
        ArrayList<PacketInListener> pil = inListeners.get(packetType);
        if (pil == null)
            inListeners.put(packetType, Lists.newArrayList(listener));
        else
            pil.add(listener);
        inListenerTypes.put(listener, packetType);
        pil = pluginInListeners.get(plugin);
        if (pil == null)
            pluginInListeners.put(plugin, Lists.newArrayList(listener));
        else
            pil.add(listener);
    }

    /**
     * Registers an outgoing packet listener
     *
     * @param plugin     - The plugin for which the listener belongs to
     * @param listener   - The packet listener
     * @param packetType - The listenable packet type
     */
    public void registerOutgoingListener(Plugin plugin, PacketOutListener listener, PacketOutType packetType) {
        if (outListenerTypes.containsKey(listener))
            throw new RuntimeException("The given listener is already registered.");
        ArrayList<PacketOutListener> pol = outListeners.get(packetType);
        if (pol == null)
            outListeners.put(packetType, Lists.newArrayList(listener));
        else
            pol.add(listener);
        outListenerTypes.put(listener, packetType);
        pol = pluginOutListeners.get(plugin);
        if (pol == null)
            pluginOutListeners.put(plugin, Lists.newArrayList(listener));
        else
            pol.add(listener);
    }

    public abstract void registerServerChannelHook() throws Throwable;

    public abstract void removeHandler(Object ch, String handler);

    /**
     * Sends the given vanilla packet to a player
     *
     * @param player - The target player
     * @param packet - The sendable packet
     */
    public void sendPacket(Player player, Object packet) {
        Object channel = getChannel(player);
        if (channel == null || packet == null) {
            SU.error(SU.cs, new RuntimeException("§cFailed to send packet " + packet + " to player " + (player == null ? "null" : player.getName())), "SpigotLib", "gyurix");
            return;
        }
        sendPacket(channel, packet);
    }

    /**
     * Sends the given vanilla packet to a channel
     *
     * @param channel - The target players channel
     * @param packet  - The sendable packet
     */
    public abstract void sendPacket(Object channel, Object packet);

    public void uninjectChannel(Object ch) {
        removeHandler(ch, "SpigotLibInit");
        removeHandler(ch, "SpigotLib");
    }

    public void uninjectPlayer(Player player) {
        uninjectChannel(getChannel(player));
    }

    /**
     * Unregisters ALL the incoming packet listeners of a plugin
     *
     * @param pl - Target plugin
     */
    public void unregisterIncomingListener(Plugin pl) {
        ArrayList<PacketInListener> pol = pluginInListeners.remove(pl);
        if (pol == null)
            return;
        for (PacketInListener l : pol)
            inListeners.remove(inListenerTypes.remove(l));
    }

    public void unregisterIncomingListener(PacketInListener listener) {
        inListeners.get(inListenerTypes.remove(listener)).remove(listener);
    }

    public void unregisterOutgoingListener(PacketOutListener listener) {
        outListeners.get(outListenerTypes.remove(listener)).remove(listener);
    }

    /**
     * Unregisters ALL the outgoing packet listeners of a plugin
     *
     * @param pl - Target plugin
     */
    public void unregisterOutgoingListener(Plugin pl) {
        ArrayList<PacketOutListener> pol = pluginOutListeners.remove(pl);
        if (pol == null)
            return;
        for (PacketOutListener l : pol)
            outListeners.remove(outListenerTypes.remove(l));
    }

    public abstract void unregisterServerChannelHandler() throws IllegalAccessException;

    /**
     * Interface used for listening to incoming packets
     */
    public interface PacketInListener {
        void onPacketIN(PacketInEvent e);
    }

    /**
     * Interface used for listening to outgoing packets
     */
    public interface PacketOutListener {
        void onPacketOUT(PacketOutEvent e);
    }
}
