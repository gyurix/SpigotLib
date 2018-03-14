package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.spigotlib.SU;

public class FlameEffect implements CustomEffect {
    public FlameInfo info;
    public boolean rotate;
    public int speed = 1;
    public int start;

    @Override
    public CustomEffect clone() {
        FlameEffect fe = new FlameEffect();
        fe.info = info;
        fe.speed = speed;
        fe.start = start;
        fe.rotate = rotate;
        return fe;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public void setText(String newText) {
    }

    @Override
    public String next(String in) {
        StringBuilder out = new StringBuilder();
        int strstate = start;
        int state = 0;
        if (strstate >= in.length()) {
            step(in);
            return in;
        }
        if (strstate > 0) {
            out.append(in.substring(0, strstate));
        }
        for (int i = 0; i < info.counts.length; i++) {
            out.append(info.pref[i]);
            if (strstate + info.counts[i] > 0) {
                int maxid = strstate + info.counts[i];
                if (maxid > in.length())
                    break;
                else {
                    out.append(in.substring(Math.max(strstate, 0), maxid));
                }
            }
            strstate += info.counts[i];
        }
        if (strstate < in.length()) {
            out.append(in.substring(Math.max(strstate, 0)));
        }
        step(in);
        return SU.optimizeColorCodes(out.toString());
    }

    private void step(String in) {
        int count = 0;
        for (int c : info.counts) {
            count += c;
        }
        count -= info.counts[info.counts.length - 1];
        if (start >= in.length()) {
            if (rotate) {
                speed = -speed;
            } else {
                start = -count;
                return;
            }
        } else if (rotate && speed < 0 && start <= -count) {
            speed = -speed;
        }
        start += speed;
    }

    public static class FlameInfo
            implements StringSerializable {
        public int[] counts;
        public String[] pref;

        public FlameInfo() {
        }

        public FlameInfo(String in) {
            String[] d = in.split(" ");
            counts = new int[d.length];
            pref = new String[d.length];
            for (int i = 0; i < d.length; ++i) {
                String[] d2 = d[i].split(":", 2);
                counts[i] = 1;
                try {
                    pref[i] = d2[0];
                    counts[i] = Integer.valueOf(d2[1]);
                } catch (Throwable t) {
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < counts.length; ++i) {
                out.append(' ').append(pref[i]).append(':').append(counts[i]);
            }
            return out.substring(1);
        }
    }

}

