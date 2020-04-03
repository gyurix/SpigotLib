package gyurix.economy;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.PostLoadable;
import gyurix.spigotutils.NullUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Data
public class BalanceData implements PostLoadable {
  /**
   * Default amount of this balance, what new players and new banks get
   */
  protected BigDecimal defaultValue = new BigDecimal(0);
  /**
   * DecimalFormat used for formatting
   */
  protected String format;
  /**
   * The whole name of this balance type
   */
  protected String fullName;
  /**
   * Name of the balance type
   */
  protected String name;
  /**
   * Text written before amount
   */
  protected String prefix = "";
  /**
   * Text written before amount, if amount is plural
   */
  protected String prefixPlural;
  /**
   * Text written after amount, if amount
   */
  protected String suffix = "";
  /**
   * Text written after amount, if amount is plural
   */
  protected String suffixPlural;
  /**
   * Use K (10^3), M (10^6), B (10^9), T (10^12) suffixes instead of
   * the whole balance amount
   */
  protected boolean useKMBT;

  @ConfigOptions(serialize = false)
  private DecimalFormat df;

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
    postLoad();
  }

  /**
   * Constructs a new BalanceData, by copying all
   * the properties of the given BalanceData
   *
   * @param bd - The BalanceData which having the copyable properties
   */
  public BalanceData(BalanceData bd) {
    this.name = bd.name;
    this.defaultValue = bd.defaultValue;
    this.format = bd.format;
    this.fullName = bd.fullName;
    this.prefix = bd.prefix;
    this.prefixPlural = bd.prefixPlural;
    this.suffix = bd.suffix;
    this.suffixPlural = bd.suffixPlural;
    this.useKMBT = bd.useKMBT;
    postLoad();
  }

  /**
   * Formats the given amount of balance with applying both prefix and suffix
   *
   * @param amount - The balance amount
   * @return The formatting result
   */
  public String format(BigDecimal amount) {
    if (amount == null)
      amount = new BigDecimal(0);
    boolean pl = !(amount.compareTo(new BigDecimal(-1)) >= 0 && amount.compareTo(new BigDecimal(1)) <= 0);
    String pf = NullUtils.to0(pl ? getPrefixPlural() : prefix);
    String sf = NullUtils.to0(pl ? getSuffixPlural() : suffix);
    String f = getKMBT(amount);
    return pf + f + sf;
  }

  /**
   * Formats the given amount of balance without applying prefix or suffix
   *
   * @param amount - The balance amount
   * @return The formatting result
   */
  public String formatRaw(BigDecimal amount) {
    return getKMBT(amount == null ? new BigDecimal(0) : amount);
  }

  private String getKMBT(BigDecimal amount) {
    if (useKMBT) {
      BigDecimal qi = new BigDecimal(1000_000_000_000_000_000L);
      BigDecimal q = new BigDecimal(1000_000_000_000_000L);
      BigDecimal t = new BigDecimal(1000_000_000_000L);
      BigDecimal b = new BigDecimal(1000_000_000L);
      BigDecimal m = new BigDecimal(1000_000L);
      BigDecimal k = new BigDecimal(1000L);
      if (amount.compareTo(qi) > -1)
        return df.format(amount.divide(q, BigDecimal.ROUND_DOWN)) + "Qi";
      if (amount.compareTo(q) > -1)
        return df.format(amount.divide(q, BigDecimal.ROUND_DOWN)) + "Q";
      if (amount.compareTo(t) > -1)
        return df.format(amount.divide(t, BigDecimal.ROUND_DOWN)) + "T";
      if (amount.compareTo(b) > -1)
        return df.format(amount.divide(b, BigDecimal.ROUND_DOWN)) + "B";
      if (amount.compareTo(m) > -1)
        return df.format(amount.divide(m, BigDecimal.ROUND_DOWN)) + "M";
      if (amount.compareTo(k) > -1)
        return df.format(amount.divide(k, BigDecimal.ROUND_DOWN)) + "K";
    }
    return df.format(amount.toString());
  }

  public String getPrefixPlural() {
    return NullUtils.to0(prefixPlural != null ? prefixPlural : prefix);
  }

  public String getSuffixPlural() {
    return NullUtils.to0(suffixPlural != null ? suffixPlural : suffix);
  }

  @Override
  public void postLoad() {
    df = new DecimalFormat(format == null ? "###,###,###,###,###,###,##0.00" : format);
  }
}