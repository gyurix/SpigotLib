package gyurix.economy.custom;

import gyurix.economy.BalanceData;
import gyurix.spigotlib.SU;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceType extends BalanceData {
  protected BalanceTypeWrapper wrapper;

  public BalanceType(BalanceData bd, BalanceTypeWrapper wrapper) {
    super(bd);
    this.wrapper = wrapper;
  }

  public BalanceType(BalanceData bd) {
    super(bd);
  }

  public BigDecimal get(UUID plr) {
    if (wrapper != null)
      return wrapper.get(plr);
    BigDecimal bd = SU.getPlayerConfig(plr).getObject("balance." + name, BigDecimal.class);
    return bd == null ? defaultValue : bd;
  }

  public BigDecimal getBank(String bank) {
    BigDecimal bd = SU.pf.getObject("bankbalance." + bank + "." + name, BigDecimal.class);
    return bd == null ? defaultValue : bd;
  }

  public boolean set(UUID plr, BigDecimal value) {
    if (wrapper != null)
      return wrapper.set(plr, value);
    SU.getPlayerConfig(plr).setObject("balance." + name, value);
    return true;
  }

  public boolean setBank(String bank, BigDecimal value) {
    SU.pf.setObject("bankbalance." + bank + "." + name, value);
    return true;
  }
}
