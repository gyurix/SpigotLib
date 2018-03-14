package gyurix.animation;

import gyurix.configfile.ConfigSerialization.StringSerializable;

import java.util.ArrayList;

public class Frame implements StringSerializable {
    public ArrayList<Long> delays;
    public ArrayList<Long> repeats;
    public String text;

    public Frame(String in) {
        if (in.startsWith("{")) {
            delays = new ArrayList();
            repeats = new ArrayList();
            int id = in.indexOf("}");
            for (String s : in.substring(1, id).split("(,+| +) *")) {
                String[] d2 = s.split(":", 2);
                if (d2.length == 2) {
                    delays.add(Long.valueOf(d2[1]));
                    repeats.add(Long.valueOf(d2[0]));
                    continue;
                }
                delays.add(Long.valueOf(d2[0]));
                repeats.add(1L);
            }
            text = in.substring(id + 1);
        } else {
            text = in;
        }
    }

    @Override
    public String toString() {
        if (delays == null || delays.isEmpty()) {
            return text;
        }
        StringBuilder out = new StringBuilder();
        out.append('{');
        for (int i = 0; i < delays.size(); ++i) {
            long repeat = repeats.get(i);
            long delay = delays.get(i);
            if (repeat == 1) {
                out.append(delay);
            } else {
                out.append(repeat).append(':').append(delay);
            }
            out.append(",");
        }
        out.setCharAt(out.length() - 1, '}');
        out.append(text);
        return out.toString();
    }
}

