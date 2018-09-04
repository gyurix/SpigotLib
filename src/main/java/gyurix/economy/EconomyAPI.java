package gyurix.economy;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.SU;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

import static gyurix.spigotlib.Config.debug;

/**
 * API used for managing multiple balance types on the server
 */
public class EconomyAPI {
    private static HashMap<String, BalanceData> balanceTypes = new HashMap<>();

    @ConfigOptions(comment = "Migrate all the Economy data through Vault from an other Economy plugin, i.e. Essentials.")
    @Getter
    @Setter
    private static boolean migrate;

    @ConfigOptions(comment = "The type of SpigotLibs vault hook, available options:\n" +
            "NONE - do NOT hook to Vault at all (not suggested)\n" +
            "USER - hook to Vault as an Economy user (suggested if you don't want to use SpigotLibs Economy management)\n" +
            "PROVIDER - hook to Vault as an Economy provider (override other Economy plugins, like Essentials)")
    @Getter
    @Setter
    private static VaultHookType vaultHookType;

    /**
     * Adds / Takes the given typed balance from players account
     *
     * @param plr         - Target players UUID
     * @param balanceType - Balance type
     * @param balance     - Amount to add (positive values) or take (negative values)
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean addBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return addBalance(plr, balance);
            BigDecimal bd = getBalance(plr, balanceType).add(balance);
            return bd.compareTo(new BigDecimal(0)) >= 0 && setBalance(plr, balanceType, bd);
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on adding " + balance + ' ' + balanceType + " balance to player " + plr + '.');
            debug.msg("Economy", e);
            return false;
        }
    }

    /**
     * Adds / Takes default balance from players account
     *
     * @param plr     - Target players UUID
     * @param balance - Amount to add (positive values) or take (negative values)
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean addBalance(UUID plr, BigDecimal balance) {
        try {
            if (useVaultProvider()) {
                if (balance.compareTo(new BigDecimal(0)) < 0)
                    return SU.econ.withdrawPlayer(Bukkit.getOfflinePlayer(plr), -balance.doubleValue()).transactionSuccess();
                return SU.econ.depositPlayer(Bukkit.getOfflinePlayer(plr), balance.doubleValue()).transactionSuccess();
            }
            BigDecimal bd = getBalance(plr).add(balance);
            return bd.compareTo(new BigDecimal(0)) >= 0 && setBalance(plr, bd);
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on adding " + balance + " default balance to player " + plr + '.');
            debug.msg("Economy", e);
            return false;
        }
    }

    /**
     * Adds / Takes default typed balance from bank accounts
     *
     * @param bank    - The banks name
     * @param balance - Amount to add (positive values) or take (negative values)
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean addBankBalance(String bank, BigDecimal balance) {
        return setBankBalance(bank, getBankBalance(bank).add(balance));
    }

    /**
     * Adds / Takes the given typed balance from bank accounts
     *
     * @param bank        - The banks name
     * @param balanceType - Balance type
     * @param balance     - Amount to add (positive values) or take (negative values)
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean addBankBalance(String bank, String balanceType, BigDecimal balance) {
        return setBankBalance(bank, balanceType, getBankBalance(bank, balanceType).add(balance));
    }

    /**
     * Get the given typed balance of the given player
     *
     * @param plr         - Target Player
     * @param balanceType - Balance type
     * @return The given typed balance of the given player or 0 if there was an error
     */
    public static BigDecimal getBalance(UUID plr, String balanceType) {
        try {
            if (balanceType.equals("default"))
                return getBalance(plr);
            else if (balanceType.equals("exp"))
                return new BigDecimal(SU.getPlayer(plr).getLevel());
            BigDecimal bal = SU.getPlayerConfig(plr).get("balance." + balanceType, BigDecimal.class);
            if (bal == null) {
                bal = balanceTypes.get(balanceType).getDefaultValue();
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, bal);
            }
            return bal;
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on getting " + balanceType + " balance of player " + plr);
            debug.msg("Economy", e);
            return new BigDecimal(0);
        }
    }

    /**
     * Get the default typed balance of the given player
     *
     * @param plr - Target Player
     * @return The given typed balance of the given player or 0 if there was an error
     */
    public static BigDecimal getBalance(UUID plr) {
        try {
            if (useVaultProvider())
                return new BigDecimal(SU.econ.getBalance(Bukkit.getOfflinePlayer(plr)));
            return SU.getPlayerConfig(plr).get("balance.default", BigDecimal.class);
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on getting default balance of player " + plr);
            debug.msg("Economy", e);
            return new BigDecimal(0);
        }
    }

    /**
     * Get the settings of the given balance type
     *
     * @param type - Target balance type
     * @return - The settings of the given balance type
     */
    public static BalanceData getBalanceType(String type) {
        BalanceData bd = balanceTypes.get(type);
        return bd == null ? new BalanceData(type) : bd;
    }

    /**
     * Gets the default typed balance of the given bank
     *
     * @param bank - The banks name
     * @return The amount of default typed balance what the given bank has or 0 if there was an error
     */
    public static BigDecimal getBankBalance(String bank) {
        try {
            if (useVaultProvider())
                return new BigDecimal(SU.econ.bankBalance(bank).balance);
            return SU.pf.get("bankbalance." + bank + ".default", BigDecimal.class);
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on getting default balance of bank " + bank);
            debug.msg("Economy", e);
            return new BigDecimal(0);
        }
    }

    /**
     * Gets the given typed balance of the given bank
     *
     * @param bank        - The banks name
     * @param balanceType - Balance type
     * @return The amount of the given typed balance what the given bank has or 0 if there was an error
     */
    public static BigDecimal getBankBalance(String bank, String balanceType) {
        try {
            if (useVaultProvider())
                return new BigDecimal(SU.econ.bankBalance(bank).balance);
            return SU.pf.get("bankbalance." + bank + '.' + balanceType, BigDecimal.class);
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on getting " + balanceType + " balance of bank " + bank);
            debug.msg("Economy", e);
            return new BigDecimal(0);
        }
    }

    /**
     * @param sender
     * @param receiver
     * @param balance
     * @return
     */
    public static boolean sendBalance(UUID sender, UUID receiver, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBalance(receiver, balance);
        return true;
    }

    /**
     * @param sender
     * @param receiver
     * @param balanceType
     * @param balance
     * @return
     */
    public static boolean sendBalance(UUID sender, UUID receiver, String balanceType, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBalance(receiver, balanceType, balance);
        return true;
    }

    /**
     * @param sender
     * @param bank
     * @param balance
     * @return
     */
    public static boolean sendBalanceToBank(UUID sender, String bank, BigDecimal balance) {
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBankBalance(bank, balance);
        return true;
    }

    /**
     * @param sender
     * @param bank
     * @param balanceType
     * @param balance
     * @return
     */
    public static boolean sendBalanceToBank(UUID sender, String bank, String balanceType, BigDecimal balance) {
        if (!addBalance(sender, balanceType, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBankBalance(bank, balanceType, balance);
        return true;
    }

    /**
     * @param plr
     * @param balance
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean setBalance(UUID plr, BigDecimal balance) {
        try {
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, getBalance(plr), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (useVaultProvider())
                    return vaultSet(plr, balance);
                SU.getPlayerConfig(plr).setObject("balance.default", balance);
                return true;
            }
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting default balance of player " + plr + " to " + balance);
            debug.msg("Economy", e);
        }
        return false;
    }

    /**
     * @param plr
     * @param balanceType
     * @param balance
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean setBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return setBalance(plr, balance);
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, getBalance(plr, balanceType), balance, balanceTypes.get(balanceType));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (balanceType.equals("exp")) {
                    SU.getPlayer(plr).setLevel(balance.intValue());
                    return true;
                }
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, balance);
                return true;
            }
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting " + balanceType + " balance of player " + plr + " to " + balance);
            debug.msg("Economy", e);
        }
        return false;
    }

    /**
     * @param bank
     * @param balance
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean setBankBalance(String bank, BigDecimal balance) {
        try {
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, getBankBalance(bank), balance, getBalanceType("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (useVaultProvider())
                    return vaultSetBank(bank, balance);
                SU.pf.setObject("bankbalance." + bank + ".default", balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting default balance of bank " + bank + " to " + balance);
            debug.msg("Economy", e);
            return false;
        }
    }

    /**
     * @param bank
     * @param balanceType
     * @param balance
     * @return True if the transaction was successful, false otherwise
     */
    public static boolean setBankBalance(String bank, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return setBankBalance(bank, balance);
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, getBankBalance(bank), balance, getBalanceType(balanceType));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                SU.pf.setObject("bankbalance." + bank + '.' + balanceType, balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting " + balanceType + " balance of bank " + bank + " to " + balance);
            debug.msg("Economy", e);
            return false;
        }
    }


    private static boolean vaultSet(UUID id, BigDecimal bal) {
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(id);
            double now = SU.econ.getBalance(p);
            double dif = bal.doubleValue() - now;
            if (dif > 0) {
                return SU.econ.depositPlayer(p, dif).transactionSuccess();
            } else if (dif < 0) {
                return SU.econ.withdrawPlayer(p, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting default balance of player " + id + " to " + bal + " in economy " + SU.econ.getName() + '.');
            debug.msg("Economy", e);
            return false;
        }
    }

    private static boolean vaultSetBank(String bank, BigDecimal bal) {
        try {
            double now = getBankBalance(bank).doubleValue();
            double dif = bal.doubleValue() - now;
            if (dif > 0) {
                return SU.econ.bankDeposit(bank, dif).transactionSuccess();
            } else if (dif < 0) {
                return SU.econ.bankWithdraw(bank, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            debug.msg("Economy", "§cError on setting default balance of bank " + bank + " to " + bal + " in economy " + SU.econ.getName() + '.');
            debug.msg("Economy", e);
            return false;
        }
    }

    /**
     * Checks if plugin Vault should be used as the provider service for default balance type or not
     *
     * @return True if Vault should be used, false otherwise
     */
    public static boolean useVaultProvider() {
        return vaultHookType == VaultHookType.USER && SU.vault && SU.econ != null;
    }

    /**
     * Types of possible hooks to plugin Vault
     */
    public enum VaultHookType {
        /**
         * Do not hook to Vault at all
         */
        NONE,
        /**
         * Use Vault as an Economy provider service
         */
        USER,
        /**
         * Provide Economy services
         */
        PROVIDER
    }
}

