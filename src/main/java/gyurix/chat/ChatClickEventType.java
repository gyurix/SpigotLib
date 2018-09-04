package gyurix.chat;

import net.md_5.bungee.api.chat.ClickEvent;

import java.util.HashMap;

public enum ChatClickEventType {
    open_url('U'),
    open_file('F'),
    twitch_user_info('D'),
    run_command('R'),
    change_page('P'),
    suggest_command('S');
    static final HashMap<Character, ChatClickEventType> byId = new HashMap<>();

    static {
        for (ChatClickEventType t : values()) {
            byId.put(t.id, t);
        }
    }

    final char id;

    ChatClickEventType(char id) {
        this.id = id;
    }

    public static ChatClickEventType forId(char id) {
        return byId.get(id);
    }

    public char getId() {
        return id;
    }

    public ClickEvent.Action toSpigotClickAction() {
        return ClickEvent.Action.valueOf(name().toUpperCase());
    }
}

