package gyurix.chat;

public class ChatHoverEvent {
    public ChatHoverEventType action;
    public ChatTag value;

    public ChatHoverEvent(ChatHoverEventType action, String value) {
        this.action = action;
        this.value = action == ChatHoverEventType.show_text ? ChatTag.fromColoredText(value) : new ChatTag(value);
    }
}

