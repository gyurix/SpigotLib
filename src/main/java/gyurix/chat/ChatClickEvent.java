package gyurix.chat;

import net.md_5.bungee.api.chat.ClickEvent;

public class ChatClickEvent {
    public ChatClickEventType action;
    public String value;

    public ChatClickEvent() {
    }

    public ChatClickEvent(ChatClickEventType action, String value) {
        this.action = action;
        this.value = value;
    }

    public ChatClickEvent(ClickEvent spigotClick) {
        action = ChatClickEventType.valueOf(spigotClick.getAction().name().toLowerCase());
        value = spigotClick.getValue();
    }

    public ClickEvent toSpigotClickEvent() {
        return new ClickEvent(action.toSpigotClickAction(), value);
    }
}

