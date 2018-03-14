package gyurix.spigotutils;

import gyurix.protocol.Reflection;
import gyurix.scoreboard.NametagBar;
import gyurix.scoreboard.ScoreboardAPI;
import gyurix.scoreboard.Sidebar;
import gyurix.scoreboard.Tabbar;
import gyurix.spigotlib.SU;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

import static gyurix.spigotutils.ServerVersion.v1_9;

/**
 * Created by GyuriX on 9/6/2016.
 */
public class PlayerData {
    public static final int maxslot = Reflection.ver.isAbove(v1_9) ? 41 : 40;
    public boolean allowFlight, isFlying;
    public Sidebar board;
    public String displayName, playerListName;
    public GameMode gm;
    public double hp, maxhp;
    public ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
    public int level, foodLevel;
    public Location loc, compassTarget;
    public NametagBar nametagBar;
    public ArrayList<PotionEffect> pes = new ArrayList<PotionEffect>();
    public String pln;
    public Tabbar tabBar;
    public float xp, saturation, exhausion, walkSpeed, flySpeed;

    public PlayerData(Player plr, GameMode setGm, Location setLoc) {
        pln = plr.getName();
        loc = plr.getLocation();
        xp = plr.getExp();
        saturation = plr.getSaturation();
        exhausion = plr.getExhaustion();
        hp = plr.getHealth();
        maxhp = plr.getMaxHealth();
        level = plr.getLevel();
        foodLevel = plr.getFoodLevel();
        PlayerInventory pi = plr.getInventory();
        for (int i = 0; i < maxslot; i++) {
            inv.add(pi.getItem(i));
            pi.setItem(i, null);
        }
        for (PotionEffect pe : plr.getActivePotionEffects()) {
            pes.add(pe);
            plr.removePotionEffect(pe.getType());
        }
        gm = plr.getGameMode();
        allowFlight = plr.getAllowFlight();
        isFlying = plr.isFlying();
        displayName = plr.getDisplayName();
        playerListName = plr.getPlayerListName();
        board = (Sidebar) ScoreboardAPI.sidebars.get(plr.getName()).active;
        nametagBar = (NametagBar) ScoreboardAPI.nametags.get(plr.getName()).active;
        tabBar = (Tabbar) ScoreboardAPI.tabbars.get(plr.getName()).active;
        walkSpeed = plr.getWalkSpeed();
        flySpeed = plr.getFlySpeed();
        compassTarget = plr.getCompassTarget();
        plr.teleport(setLoc);
        plr.setGameMode(setGm);
        plr.setFoodLevel(20);
    }

    public void restore() {
        Player plr = SU.getPlayer(pln);
        plr.teleport(loc);
        plr.setLevel(level);
        plr.setExp(xp);
        plr.setSaturation(saturation);
        plr.setExhaustion(exhausion);
        plr.setMaxHealth(maxhp);
        plr.setHealth(hp);
        plr.setFoodLevel(foodLevel);
        PlayerInventory pi = plr.getInventory();
        for (int i = 0; i < maxslot; i++)
            pi.setItem(i, inv.get(i));
        for (PotionEffect pe : plr.getActivePotionEffects())
            plr.removePotionEffect(pe.getType());
        for (PotionEffect pe : pes)
            plr.addPotionEffect(pe);
        plr.setGameMode(gm);
        plr.setAllowFlight(allowFlight);
        plr.setFlying(isFlying);
        plr.setDisplayName(displayName);
        plr.setWalkSpeed(walkSpeed);
        plr.setFlySpeed(flySpeed);
        plr.setCompassTarget(compassTarget);
        plr.setPlayerListName(playerListName);
        ScoreboardAPI.setSidebar(plr, board);
        ScoreboardAPI.setNametagBar(plr, nametagBar);
        ScoreboardAPI.setTabbar(plr, tabBar);
    }
}
