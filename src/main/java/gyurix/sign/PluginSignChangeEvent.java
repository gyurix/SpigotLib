//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package gyurix.sign;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class PluginSignChangeEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final String[] lines;
    private boolean cancel;

    public PluginSignChangeEvent(Block block, String[] lines) {
        super(block);
        this.lines = lines;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {
        return lines[index];
    }

    public String[] getLines() {
        return lines;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        lines[index] = line;
    }
}
