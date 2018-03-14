package gyurix.commands.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PostTabCompleteEvent extends CommandEvent {
    private static final HandlerList hl = new HandlerList();
    private List<String> result;

    public PostTabCompleteEvent(CommandSender sender, String command, List<String> result) {
        super(sender, command);
        this.result = result;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }


    @Override
    public HandlerList getHandlers() {
        return hl;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }
}

