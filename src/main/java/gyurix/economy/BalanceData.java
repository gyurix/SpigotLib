package gyurix.economy;

import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Data
public class BalanceData {
    /**
     * Name of the balance type
     */
    private final String name;

    /**
     * Default amount of this balance, what new players and new banks get
     */
    private BigDecimal defaultValue = new BigDecimal(0);

    /**
     * DecimalFormat used for formatting
     */
    private String format;

    /**
     * The whole name of this balance type
     */
    private String fullName;

    /**
     * Text written before amount
     */
    private String prefix = "";

    /**
     * Text written before amount, if amount is plural
     */
    private String prefixPlural;

    /**
     * Text written after amount, if amount
     */
    private String suffix = "";

    /**
     * Text written after amount, if amount is plural
     */
    private String suffixPlural;

    /**
     * Use K (10^3), M (10^6), B (10^9), T (10^12) suffixes instead of
     * the whole balance amount
     */
    private boolean useKMBT;

    private BalanceData() {
        this.name = null;
    }

    /**
     * Constructs a new BalanceData, sets it's name and full name
     * to the balance type represented by this BalanceData
     *
     * @param name - The balance type represented by this BalanceData
     */
    public BalanceData(String name) {
        this.name = name;
        this.fullName = name;
    }

    /**
     * Formats the given amount of balance
     *
     * @param amount - The balance amount
     * @return The formatting result
     */
    public String format(BigDecimal amount) {
        if (amount == null)
            amount = new BigDecimal(0);
        boolean pl = !(amount.compareTo(new BigDecimal(-1)) >= 0 && amount.compareTo(new BigDecimal(1)) <= 0);
        String pf = pl ? getPrefixPlural() : prefix;
        String sf = pl ? getSuffixPlural() : suffix;
        String f = useKMBT ? getKMBT(amount) : format == null ? amount.toString() : new DecimalFormat(format).format(amount);
        return pf + f + sf;
    }

    private String getKMBT(BigDecimal amount) {
        BigDecimal t = new BigDecimal(1000_000_000_000L);
        BigDecimal b = new BigDecimal(1000_000_000L);
        BigDecimal m = new BigDecimal(1000_000L);
        BigDecimal k = new BigDecimal(1000L);
        if (amount.compareTo(t) > -1)
            return amount.divide(t, BigDecimal.ROUND_DOWN).longValue() + "T";
        if (amount.compareTo(b) > -1)
            return amount.divide(b, BigDecimal.ROUND_DOWN).longValue() + "B";
        if (amount.compareTo(m) > -1)
            return amount.divide(m, BigDecimal.ROUND_DOWN).longValue() + "M";
        if (amount.compareTo(k) > -1)
            return amount.divide(k, BigDecimal.ROUND_DOWN).longValue() + "K";
        return amount.toString();
    }

    public String getPrefixPlural() {
        return prefixPlural != null ? prefixPlural : prefix;
    }

    public String getSuffixPlural() {
        return suffixPlural != null ? suffixPlural : suffix;
    }
}