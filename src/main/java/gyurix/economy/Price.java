package gyurix.economy;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

/**
 * Class for managing a single balance type - value pair
 */
@Getter
public class Price {
    private String type;
    private BigDecimal value;

    public Price(String type, String value) {
        this.type = type;
        this.value = new BigDecimal(value);
    }

    /**
     * Gives the amount of money represented by this Price object to the given Player.
     *
     * @param plr - Target Player
     */
    public void give(Player plr) {
        EconomyAPI.addBalance(plr.getUniqueId(), type, value);
    }

    /**
     * Checks if the given Player has the amount of money represented by this Price object
     *
     * @param plr - Target Player
     * @return True of the Player has enough money, false otherwise
     */
    public boolean has(Player plr) {
        return EconomyAPI.getBalance(plr.getUniqueId(), type).compareTo(value) >= 0;
    }

    /**
     * Takes the amount of money represented by this Price object from the given Player.
     * You should use the has method before using this one.
     *
     * @param plr - Target Player
     */
    public void take(Player plr) {
        EconomyAPI.addBalance(plr.getUniqueId(), type, new BigDecimal("-" + value));
    }

    /**
     * Gets the formatted String of this Price according to the balanceTypes configuration settings
     *
     * @return The formatted String of this Price
     */
    @Override
    public String toString() {
        return EconomyAPI.getBalanceType(type).format(value);
    }
}
