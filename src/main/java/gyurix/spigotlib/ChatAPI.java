package gyurix.spigotlib;

import gyurix.chat.ChatTag;
import gyurix.json.JsonAPI;
import gyurix.protocol.Reflection;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;

import static gyurix.spigotlib.Config.debug;
import static gyurix.spigotutils.ServerVersion.v1_8;

public class ChatAPI {
    /**
     * Method for converting IChatBaseComponent to raw JSON.
     */
    public static Method fromICBC;
    /**
     * The IChatBaseComponent class
     */
    public static Class icbcClass;
    /**
     * Method for converting raw json to IChatBaseComponent.
     */
    public static Method toICBC;

    /**
     * Converts the given message to raw JSON format
     *
     * @param msg - The message
     * @return The conversion result raw json
     */
    public static String TextToJson(String msg) {
        return ChatTag.fromExtraText(msg).toString();
    }

    /**
     * Initializes the ChatAPI. Do not use this method.
     */
    public static void init() {
        try {
            icbcClass = Reflection.getNMSClass("IChatBaseComponent");
            Class serializerClass = Reflection.getNMSClass("ChatSerializer");
            if (serializerClass == null)
                for (Class c : icbcClass.getClasses())
                    if (c.getName().endsWith("ChatSerializer")) {
                        serializerClass = c;
                        break;
                    }
            toICBC = serializerClass.getMethod("a", String.class);
            fromICBC = serializerClass.getMethod("a", icbcClass);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    /**
     * Converts a string to it's json format
     *
     * @param value - The convertable String
     * @return The conversion result
     */
    public static String quoteJson(String value) {
        return "{\"text\":\"" + JsonAPI.escape(value) + "\"}";
    }

    /**
     * Sends the given type json message to the given players or to every online player
     *
     * @param type - The type of the sendable JSON message
     * @param msg  - The json message
     * @param pls  -  The receiver list, if it is empty then all the online players will get the message
     */
    public static void sendJsonMsg(ChatMessageType type, String msg, Player... pls) {
        if (pls.length == 0) {
            sendJsonMsg(type, msg, Bukkit.getOnlinePlayers());
            return;
        }
        String json = type == ChatMessageType.ACTION_BAR ? quoteJson(msg) : TextToJson(msg);
        sendRawJson(type, json, pls);
    }

    /**
     * Sends the given type json message to the given players or to every online player
     *
     * @param type - The type of the sendable JSON message
     * @param msg  - The json message
     * @param pls  -  The receiver list, if it is empty then all the online players will get the message
     */
    public static void sendJsonMsg(ChatMessageType type, String msg, Collection<? extends Player> pls) {
        if (!Config.forceReducedMode && Reflection.ver.isAbove(v1_8)) {
            String json = type == ChatMessageType.ACTION_BAR ? quoteJson(msg) : TextToJson(msg);
            sendRawJson(type, json, pls);
        } else {
            msg = ChatTag.stripExtras(msg);
            for (Player p : pls)
                p.sendMessage(msg);
        }
    }

    /**
     * Sends a raw Json message to the given players
     *
     * @param type - The type of the sendable JSON message
     * @param json - The raw json
     * @param pls  - The receiver list
     */
    public static void sendRawJson(ChatMessageType type, String json, Player... pls) {
        debug.msg("Chat", "§bSendRawJson - §f" + json);
        if (!Config.forceReducedMode) {
            Object packet = new PacketPlayOutChat((byte) type.ordinal(), JsonAPI.deserialize(json, ChatTag.class)).getVanillaPacket();
            for (Player p : pls)
                SU.tp.sendPacket(p, packet);
            return;
        }
        json = JsonAPI.deserialize(json, ChatTag.class).toColoredString();
        for (Player p : pls)
            p.sendMessage(json);
    }

    /**
     * Sends a raw Json message to the given players
     *
     * @param type - The type of the sendable JSON message
     * @param json - The raw json
     * @param pls  - The receiver list
     */
    public static void sendRawJson(ChatMessageType type, String json, Collection<? extends Player> pls) {
        debug.msg("Chat", "§bSendRawJson - §f" + json);
        if (!Config.forceReducedMode) {
            Object packet = new PacketPlayOutChat((byte) type.ordinal(), JsonAPI.deserialize(json, ChatTag.class)).getVanillaPacket();
            for (Player p : pls)
                SU.tp.sendPacket(p, packet);
            return;
        }
        String[] j = JsonAPI.deserialize(json, ChatTag.class).toColoredString().split("\n");
        for (Player p : pls)
            p.sendMessage(j);
    }

    /**
     * Converts a raw json message to vanilla IChatBaseComponent
     *
     * @param json - The raw json
     * @return The vanilla IChatBaseComponent
     */
    public static Object toICBC(String json) {
        try {
            return json == null ? null : toICBC.invoke(null, json);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a vanilla IChatBaseComponent to a raw Json message
     *
     * @param icbc - The vanilla IChatBaseComponent
     * @return The raw json message
     */
    public static String toJson(Object icbc) {
        try {
            if (icbc == null) {
                return null;
            }
            return (String) fromICBC.invoke(null, icbc);
        } catch (Throwable e) {
            debug.msg("Chat", e);
            return null;
        }
    }

    /**
     * Escapes an unicode character
     *
     * @param ch - The escapeable character
     * @return The unicode escaped character in String
     */
    public static String unicodeEscape(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\u");
        String hex = Integer.toHexString(ch);
        for (int i = hex.length(); i < 4; ++i) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString();
    }

    /**
     * Enum of the available ChatMessageTypes
     */
    public enum ChatMessageType {
        CHAT,
        SYSTEM,
        ACTION_BAR;

        ChatMessageType() {
        }
    }
}