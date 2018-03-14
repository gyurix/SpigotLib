package gyurix.animation.effects;

import gyurix.animation.CustomEffect;
import gyurix.spigotlib.SU;

public class RainbowEffect implements CustomEffect {
    public boolean random;
    public int state = -1;

    public RainbowEffect() {
    }

    public RainbowEffect(int state, boolean random) {
        this.state = state;
        this.random = random;
    }

    @Override
    public CustomEffect clone() {
        return new RainbowEffect(state, random);
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
        state = random ? SU.rand.nextInt(in.length()) : (state + 1) % in.length();
        return "\u00a7" + in.charAt(state % in.length());
    }
}

