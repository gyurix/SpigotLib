package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.configfile.ConfigSerialization.StringSerializable;

import java.util.ArrayList;
import java.util.Iterator;

public class BlinkEffect implements StringSerializable, CustomEffect {
    private boolean active = true;
    private Iterator<Long> data;
    private long remaining;
    private ArrayList<Long> repeat = new ArrayList();
    private String text;

    public BlinkEffect(String in) {
        if (in.startsWith("{")) {
            for (String s : in.substring(1, in.indexOf("}")).split(" ")) {
                if (s.equals("A")) {
                    active = false;
                    continue;
                }
                repeat.add(Long.valueOf(s));
            }
        } else {
            repeat.add(1L);
            text = in;
        }
        data = repeat.iterator();
        remaining = data.next();
    }

    public BlinkEffect() {
    }

    @Override
    public CustomEffect clone() {
        BlinkEffect be = new BlinkEffect();
        be.active = active;
        be.data = data;
        be.remaining = remaining;
        be.repeat = repeat;
        be.text = text;
        return be;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        if (!active) {
            out.append("A ");
        }
        for (Long r : repeat) {
            out.append(r).append(' ');
        }
        out.setCharAt(out.length() - 1, '}');
        return out.append(text).toString();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String newText) {
        text = newText;
    }

    @Override
    public String next(String in) {
        --remaining;
        if (remaining == 0) {
            if (!data.hasNext()) {
                data = repeat.iterator();
            }
            remaining = data.next();
            active = !active;
        }
        return active ? in : in.replaceAll(".", " ");
    }
}

