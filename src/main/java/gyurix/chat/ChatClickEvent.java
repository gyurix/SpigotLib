package gyurix.chat;

public class ChatClickEvent {
    public ChatClickEventType action;
    public String value;

    public ChatClickEvent() {
    }

    public ChatClickEvent(ChatClickEventType action, String value) {
        this.action = action;
        this.value = value;
    }
}

