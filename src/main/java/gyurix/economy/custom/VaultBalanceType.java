package gyurix.economy.custom;

import gyurix.economy.BalanceData;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.UUID;

public class VaultBalanceType extends BalanceType {
  public VaultBalanceType(BalanceData bd) {
    super(bd);
  }

  @Override
  public BigDecimal get(UUID plr) {
    return new BigDecimal(SU.econ.getBalance(Bukkit.getOfflinePlayer(plr)));
  }

  @Override
  public BigDecimal getBank(String bank) {
    return new BigDecimal(SU.econ.bankBalance(bank).balance);
  }

  @Override
  public boolean set(UUID plr, BigDecimal value) {
    BigDecimal has = get(plr);
    int out = has.compareTo(value);
    if (out == 0)
      return true;
    if (out < 0)
      return SU.econ.depositPlayer(Bukkit.getOfflinePlayer(plr), value.subtract(has).doubleValue()).transactionSuccess();
    return SU.econ.withdrawPlayer(Bukkit.getOfflinePlayer(plr), has.subtract(value).doubleValue()).transactionSuccess();
  }

  @Override
  public boolean setBank(String bank, BigDecimal value) {
    BigDecimal has = getBank(bank);
    int out = has.compareTo(value);
    if (out == 0)
      return true;
    if (out < 0)
      return SU.econ.bankWithdraw(bank, has.subtract(value).doubleValue()).transactionSuccess();
    return SU.econ.bankDeposit(bank, value.subtract(has).doubleValue()).transactionSuccess();
  }
}
