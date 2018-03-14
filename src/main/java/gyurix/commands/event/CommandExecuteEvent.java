package gyurix.commands.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class CommandExecuteEvent extends CommandEvent {
    private static final HandlerList hl = new HandlerList();

    public CommandExecuteEvent(CommandSender sender, String command) {
        super(sender, command);
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    @Override
    public HandlerList getHandlers() {
        return hl;
    }
}

