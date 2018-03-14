package gyurix.commands;

import org.bukkit.command.CommandSender;

/**
 * Created by gyurix on 20/12/2015.
 */
public class DelayedCommandExecutor implements Runnable {
    private final Object[] args;
    private final Command c;
    private final CommandSender sender;

    public DelayedCommandExecutor(Command c, CommandSender sender, Object[] args) {
        this.args = args;
        this.sender = sender;
        this.c = c;
    }

    @Override
    public void run() {
        c.executeNow(sender, args);
    }
}
