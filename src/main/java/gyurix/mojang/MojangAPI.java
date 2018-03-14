package gyurix.mojang;

import gyurix.json.JsonAPI;
import gyurix.protocol.utils.GameProfile;
import gyurix.spigotlib.SU;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static gyurix.mojang.WebApi.get;
import static gyurix.mojang.WebApi.post;

/**
 * Created by GyuriX on 2015.12.27..
 */
public class MojangAPI {
    public static ClientSession clientLogin(String userName, String password) {
        try {
            String s = post("https://authserver.mojang.com/authenticate",
                    "{\"agent\":{\"name\":\"Minecraft\",\"version\":1}," +
                            "\"username\":\"" + userName + "\"," +
                            "\"password\":\"" + password + "\"}");
            System.out.println(s);
            ClientSession client = JsonAPI.deserialize(s, ClientSession.class);
            client.username = userName;
            client.password = password;
            return client;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<NameData> getNameHistory(UUID id) {
        try {
            return (ArrayList<NameData>) JsonAPI.deserialize(get("https://api.mojang.com/user/profiles/" + id.toString().replace("-", "") + "/names"), ArrayList.class, NameData.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfile getProfile(String name) {
        try {
            return JsonAPI.deserialize(get("https://api.mojang.com/users/profiles/minecraft/" + name), GameProfile.class);
        } catch (Throwable e) {
            return new GameProfile(name, SU.getOfflineUUID(name));
        }
    }

    public static GameProfile getProfile(String name, long time) {
        return JsonAPI.deserialize(get("https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + time), GameProfile.class);
    }

    public static GameProfile getProfileWithSkin(UUID id) {
        return JsonAPI.deserialize(get("https://sessionserver.mojang.com/session/minecraft/profile/" + id.toString().replace("-", "") + "?unsigned=false"), GameProfile.class);
    }

    public static ArrayList<GameProfile> getProfiles(String... names) {
        return (ArrayList<GameProfile>) JsonAPI.deserialize(post("https://api.mojang.com/profiles/minecraft", JsonAPI.serialize(names)), ArrayList.class, GameProfile.class);
    }

    public static HashMap<String, MojangServerState> getServerState() {
        HashMap<String, MojangServerState> out = new HashMap<>();
        String[] d = get("https://status.mojang.com/check").split(",");
        for (String s : d) {
            String[] s2 = s.split(":");
            out.put(s2[0].substring(s2[0].indexOf("\"") + 1, s2[0].length() - 1),
                    MojangServerState.valueOf(s2[1].substring(s2[1].indexOf("\"") + 1, s2[1].lastIndexOf("\"")).toUpperCase()));
        }
        return out;
    }


    public enum MojangServerState {
        RED, GREEN, YELLOW
    }

    public static class NameData {
        public long changedToAt;
        public String name;

        @Override
        public String toString() {
            return JsonAPI.serialize(this);
        }
    }
}
