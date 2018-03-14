package gyurix.economy;

import gyurix.economy.EconomyAPI.BalanceData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

public final class BankBalanceUpdateEvent
        extends Event
        implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final BalanceData balanceType;
    private final BigDecimal before;
    private final String target;
    private BigDecimal after;
    private boolean cancel;

    BankBalanceUpdateEvent(String target, BigDecimal before, BigDecimal after, BalanceData balanceType) {
        this.target = target;
        this.before = before;
        this.after = after;
        this.balanceType = balanceType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BigDecimal getAfter() {
        return after;
    }

    public void setAfter(BigDecimal after) {
        this.after = after;
    }

    public BalanceData getBalanceType() {
        return balanceType;
    }

    public BigDecimal getBefore() {
        return before;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public String getTarget() {
        return target;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}

