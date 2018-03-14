package gyurix.economy;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple class for vault compatibility.
 */
public class EconomyVaultHook implements Economy {
    /**
     * Initializes Vault hook, DO NOT USE THIS METHOD.
     */
    public static void init() {
        Bukkit.getServicesManager().register(Economy.class, new EconomyVaultHook(), Main.pl, ServicePriority.Highest);
    }

    public EconomyResponse bankMemberOwner(String bankName) {
        return new EconomyResponse(0, 0, ResponseType.FAILURE, "§cBanks doesn't have members and owners.");
    }

    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return "SpigotLib - EconomyAPI";
    }

    public boolean hasBankSupport() {
        return true;
    }

    public int fractionalDigits() {
        return Integer.MAX_VALUE;
    }

    public String format(double v) {
        return EconomyAPI.balanceTypes.get("default").format(new BigDecimal(v));
    }

    public String currencyNamePlural() {
        return EconomyAPI.balanceTypes.get("default").suffix;
    }

    public String currencyNameSingular() {
        return EconomyAPI.balanceTypes.get("default").suffix;
    }

    public boolean hasAccount(String s) {
        return true;
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    public boolean hasAccount(String s, String s1) {
        return true;
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return true;
    }

    public double getBalance(String s) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue();
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        UUID id = offlinePlayer.getUniqueId();
        if (id == null)
            id = SU.getUUID(offlinePlayer.getName());
        return EconomyAPI.getBalance(id).doubleValue();
    }

    public double getBalance(String s, String world) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue();
    }

    public double getBalance(OfflinePlayer offlinePlayer, String world) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue();
    }

    public boolean has(String s, double v) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue() >= v;
    }

    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue() >= v;
    }

    public boolean has(String player, String world, double v) {
        return EconomyAPI.getBalance(SU.getUUID(player)).doubleValue() >= v;
    }

    public boolean has(OfflinePlayer offlinePlayer, String world, double v) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue() >= v;
    }

    public EconomyResponse withdrawPlayer(String player, double v) {
        UUID id = SU.getUUID(player);
        boolean success = EconomyAPI.addBalance(id, new BigDecimal(0 - v));
        return new EconomyResponse(v, EconomyAPI.getBalance(id).doubleValue(),
                success ? ResponseType.SUCCESS : ResponseType.FAILURE, success ? "§aSuccess." : "§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        UUID id = offlinePlayer.getUniqueId();
        if (id == null)
            id = SU.getUUID(offlinePlayer.getName());
        boolean success = EconomyAPI.addBalance(id, new BigDecimal(0 - v));
        return new EconomyResponse(v, EconomyAPI.getBalance(id).doubleValue(),
                success ? ResponseType.SUCCESS : ResponseType.FAILURE, success ? "§aSuccess." : "§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(String player, String world, double v) {
        UUID id = SU.getUUID(player);
        boolean success = EconomyAPI.addBalance(id, new BigDecimal(0 - v));
        return new EconomyResponse(v, EconomyAPI.getBalance(id).doubleValue(),
                success ? ResponseType.SUCCESS : ResponseType.FAILURE, success ? "§aSuccess." : "§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        UUID id = offlinePlayer.getUniqueId();
        if (id == null)
            id = SU.getUUID(offlinePlayer.getName());
        boolean success = EconomyAPI.addBalance(id, new BigDecimal(0 - v));
        return new EconomyResponse(v, EconomyAPI.getBalance(id).doubleValue(),
                success ? ResponseType.SUCCESS : ResponseType.FAILURE, success ? "§aSuccess." : "§cNot enough money.");
    }

    public EconomyResponse depositPlayer(String player, double v) {
        return withdrawPlayer(player, 0 - v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        return withdrawPlayer(offlinePlayer, 0 - v);
    }

    public EconomyResponse depositPlayer(String player, String world, double v) {
        return withdrawPlayer(player, v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    public EconomyResponse createBank(String bankName, String s1) {
        return new EconomyResponse(0, 0, ResponseType.SUCCESS, "§2Banks are handled automatically, there is no need to create them.");
    }

    public EconomyResponse createBank(String bankName, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, ResponseType.SUCCESS, "§2Banks are handled automatically, there is no need to create them.");
    }

    public EconomyResponse deleteBank(String bankName) {
        double bal = EconomyAPI.getBankBalance(bankName).doubleValue();
        SU.pf.removeData("bankbalance." + bankName);
        return new EconomyResponse(0, bal, ResponseType.SUCCESS, "§2Bank §a§l" + bankName + "§2 has been removed successfully.");
    }

    public EconomyResponse bankBalance(String bankName) {
        return new EconomyResponse(0, EconomyAPI.getBankBalance(bankName).doubleValue(), ResponseType.SUCCESS, "§aSuccess.");
    }

    public EconomyResponse bankHas(String bankName, double v) {
        double bal = EconomyAPI.getBankBalance(bankName).doubleValue();
        if (v > bal) {
            return new EconomyResponse(v, bal, ResponseType.FAILURE, "§cNot enough money.");
        }
        return new EconomyResponse(v, bal, ResponseType.SUCCESS, "§aSuccess.");
    }

    public EconomyResponse bankWithdraw(String bankName, double v) {
        EconomyAPI.addBankBalance(bankName, new BigDecimal(0 - v));
        return new EconomyResponse(v, EconomyAPI.getBankBalance(bankName).doubleValue(), ResponseType.SUCCESS, "§aSuccess.");
    }

    public EconomyResponse bankDeposit(String bankName, double v) {
        EconomyAPI.addBankBalance(bankName, new BigDecimal(v));
        return new EconomyResponse(v, EconomyAPI.getBankBalance(bankName).doubleValue(), ResponseType.SUCCESS, "§aSuccess.");
    }

    public EconomyResponse isBankOwner(String bankName, String s1) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankOwner(String bankName, OfflinePlayer offlinePlayer) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankMember(String bankName, String s1) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankMember(String bankName, OfflinePlayer offlinePlayer) {
        return bankMemberOwner(bankName);
    }

    public List<String> getBanks() {
        return new ArrayList<String>(SU.pf.getStringKeyList("bankbalance"));
    }

    public boolean createPlayerAccount(String s) {
        return false;
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
