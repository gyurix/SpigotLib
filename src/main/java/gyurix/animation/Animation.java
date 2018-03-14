package gyurix.animation;

import gyurix.animation.effects.FramesEffect;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigSerialization.Serializer;
import gyurix.spigotlib.SU;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Animation {
    public HashMap<String, HashMap<String, CustomEffect>> effects = new HashMap();
    public HashMap<String, String> init = new HashMap<>();

    public static class AnimationSerializer
            implements Serializer {
        @Override
        public Object fromData(ConfigData data, Class cl, Type... args) {
            Animation anim = new Animation();
            long ft = 0;
            if (data.mapData != null) {
                for (Entry<ConfigData, ConfigData> e : data.mapData.entrySet()) {
                    String key = e.getKey().stringData;
                    ConfigData value = e.getValue();
                    if (key.endsWith("s")) {
                        if (AnimationAPI.effects.containsKey(key = key.substring(0, key.length() - 1))) {
                            anim.effects.put(key, e.getValue().deserialize(HashMap.class, String.class, AnimationAPI.effects.get(key)));
                            continue;
                        }
                        SU.cs.sendMessage("§e[AnimationAPI] §cUnregistered effect type §e" + key + "§c can't be active.");
                        continue;
                    }
                    if (key.equals("frameTime"))
                        ft = Long.valueOf(value.stringData);
                }
            }
            if (data.listData != null) {
                FramesEffect fe = new FramesEffect();
                for (ConfigData cd : data.listData) {
                    fe.frames.add(new Frame(cd.stringData));
                }
                HashMap<String, CustomEffect> map = anim.effects.get("frame");
                if (map == null)
                    map = new HashMap<>();
                map.put("main", fe);
                anim.effects.put("frame", map);
            }
            if (data.stringData != null && !data.stringData.isEmpty()) {
                if (data.stringData.startsWith("{")) {
                    int id = data.stringData.indexOf("}");
                    for (String d : data.stringData.substring(1, id).split(" ")) {
                        String[] d2 = d.split(":", 2);
                        if (d2[0].equals("FT"))
                            ft = Integer.valueOf(d2[1]);
                        else
                            anim.init.put(d2[0], d2.length == 1 ? null : d2[1]);
                    }
                    data.stringData = data.stringData.substring(id + 1);
                }
                HashMap map = anim.effects.get("frame");
                if (map == null)
                    anim.effects.put("frame", map = new HashMap());
                if (!map.containsKey("main")) {
                    FramesEffect fe = new FramesEffect();
                    if (data.stringData.contains(";")) {
                        for (String s : data.stringData.split(";"))
                            fe.frames.add(new Frame(s));
                    } else
                        fe.frames.add(new Frame(data.stringData));
                    map.put("main", fe);
                }
            }
            if (!anim.effects.containsKey("frame")) {
                SU.cs.sendMessage("§e[AnimationAPI] §cError, the animation doesn't contain ANY frames parts.");
                return fromData(new ConfigData("ERROR-NO-FRAMES"), cl, args);
            }
            if (!anim.effects.get("frame").containsKey("main")) {
                SU.cs.sendMessage("§e[AnimationAPI] §cError, the animation doesn't contain the main frames part.");
                return fromData(new ConfigData("ERROR-NO-MAINFRAMEPART"), cl, args);
            }
            if (((FramesEffect) anim.effects.get("frame").get("main")).frames.isEmpty()) {
                SU.cs.sendMessage("§e[AnimationAPI] §cError, the animation doesn't contain any frames.");
                return fromData(new ConfigData("ERROR-NO-MAINFRAMES"), cl, args);
            }
            if (ft > 0) {
                ((FramesEffect) anim.effects.get("frame").get("main")).frameTime = ft;
            }
            for (CustomEffect fe : anim.effects.get("frame").values()) {
                for (Frame f : ((FramesEffect) fe).frames) {
                    for (Entry<String, Class> ef : AnimationAPI.effects.entrySet()) {
                        int id;
                        String text = f.text;
                        String efn = ef.getKey();
                        while ((id = text.indexOf("<" + efn + ":")) != -1) {
                            int bracket;
                            int colon = (text = text.substring(id += efn.length() + 2)).indexOf(":");
                            if (colon == -1) {
                                colon = text.length();
                            }
                            if ((bracket = text.indexOf(">")) == -1) {
                                bracket = text.length();
                            }
                            int id2 = Math.min(bracket, colon);
                            String name = text.substring(0, id2);
                            HashMap map = anim.effects.get(efn);
                            if (map == null) {
                                map = new HashMap();
                            }
                            try {
                                if (map.containsKey(name)) continue;
                                map.put(name, ef.getValue().newInstance());
                                anim.effects.put(efn, map);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return anim;
        }

        @Override
        public ConfigData toData(Object obj, Type... types) {
            Animation anim = (Animation) obj;
            ConfigData out = new ConfigData();
            out.mapData = new LinkedHashMap();
            for (Entry<String, HashMap<String, CustomEffect>> e : anim.effects.entrySet()) {
                Class cl = AnimationAPI.effects.get(e.getKey());
                if (cl == null) {
                    System.err.println("Unregistered effect type " + e.getKey() + " can't be saved.");
                    continue;
                }
                out.mapData.put(new ConfigData(e.getKey() + "s"), ConfigData.serializeObject(e.getValue(), String.class, cl));
            }
            return out;
        }
    }

}

