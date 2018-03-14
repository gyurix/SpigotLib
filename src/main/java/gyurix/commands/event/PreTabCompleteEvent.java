package gyurix.commands.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class PreTabCompleteEvent extends CommandEvent {
    private static final HandlerList hl = new HandlerList();
    private List<String> result = new ArrayList<>();

    public PreTabCompleteEvent(CommandSender sender, String command) {
        super(sender, command);
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

