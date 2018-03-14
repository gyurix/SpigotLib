package gyurix.inventory.trade;

import com.google.common.collect.Lists;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.EntityUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

/**
 * An API for managing NPC trade menus
 */
public class TradeAPI implements Listener {
    /**
     * An instance of the AIR material, feel free to use it anywhere, just do NOT modify it
     */
    public static final ItemStack air = new ItemStack(Material.AIR);
    private static Class cmrCl = Reflection.getOBCClass("inventory.CraftMerchantRecipe");
    private static Constructor cmrC = Reflection.getConstructor(cmrCl, ItemStack.class, int.class, int.class, boolean.class);
    private static Class humanCl = Reflection.getNMSClass("EntityHuman");
    private static Class imc = Reflection.getNMSClass("IMerchant");
    private static Field ingredientsF = Reflection.getField(MerchantRecipe.class, "ingredients");
    private static Method openTradeM = Reflection.getMethod(humanCl, "openTrade", imc);
    private static Method toMinecraftM = Reflection.getMethod(cmrCl, "toMinecraft");

    /**
     * Initializes the TradeAPI, do NOT use this method
     */
    public TradeAPI() {
        SU.pm.registerEvents(this, Main.pl);
    }

    /**
     * Creates a not acceptable (crossed arrow) MerchantRecipe from the given items
     *
     * @param items - no items = empty recipe, 1. item = output slot, 2. item = first slot, 3. item = second slot, there
     *              are no required amount of given items, this method works with 0,1,2 and 3 arguments too.
     * @return The generated recipe
     */
    public static MerchantRecipe makeDeniedRecipe(ItemStack... items) {
        MerchantRecipe mr = new MerchantRecipe((items.length == 0 || items[0] == null) ? air : items[0], 1, 0, false);
        if (items.length > 1)
            mr.addIngredient(items[1] == null ? air : items[1]);
        else
            mr.addIngredient(air);
        if (items.length > 2)
            mr.addIngredient(items[2] == null ? air : items[2]);
        return mr;
    }

    /**
     * Creates an acceptable MerchantRecipe from the given items
     *
     * @param items - no items = empty recipe, 1. item = output slot, 2. item = first slot, 3. item = second slot, there
     *              are no required amount of given items, this method works with 0,1,2 and 3 arguments too.
     * @return The generated recipe
     */
    public static MerchantRecipe makeRecipe(ItemStack... items) {
        MerchantRecipe mr = new MerchantRecipe((items.length == 0 || items[0] == null) ? air : items[0], 0, 10000, false);
        if (items.length > 1)
            mr.addIngredient(items[1] == null ? air : items[1]);
        else
            mr.addIngredient(air);
        if (items.length > 2)
            mr.addIngredient(items[2] == null ? air : items[2]);
        return mr;
    }

    /**
     * Opens a TradeGUI for the given player
     *
     * @param plr     - The TradeGUI receiver player
     * @param title   - The title of the TradeGUI
     * @param recipes - The recipes shown in the GUI
     * @return The opened TradeGUI
     */
    public static MerchantInventory openGUI(final Player plr, String title, MerchantRecipe... recipes) {
        return openGUI(plr, title, Lists.newArrayList(recipes));
    }

    /**
     * Opens a TradeGUI for the given player
     *
     * @param plr     - The TradeGUI receiver player
     * @param title   - The title of the TradeGUI
     * @param recipes - The recipes shown in the GUI
     * @return The opened TradeGUI
     */
    public static MerchantInventory openGUI(final Player plr, String title, Iterable<MerchantRecipe> recipes) {
        try {
            Object nmsPlr = EntityUtils.getNMSEntity(plr);
            IMerchantHook imh = new IMerchantHook(nmsPlr, title);
            Object im = Proxy.newProxyInstance(nmsPlr.getClass().getClassLoader(), new Class[]{imc}, imh);
            ArrayList<Object> mrl = imh.offers;
            for (MerchantRecipe r : recipes) {
                Object cmr = cmrC.newInstance(r.getResult(), r.getUses(), r.getMaxUses(), false);
                ingredientsF.set(cmr, ingredientsF.get(r));
                mrl.add(toMinecraftM.invoke(cmr));
            }
            openTradeM.invoke(nmsPlr, im);
            return (MerchantInventory) plr.getOpenInventory().getTopInventory();
        } catch (Throwable e) {
            SU.error(SU.cs, e, "MythaliumCore", "mythalium");
        }
        return null;
    }
}
