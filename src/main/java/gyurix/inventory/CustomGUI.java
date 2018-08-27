package gyurix.inventory;

import org.bukkit.entity.Player;

public abstract class CustomGUI extends CloseableGUI {
    public CustomGUI(Player plr) {
        super(plr);
    }

    public abstract void onClick(int slot, boolean right, boolean shift);
}
