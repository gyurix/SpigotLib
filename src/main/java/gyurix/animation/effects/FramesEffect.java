package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.animation.Frame;
import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.SU;

import java.util.ArrayList;
import java.util.Iterator;

public class FramesEffect implements CustomEffect {
    @ConfigOptions(serialize = false)
    public long delay;
    @ConfigOptions(serialize = false)
    public Iterator<Long> delays;
    @ConfigOptions(serialize = false)
    public Frame f;
    @ConfigOptions(defaultValue = "9223372036854775807")
    public long frameTime = Long.MAX_VALUE;
    public ArrayList<Frame> frames = new ArrayList();
    public boolean random;
    @ConfigOptions(serialize = false)
    public long repeat;
    @ConfigOptions(serialize = false)
    public Iterator<Long> repeats;
    @ConfigOptions(serialize = false)
    public int state = -1;

    @Override
    public CustomEffect clone() {
        FramesEffect fe = new FramesEffect();
        fe.frameTime = frameTime;
        fe.state = state;
        fe.frames = frames;
        fe.random = random;
        fe.f = f;
        fe.delay = delay;
        fe.delays = delays;
        fe.repeats = repeats;
        fe.repeat = repeat;
        return fe;
    }

    @Override
    public String getText() {
        return f != null ? f.text : "";
    }

    @Override
    public void setText(String newText) {
    }

    @Override
    public String next(String in) {
        if (repeat == 0) {
            if (delays == null || !delays.hasNext()) {
                state = random ? SU.rand.nextInt(frames.size()) : (state + 1) % frames.size();
                f = frames.get(state);
                if (f.delays == null) {
                    delay = frameTime;
                    repeat = 1;
                    delays = null;
                } else {
                    delays = f.delays.iterator();
                    repeats = f.repeats.iterator();
                    delay = delays.next();
                    repeat = repeats.next();
                }
            } else {
                delay = delays.next();
                repeat = repeats.next();
            }
        }
        --repeat;
        return f.text;
    }
}

