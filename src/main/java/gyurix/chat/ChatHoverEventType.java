package gyurix.chat;

import java.util.HashMap;

/**
 * Created by gyurix on 22/11/2015.
 */
public enum ChatHoverEventType {
    show_text('T'), show_item('I'), show_achievement('A'), show_entity('E');
    static final HashMap<Character, ChatHoverEventType> byId = new HashMap<>();

    static {
        for (ChatHoverEventType t : values()) {
            byId.put(t.id, t);
        }
    }

    final char id;

    ChatHoverEventType(char id) {
        this.id = id;
    }

    public static ChatHoverEventType forId(char id) {
        return byId.get(id);
    }

    public char getId() {
        return id;
    }


}
