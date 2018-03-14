package gyurix.animation.effects;

import gyurix.animation.CustomEffect;

import java.util.HashSet;
import java.util.Iterator;

public class ScrollerEffect implements CustomEffect {
    public char fill = 32;
    public int max = 80;
    public boolean reversed;
    public int size = 16;
    public boolean skipColors = true;
    public int speed = 1;
    public int start;
    public String text;

    public ScrollerEffect() {
    }

    public ScrollerEffect(int max, int size, String text) {
        this(max, size, 1, 0, false, true, ' ', text);
    }

    public ScrollerEffect(int max, int size, int speed, int start, boolean reversed, boolean skipColors, char fill, String text) {
        this.max = max;
        this.size = size;
        this.speed = speed;
        this.start = start;
        this.reversed = reversed;
        this.skipColors = skipColors;
        this.fill = fill;
        this.text = text;
    }

    @Override
    public CustomEffect clone() {
        return new ScrollerEffect(max, size, speed, start, reversed, skipColors, fill, text);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String next(String text) {
        char c;
        int i;
        int i2;
        int id;
        StringBuilder sb = new StringBuilder(size);
        HashSet<Character> formats = new HashSet<Character>();
        int inc = speed;
        char colorPrefix = ' ';
        char[] chars = text.toCharArray();
        for (int i3 = 1; i3 < max; ++i3) {
            int id2 = (start + max - i3) % max;
            if (chars.length <= id2 || chars[id2] != '\u00a7' || chars.length <= (id2 = (id2 + 1) % max)) continue;
            c = chars[id2];
            if (c < 'k' || c > 'o') {
                colorPrefix = c;
                break;
            }
            formats.add(Character.valueOf(c));
        }
        int n = i2 = chars.length > (id = (start + max - 1) % max) && chars[id] == '\u00a7' ? 1 : 0;
        while (i2 < size && chars.length > (id = (start + i2) % max) && chars[id] == '\u00a7') {
            id = (start + i2 + 1) % max;
            if (chars.length > id) {
                c = chars[id];
                if (skipColors) {
                    inc += 2;
                }
                if (c < 'k' || c > 'o') {
                    formats.clear();
                    colorPrefix = c;
                } else {
                    formats.add(Character.valueOf(c));
                }
            }
            i2 += 2;
        }
        if (colorPrefix != ' ') {
            sb.append('\u00a7').append(colorPrefix);
        }
        Iterator i$ = formats.iterator();
        while (i$.hasNext()) {
            c = ((Character) i$.next()).charValue();
            sb.append('\u00a7').append(c);
        }
        id = (start + max - 1) % max;
        int n2 = i = chars.length > id && chars[id] == '\u00a7' ? 1 : 0;
        while (sb.length() < size) {
            id = (start + i) % max;
            sb.append(chars.length > id ? chars[id] : fill);
            ++i;
        }
        if (sb.charAt(size - 1) == '\u00a7') {
            sb.setCharAt(size - 1, fill);
        }
        if (reversed) {
            start -= inc;
            if (start < 0) {
                start = max - inc;
            }
        } else {
            start += inc;
            if (start >= max) {
                start = inc - 1;
            }
        }
        return sb.toString();
    }
}

