package gyurix.spigotlib;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.configfile.PostLoadable;
import gyurix.spigotutils.BlockData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Class representing the custom item names stored in the config, used in the ItemAPI
 */
public class Items implements PostLoadable {
  /**
   * The map containing all the enchant alias (in keys) and their corresponding enchants (in values)
   */
  @ConfigOptions(serialize = false)
  private static HashMap<String, Enchantment> enchantAliases = new HashMap<>();
  /**
   * The in config editable enchant name (key), enchant aliases (value) map.
   */
  private static HashMap<String, ArrayList<String>> enchants = new HashMap<>();
  /**
   * The map containing all the material ids and their corresponding materials (values)
   */
  private static HashMap<Integer, Material> materialIds = new HashMap<>();
  /**
   * The map containing all the item names (in keys) and their corresponding item types (values)
   */
  @ConfigOptions(serialize = false)
  private static HashMap<String, ItemStack> nameAliases = new HashMap<>();

  /**
   * The in config editable item type (key), item name aliases (value) map.
   */
  private static HashMap<BlockData, ArrayList<String>> names = new HashMap<>();

  /**
   * Add an item name alias to item to string and item from string converters
   *
   * @param name - The items name
   * @param item - The item
   */
  public static void addItemNameAlias(String name, ItemStack item) {
    nameAliases.put(name, item);
  }

  public static Enchantment getEnchant(String name) {
    name = name.toLowerCase();
    Enchantment enchantment = enchantAliases.get(name);
    if (enchantment == null) {
      name = name.toUpperCase();
      return Enchantment.getByName(name);
    }
    return enchantment;
  }

  public static String getEnchantName(Enchantment ench) {
    String out = ench.getName();
    List<String> encs = enchants.get(out);
    if (encs == null)
      return out;
    return encs.get(0);
  }

  public static ItemStack getItem(String name) {
    name = name.toLowerCase();
    ItemStack is = nameAliases.get(name);
    if (is != null)
      return is.clone();
    try {
      return new ItemStack(Material.valueOf(name.toUpperCase()));
    } catch (Throwable ignored) {
      try {
        return new ItemStack(getMaterial(Integer.valueOf(name)));
      } catch (Throwable ig) {
        try {
          return new ItemStack(Material.valueOf("LEGACY_"+name.toUpperCase()));
        } catch(Throwable t) {
          SU.cs.sendMessage("§cInvalid item name or id: §e" + name);
        }
      }
      System.out.println("Name = \"" + name + "\"");
    }
    return null;
  }

  public static Material getMaterial(int id) {
    Material mat = materialIds.get(id);
    if (mat == null)
      throw new RuntimeException("Item for id " + id + " was not found.");
    return mat;
  }

  public static String getName(BlockData block) {
    ArrayList<String> list = names.get(block);
    if (list == null)
      return block.getType().name();
    return list.get(0);
  }

  public static List<String> getNames(BlockData block) {
    return names.get(block);
  }

  /**
   * Remove an item name alias from the item to string and item from string converters
   *
   * @param name - The items name
   * @return True if the item name alias was removed successfully, false otherwise
   */
  public static boolean removeItemNameAlias(String name) {
    return nameAliases.remove(name) != null;
  }

  /**
   * Makes the enchantAliases and nameAliases caches.
   */
  @Override
  public void postLoad() {
    for (Material m : Material.values()) {
      try {
        materialIds.put(m.getId(), m);
      } catch (Throwable ignored) {
      }
    }
    for (Enchantment e : Enchantment.values()) {
      if (!enchants.containsKey(e.getName()))
        enchants.put(e.getName(), newArrayList(e.getName().toLowerCase().replace("_", "")));
    }
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
        try {
          nameAliases.put(s, bd.toItem());
        } catch (Throwable err) {
          SU.error(SU.cs, err, "SpigotLib", "gyurix");
        }
      }
    }
  }
}
