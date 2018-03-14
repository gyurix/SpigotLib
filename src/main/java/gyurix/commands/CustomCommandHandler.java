package gyurix.commands;

import org.bukkit.command.CommandSender;

public interface CustomCommandHandler {
    boolean handle(CommandSender cs, String text, Object... args);
}

