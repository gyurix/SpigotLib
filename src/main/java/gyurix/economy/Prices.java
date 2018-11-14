package gyurix.economy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Class used for managing multiple typed balance cost items
 */
public class Prices implements Iterable<Price> {
    private final List<Price> prices = new ArrayList<>();

    /**
     * Creates a new Prices instance, generates the price list
     * according to the input parameters
     *
     * @param types - The used balance types
     * @param cost  - Space separated list of costs
     */
    public Prices(List<String> types, String cost) {
        int len = types.size();
        String[] d = cost.split(" ");
        for (int i = 0; i < len; ++i)
            prices.add(new Price(types.get(i), d[i]));

    }

    /**
     * Returns an alphabetically ordered comma separated list of
     * balance types of which the given Player does not have enough
     *
     * @param plr - Target Player
     * @return - An alphabetically ordered comma separated list of
     * balance types of which the given Player does not have enough
     */
    public String getNotEnough(Player plr) {
        TreeSet<String> notHave = new TreeSet<>();
        for (Price p : prices)
            if (!p.has(plr))
                notHave.add(EconomyAPI.getBalanceType(p.getType()).getFullName());
        return StringUtils.join(notHave, ", ");
    }

    /**
     * Give all the prices to the given player
     *
     * @param plr
     */
    public void give(Player plr) {
        for (Price p : prices)
            p.give(plr);
    }

    /**
     * Checks if the given player is able to pay all the prices,
     * this method should be used before using the take method
     *
     * @param plr - Target Player
     * @return true if the player is able to pay all the prices, false otherwise
     */
    public boolean has(Player plr) {
        for (Price p : prices)
            if (!p.has(plr))
                return false;
        return true;
    }

    /**
     * Iterates through the prices
     *
     * @return The Price iterator
     */
    @Override
    public Iterator<Price> iterator() {
        return prices.iterator();
    }

    /**
     * Takes all the prices from the give Player
     *
     * @param plr - Target Player
     */
    public void take(Player plr) {
        for (Price p : prices)
            p.take(plr);
    }

    /**
     * Returns a comma separated list of prices
     */
    @Override
    public String toString() {
        return StringUtils.join(prices, ", ");
    }
}
