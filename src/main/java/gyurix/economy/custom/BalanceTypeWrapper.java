package gyurix.economy.custom;

import java.math.BigDecimal;
import java.util.UUID;

public interface BalanceTypeWrapper {
  BigDecimal get(UUID plr);

  boolean set(UUID plr, BigDecimal value);
}
