package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.PostLoadable;
import gyurix.spigotutils.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Class representing the custom item names stored in the config, used in the ItemAPI
 */
public class Items implements PostLoadable {
    /**
     * The map containing all the enchant alias (in keys) and their corresponding enchants (in values)
     */
    @ConfigOptions(serialize = false)
    public static HashMap<String, Enchantment> enchantAliases = new HashMap<>();
    /**
     * The in config editable enchant name (key), enchant aliases (value) map.
     */
    public static HashMap<String, ArrayList<String>> enchants = new HashMap<>();
    /**
     * The map containing all the item names (in keys) and their corresponding item types (values)
     */
    @ConfigOptions(serialize = false)
    public static HashMap<String, ItemStack> nameAliases = new HashMap<>();
    /**
     * The in config editable item type (key), item name aliases (value) map.
     */
    public static HashMap<BlockData, ArrayList<String>> names = new HashMap<>();

    /**
     * Makes the enchantAliases and nameAliases caches.
     */
    @Override
    public void postLoad() {
        enchantAliases.clear();
        for (Entry<String, ArrayList<String>> e : enchants.entrySet()) {
            Enchantment ec = Enchantment.getByName(e.getKey());
            for (String s : e.getValue()) {
                enchantAliases.put(s, ec);
            }
        }
        nameAliases.clear();
        for (Entry<BlockData, ArrayList<String>> e : names.entrySet()) {
            BlockData bd = e.getKey();
            for (String s : e.getValue()) {
                nameAliases.put(s, bd.toItem());
            }
        }
    }
}
