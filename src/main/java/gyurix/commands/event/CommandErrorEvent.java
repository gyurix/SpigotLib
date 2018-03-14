package gyurix.commands.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class CommandErrorEvent extends CommandEvent {
    private static final HandlerList hl = new HandlerList();
    private final Throwable error;

    public CommandErrorEvent(CommandSender sender, String command, Throwable error) {
        super(sender, command);
        this.error = error;
    }

    public static HandlerList getHandlerList() {
        return hl;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public HandlerList getHandlers() {
        return hl;
    }
}

