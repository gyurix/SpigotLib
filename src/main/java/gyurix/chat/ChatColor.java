package gyurix.chat;

import java.util.HashMap;

/**
 * An enum for handling chat colors;
 */
public enum ChatColor {
    black('0'), dark_blue('1'), dark_green('2'), dark_aqua('3'), dark_red('4'), dark_purple('5'), gold('6'), gray('7'),
    dark_gray('8'), blue('9'), green('a'), aqua('b'), red('c'), light_purple('d'), yellow('e'), white('f'),
    obfuscated('k', true), bold('l', true), strikethrough('m', true), underline('n', true), italic('o', true), reset('r');

    static final HashMap<Character, ChatColor> byId = new HashMap<>();

    static {
        for (ChatColor cc : values()) {
            byId.put(cc.getId(), cc);
        }
    }

    final boolean format;
    final char id;

    ChatColor(char id) {
        this.id = id;
        format = false;
    }

    ChatColor(char id, boolean format) {
        this.id = id;
        this.format = format;
    }

    public static ChatColor forId(char id) {
        ChatColor cc = byId.get(id);
        if (cc == null)
            cc = white;
        return cc;
    }

    public char getId() {
        return id;
    }

    public boolean isFormat() {
        return format;
    }
}
