package gyurix.economy.custom;

import gyurix.economy.BalanceData;
import gyurix.spigotlib.SU;

import java.math.BigDecimal;
import java.util.UUID;

public class ExpBalanceType extends BalanceType {
    public ExpBalanceType(BalanceData bd) {
        super(bd);
    }

    @Override
    public BigDecimal get(UUID plr) {
        return new BigDecimal(SU.getPlayer(plr).getLevel());
    }

    @Override
    public boolean set(UUID plr, BigDecimal value) {
        SU.getPlayer(plr).setLevel(value.intValue());
        return true;
    }
}
