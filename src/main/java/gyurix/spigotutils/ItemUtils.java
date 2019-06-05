package gyurix.spigotutils;

import com.google.common.collect.Lists;
import gyurix.api.VariableAPI;
import gyurix.chat.ChatTag;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTList;
import gyurix.protocol.utils.ItemStackWrapper;
import gyurix.spigotlib.Items;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static gyurix.protocol.Reflection.ver;
import static gyurix.spigotlib.Items.*;
import static gyurix.spigotlib.SU.*;
import static gyurix.spigotutils.ServerVersion.*;

/**
 * Utils for managing items
 */
public class ItemUtils {
  /**
   * Adds the given item to the given inventory with the default max stack size
   *
   * @param inv - The inventory to which the item should be added
   * @param is  - The addable item
   * @return The remaining items after the addition
   */
  public static int addItem(Inventory inv, ItemStack is) {
    return addItem(inv, is, is.getMaxStackSize());
  }

  /**
   * Adds the given item to the given inventory
   *
   * @param inv      - The inventory to which the item should be added
   * @param is       - The addable item
   * @param maxStack - Maximal stack size of the item
   * @return The remaining items after the addition
   */
  public static int addItem(Inventory inv, ItemStack is, int maxStack) {
    int left = is.getAmount();
    int size = inv instanceof PlayerInventory ? 36 : inv.getSize();
    for (int i = 0; i < size; i++) {
      ItemStack current = inv.getItem(i);
      if (itemSimilar(current, is)) {
        int am = current.getAmount();
        int canPlace = maxStack - am;
        if (canPlace >= left) {
          current.setAmount(am + left);
          return 0;
        } else if (canPlace > 0) {
          current.setAmount(am + canPlace);
          left -= canPlace;
        }
      }
    }
    for (int i = 0; i < size; i++) {
      ItemStack current = inv.getItem(i);
      if (current == null || current.getType() == Material.AIR) {
        current = is.clone();
        if (maxStack >= left) {
          current.setAmount(left);
          inv.setItem(i, current);
          return 0;
        } else {
          current.setAmount(maxStack);
          left -= maxStack;
          inv.setItem(i, current);
        }
      }
    }
    return left;
  }

  public static ItemStack addLore(ItemStack is, String... lore) {
    if (is == null || is.getType() == Material.AIR)
      return is;
    is = is.clone();
    ItemMeta meta = is.getItemMeta();
    List<String> fullLore = meta.getLore();
    if (fullLore == null)
      fullLore = new ArrayList<>();
    fullLore.addAll(Arrays.asList(lore));
    meta.setLore(fullLore);
    is.setItemMeta(meta);
    return is;
  }

  public static ItemStack addLore(ItemStack is, List<String> lore, Object... vars) {
    if (is == null || is.getType() == Material.AIR)
      return is;
    is = is.clone();
    ItemMeta meta = is.getItemMeta();
    List<String> fullLore = meta.getLore();
    if (fullLore == null)
      fullLore = new ArrayList<>();
    fullLore.addAll(SU.fillVariables(lore, vars));
    meta.setLore(fullLore);
    is.setItemMeta(meta);
    return is;
  }

  /**
   * Adds the given lore storage meta to the given item
   *
   * @param is     - Modifiable item
   * @param values - Array of storage format and values
   * @return The modified item
   */
  public static ItemStack addLoreStorageMeta(ItemStack is, Object... values) {
    if (is == null || is.getType() == Material.AIR)
      return is;
    is = is.clone();
    ItemMeta meta = is.getItemMeta();
    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
    boolean key = true;
    int id = -1;
    String prefix = "";
    String suffix = "";
    for (Object o : values) {
      if (key) {
        id = -1;
        prefix = (String) o;
        int id2 = prefix.indexOf("<value>");
        suffix = prefix.substring(id2 + 7);
        prefix = prefix.substring(0, id2);
        for (int i = 0; i < lore.size(); ++i) {
          String s = lore.get(i);
          if (s.startsWith(prefix) && s.endsWith(suffix)) {
            id = i;
            break;
          }
        }
        key = false;
        continue;
      }
      if (id == -1)
        lore.add(prefix + o + suffix);
      else
        lore.set(id, prefix + o + suffix);
      key = true;
    }
    meta.setLore(lore);
    is.setItemMeta(meta);
    return is;
  }

  /**
   * A truth check if an iterable contains the given typed item or not
   *
   * @param source ItemStack iterable
   * @param is     checked ItemStack
   * @return True if the ItemStack iterable contains the checked ItemStack in any amount, false otherwise.
   */
  public static boolean containsItem(Iterable<ItemStack> source, ItemStack is) {
    for (ItemStack i : source) {
      if (itemSimilar(i, is))
        return true;
    }
    return false;
  }

  /**
   * Counts the given item in the given inventory
   *
   * @param inv - The inventory in which the item should be counted
   * @param is  - The countable item, the amount of the item is ignored (calculated as 1)
   * @return The amount of the item which is in the inventory
   */
  public static int countItem(Inventory inv, ItemStack is) {
    int count = 0;
    int size = inv instanceof PlayerInventory ? 36 : inv.getSize();
    for (int i = 0; i < size; i++) {
      ItemStack current = inv.getItem(i);
      if (ItemUtils.itemSimilar(is, current))
        count += current.getAmount();
    }
    return count;
  }

  /**
   * Counts the available space for the given item in the given inventory
   *
   * @param inv      - The inventory to which the item should be added
   * @param is       - The addable item
   * @param maxStack - Maximal stack size of the item
   * @return The maximum amount
   */
  public static int countItemSpace(Inventory inv, ItemStack is, int maxStack) {
    int space = 0;
    int size = inv instanceof PlayerInventory ? 36 : inv.getSize();
    for (int i = 0; i < size; i++) {
      ItemStack current = inv.getItem(i);
      if (current == null || current.getType() == Material.AIR)
        space += maxStack;
      else if (itemSimilar(current, is))
        space += maxStack - current.getAmount();
    }
    return space;
  }

  /**
   * Fill variables in a ItemStack
   * Available special variables:
   * #amount: the amount value of the item
   * #id: the id of the item
   * #sub: the subid of the item
   *
   * @param is   - The ItemStack in which the variables should be filled
   * @param vars - The fillable variables
   * @return A clone of the original ItemStack with filled variables
   */
  public static ItemStack fillVariables(ItemStack is, Object... vars) {
    if (is == null || is.getType() == Material.AIR)
      return is;
    is = is.clone();
    String last = null;
    for (Object v : vars) {
      if (last == null)
        last = (String) v;
      else {
        switch (last) {
          case "#amount":
            is.setAmount(Integer.valueOf(String.valueOf(v)));
            break;
          case "#id":
            String vs = String.valueOf(v);
            try {
              is.setType(getMaterial(Integer.valueOf(vs)));
            } catch (Throwable e) {
              try {
                is.setType(Material.valueOf(vs.toUpperCase()));
              } catch (Throwable e2) {
                error(cs, e2, "SpigotLib", "gyurix");
              }
            }
            break;
          case "#sub":
            is.setDurability(Short.valueOf(String.valueOf(v)));
            break;
          case "#owner":
            try {
              SkullMeta sm = (SkullMeta) is.getItemMeta();
              sm.setOwner(String.valueOf(v));
              is.setItemMeta(sm);
            } catch (Throwable ignored) {
            }
            break;
        }
        last = null;
      }
    }
    if (is.hasItemMeta() && is.getItemMeta() != null) {
      ItemMeta meta = is.getItemMeta();
      if (meta.hasDisplayName() && meta.getDisplayName() != null)
        meta.setDisplayName(SU.fillVariables(meta.getDisplayName(), vars));
      if (meta.hasLore() && meta.getLore() != null) {
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++)
          lore.set(i, SU.fillVariables(lore.get(i), vars));
        ArrayList<String> newLore = new ArrayList<>();
        for (String l : lore) {
          Collections.addAll(newLore, l.split("\n"));
        }
        meta.setLore(newLore);
      }
      is.setItemMeta(meta);
    }
    return is;
  }

  public static ItemStack fillVariables(Player plr, ItemStack is) {
    if (is == null || is.getType() == Material.AIR)
      return is;
    is = is.clone();
    if (is.hasItemMeta() && is.getItemMeta() != null) {
      ItemMeta meta = is.getItemMeta();
      if (meta.hasDisplayName() && meta.getDisplayName() != null)
        meta.setDisplayName(VariableAPI.fillVariables(meta.getDisplayName(), plr));
      if (meta.hasLore() && meta.getLore() != null) {
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++)
          lore.set(i, VariableAPI.fillVariables(lore.get(i), plr));
        ArrayList<String> newLore = new ArrayList<>();
        for (String l : lore)
          Collections.addAll(newLore, l.split("\n"));
        meta.setLore(newLore);
      }
      is.setItemMeta(meta);
    }
    return is;
  }

  /**
   * Get the numeric id of the given itemname, it works for both numeric and text ids.
   *
   * @param name the case insensitive material name of the item or the numeric id of the item.
   * @return the numeric id of the requested item or 1, if the given name is incorrect or null
   */
  public static ItemStack getItem(String name) {
    return Items.getItem(name);
  }

  /**
   * Gets the given lore storage meta from the given ItemStack
   *
   * @param is     - The checkable ItemStack
   * @param format - The format of the storage meta
   * @param def    - Default value to return if the meta was not found.
   * @return The first found compatible lore storage meta or def if not found any.
   */
  public static String getLoreStorageMeta(ItemStack is, String format, String def) {
    if (is == null || is.getType() == Material.AIR || !is.hasItemMeta())
      return def;
    is = is.clone();
    ItemMeta meta = is.getItemMeta();
    if (!meta.hasLore())
      return def;
    int id2 = format.indexOf("<value>");
    String prefix = format.substring(0, id2);
    String suffix = format.substring(id2 + 7);
    for (String s : meta.getLore()) {
      if (s.startsWith(prefix) && s.endsWith(suffix))
        return s.substring(prefix.length(), s.length() - suffix.length());
    }
    return def;
  }

  /**
   * Gets the name of the given item
   *
   * @param is - The item which name we would like to get
   * @return The name of the item
   */
  public static String getName(ItemStack is) {
    if (is == null || is.getType() == Material.AIR)
      return "Air";
    return is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : SU.toCamelCase(is.getType().name());
  }

  /**
   * Makes item glowing by adding luck 1 enchant to it and hide enchants attribute
   *
   * @param item - The glowable item
   * @return The glowing item
   */
  public static ItemStack glow(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return item;
    item = item.clone();
    ItemMeta meta = item.getItemMeta();
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
    item.addUnsafeEnchantment(Enchantment.LUCK, 1);
    return item;
  }

  /**
   * A truth check for two items, if they are actually totally same or not
   *
   * @param item1 first item of the equal checking
   * @param item2 second item of the equal checking
   * @return True if the two itemstack contains exactly the same abilities (id, count, durability, metadata), false
   * otherwise
   */
  public static boolean itemEqual(ItemStack item1, ItemStack item2) {
    return itemToString(item1).equals(itemToString(item2));
  }

  /**
   * Converts an NBT JSON String to an ItemStack
   *
   * @param str - Convertable NBT JSON String
   * @return Conversion result
   */
  public static ItemStack itemFromNBTJson(String str) {
    return null;
  }

  /**
   * A truth check for two items, if they type is actually totally same or not.
   * The only allowed difference between the stacks could be only their count.
   *
   * @param item1 first item of the similiar checking
   * @param item2 second item of the similiar checking
   * @return True if the two itemstack contains exactly the same abilities (id, durability, metadata), the item counts
   * could be different; false otherwise.
   */
  public static boolean itemSimilar(ItemStack item1, ItemStack item2) {
    if (item1 == item2)
      return true;
    if (item1 == null || item2 == null)
      return false;
    item1 = item1.clone();
    item1.setAmount(1);
    item2 = item2.clone();
    item2.setAmount(1);
    return itemToString(item1).equals(itemToString(item2));
  }

  /**
   * Converts an item to NBT Json format
   *
   * @param is - Convertable item
   * @return Conversion result
   */
  public static String itemToNBTJson(ItemStack is) {
    return new ItemStackWrapper(is).getNbtData().toNMS().toString();
  }

  /**
   * Converts an ItemStack to it's representing string
   *
   * @param in convertable ItemStack
   * @return the conversion output String or "0:-1 0" if the given ItemStack is null
   */
  public static String itemToString(ItemStack in) {
    return itemToString(in, false);
  }

  /**
   * Converts an ItemStack to it's representing string with the option to store all NBT tags of the item
   *
   * @param in     - The convertable ItemStack
   * @param useNBT - Store every nbt tag of the item
   * @return The conversion output String or "0:-1 0" if the given ItemStack is null
   */
  public static String itemToString(ItemStack in, boolean useNBT) {
    if (in == null)
      return "0:-1 0";
    StringBuilder out = new StringBuilder();
    out.append(Items.getName(new BlockData(in.getType(), in.getDurability())));
    if (in.getDurability() != 0)
      out.append(':').append(in.getDurability());
    if (in.getAmount() != 1)
      out.append(' ').append(in.getAmount());
    if (useNBT) {
      out.append(" nbt:").append(escapeText(new ItemStackWrapper(in).getMetaData().toNMS().toString()));
      return out.toString();
    }
    ItemMeta meta = in.getItemMeta();
    if (meta == null)
      return out.toString();
    if (ver.isAbove(v1_8))
      for (ItemFlag f : meta.getItemFlags())
        out.append(" hide:").append(f.name().substring(5));
    if (meta.hasDisplayName())
      out.append(" name:").append(escapeText(meta.getDisplayName()));
    if (meta.hasLore())
      out.append(" lore:").append(escapeText(StringUtils.join(meta.getLore(), '\n')));
    for (Entry<Enchantment, Integer> ench : meta.getEnchants().entrySet()) {
      out.append(' ').append(getEnchantName(ench.getKey())).append(':').append(ench.getValue());
    }
    if (meta instanceof BookMeta) {
      BookMeta bmeta = (BookMeta) meta;
      if (bmeta.hasAuthor())
        out.append(" author:").append(bmeta.getAuthor());
      if (bmeta.hasTitle())
        out.append(" title:").append(bmeta.getTitle());
      if (ver.isAbove(v1_13)) {
        for (BaseComponent[] components : bmeta.spigot().getPages())
          out.append(" page:").append(escapeText(ChatTag.fromBaseComponents(components).toExtraString()));
        out.append(" generation:").append(bmeta.getGeneration());
      } else {
        for (String page : bmeta.getPages())
          out.append(" page:").append(escapeText(page));
      }
    }
    if (ver.isAbove(v1_8) && (meta instanceof BannerMeta)) {
      BannerMeta bmeta = (BannerMeta) meta;
      out.append(" color:").append(bmeta.getBaseColor() == null ? "BLACK" : bmeta.getBaseColor().name());
      for (Pattern p : bmeta.getPatterns())
        out.append(' ').append(p.getPattern().getIdentifier()).append(':').append(p.getColor().name());
    } else if (meta instanceof LeatherArmorMeta) {
      LeatherArmorMeta bmeta = (LeatherArmorMeta) meta;
      Color c = bmeta.getColor();
      if (!c.equals(Bukkit.getItemFactory().getDefaultLeatherColor()))
        out.append(" color:").append(Integer.toHexString(c.asRGB()));
    } else if (meta instanceof FireworkMeta) {
      FireworkMeta bmeta = (FireworkMeta) meta;
      out.append(" power:").append(bmeta.getPower());
      for (FireworkEffect e : bmeta.getEffects()) {
        out.append(' ').append(e.getType().name()).append(':');
        boolean pref = false;
        if (!e.getColors().isEmpty()) {
          pref = true;
          out.append("colors:");
          for (Color c : e.getColors()) {
            out.append(c.getRed()).append(',').append(c.getGreen()).append(',').append(c.getBlue()).append(';');
          }
          out.setLength(out.length() - 1);
        }
        if (!e.getFadeColors().isEmpty()) {
          if (pref)
            out.append('|');
          else
            pref = true;
          out.append("fades:");
          for (Color c : e.getFadeColors()) {
            out.append(c.getRed()).append(',').append(c.getGreen()).append(',').append(c.getBlue()).append(';');
          }
          out.setLength(out.length() - 1);
        }
        if (e.hasFlicker()) {
          if (pref)
            out.append('|');
          else
            pref = true;
          out.append("flicker");
        }
        if (e.hasTrail()) {
          if (pref)
            out.append('|');
          out.append("trail");
        }
      }
    } else if (meta instanceof PotionMeta) {
      PotionMeta bmeta = (PotionMeta) meta;
      if (ver.isAbove(v1_9)) {
        PotionData pd = bmeta.getBasePotionData();
        if (pd != null) {
          out.append(" potion:").append(pd.getType().name());
          if (pd.isExtended())
            out.append(":E");
          if (pd.isUpgraded())
            out.append(":U");
        }
      }
      for (PotionEffect e : bmeta.getCustomEffects()) {
        out.append(' ').append(e.getType().getName()).append(':').append(e.getDuration()).append(':').append(e.getAmplifier());
        if (ver.isAbove(v1_8))
          if (!e.hasParticles())
            out.append(":np");
        if (!e.isAmbient())
          out.append(":na");
      }
    } else if (meta instanceof SkullMeta) {
      SkullMeta bmeta = (SkullMeta) meta;
      if (bmeta.hasOwner())
        out.append(" owner:").append(bmeta.getOwner());
    } else if (meta instanceof EnchantmentStorageMeta) {
      for (Entry<Enchantment, Integer> e : ((EnchantmentStorageMeta) meta).getStoredEnchants().entrySet()) {
        out.append(" +").append(getEnchantName(e.getKey())).append(':').append(e.getValue());
      }
    }

    return out.toString();
  }

  /**
   * Converts a map representation of a saved inventory to an actual Inventory.
   * Completely overrides all the existing items in the inventory.
   *
   * @param map - The map containing the items and their slots of the saved inventory
   */
  public static void loadInv(Map<Integer, ItemStack> map, Inventory inv) {
    int size = inv instanceof PlayerInventory ? ver.isAbove(v1_9) ? 41 : 40 : inv.getSize();
    for (int i = 0; i < size; ++i)
      inv.setItem(i, map.get(i));
  }

  public static ItemStack makeItem(Material type, int amount, short sub, String name, String... lore) {
    ItemStack is = new ItemStack(type, amount, sub);
    ItemMeta im = is.getItemMeta();
    im.setDisplayName(name);
    im.setLore(Arrays.asList(lore));
    is.setItemMeta(im);
    return is;
  }

  public static ItemStack makeItem(Material type, String name, String... lore) {
    ItemStack is = new ItemStack(type, 1, (short) 0);
    ItemMeta im = is.getItemMeta();
    im.setDisplayName(name);
    im.setLore(Arrays.asList(lore));
    is.setItemMeta(im);
    return is;
  }

  public static ItemStack makeItem(Material type, int amount, short sub, String name, ArrayList<String> lore, Object... vars) {
    ItemStack is = new ItemStack(type, amount, sub);
    ItemMeta im = is.getItemMeta();
    im.setDisplayName(name);
    im.setLore(lore);
    is.setItemMeta(im);
    return fillVariables(is, vars);
  }

  /**
   * Removes the given item from the given inventory
   *
   * @param inv - The inventory from which the item should be removed
   * @param is  - The removable item
   * @return The remaining amount of the removable item
   */
  public static int removeItem(Inventory inv, ItemStack is) {
    int left = is.getAmount();
    boolean pi = inv instanceof PlayerInventory;
    int size = pi ? 36 : inv.getSize();
    Iterator<Integer> it = new Iterator<Integer>() {
      boolean first = true;
      int id = 0;

      @Override
      public boolean hasNext() {
        return id < size;
      }

      @Override
      public Integer next() {
        if (first) {
          first = false;
          if (pi)
            return ((PlayerInventory) inv).getHeldItemSlot();
        }
        return id++;
      }
    };
    while (it.hasNext()) {
      Integer i = it.next();
      ItemStack current = inv.getItem(i);
      if (itemSimilar(current, is)) {
        int am = current.getAmount();
        if (left == am) {
          inv.setItem(i, null);
          return 0;
        } else if (left < am) {
          current.setAmount(am - left);
          inv.setItem(i, current);
          return 0;
        } else {
          inv.setItem(i, null);
          left -= am;
        }
      }
    }
    return left;
  }

  /**
   * Saves an inventory to a TreeMap containing the slots and the items of the given inventory
   *
   * @param inv - The saveable inventory
   * @return The saving result
   */
  public static TreeMap<Integer, ItemStack> saveInv(Inventory inv) {
    int size = inv instanceof PlayerInventory ? ver.isAbove(v1_9) ? 41 : 40 : inv.getSize();
    TreeMap<Integer, ItemStack> out = new TreeMap<>();
    for (int i = 0; i < size; ++i) {
      ItemStack is = inv.getItem(i);
      if (is != null && is.getType() != Material.AIR)
        out.put(i, is);
    }
    return out;
  }

  /**
   * Converts an ItemStack representing string back to the ItemStack
   *
   * @param in string represantation of an ItemStack
   * @return The conversion output ItemStack, or null if the given string is null
   */
  public static ItemStack stringToItemStack(String in) {
    if (in == null)
      return null;
    String[] parts = in.split(" ");
    String[] idParts = parts[0].split(":");
    ItemStack out = getItem(idParts[0]);
    if (out == null)
      return null;
    int st = 1;
    try {
      out.setDurability(Short.valueOf(idParts[1]));
    } catch (Throwable ignored) {
    }

    try {
      out.setAmount(Short.valueOf(parts[1]));
      st = 2;
    } catch (Throwable ignored) {
    }

    int l = parts.length;
    ItemMeta meta = out.getItemMeta();
    ArrayList<String[]> remaining = new ArrayList<>();
    ArrayList<String> attributes = new ArrayList<>();
    for (int i = st; i < l; i++) {
      String[] s = parts[i].split(":", 2);
      s[0] = s[0].toUpperCase();
      try {
        Enchantment enc = getEnchant(s[0]);
        if (enc == null)
          enc = Enchantment.getByName(s[0].toUpperCase());
        if (enc == null) {
          if (ver.isAbove(v1_8)) {
            if (s[0].equals("HIDE"))
              meta.addItemFlags(ItemFlag.valueOf("HIDE_" + s[1].toUpperCase()));
          }
          switch (s[0]) {
            case "NAME":
              meta.setDisplayName(unescapeText(s[1]));
              break;
            case "LORE":
              meta.setLore(Lists.newArrayList(unescapeText(s[1]).split("\n")));
              break;
            case "NBT":
              return Bukkit.getUnsafe().modifyItemStack(out, unescapeText(s[1]));
            case "ATTR":
              attributes.add(s[1]);
              break;
            default:
              remaining.add(s);
              break;
          }
        } else {
          meta.addEnchant(enc, Integer.valueOf(s[1]), true);
        }
      } catch (Throwable e) {
        log(Main.pl, "§cError on deserializing §eItemMeta§c data of item \"§f" + in + "§c\"");
        error(cs, e, "SpigotLib", "gyurix");
      }
    }
    if (meta instanceof BookMeta) {
      BookMeta bmeta = (BookMeta) meta;
      for (String[] s : remaining) {
        try {
          String text = unescapeText(s[1]);
          switch (s[0]) {
            case "AUTHOR":
              bmeta.setAuthor(text);
              break;
            case "TITLE":
              bmeta.setTitle(text);
              break;
            case "PAGE":
              if (ver.isAbove(v1_13))
                bmeta.spigot().addPage(ChatTag.fromExtraText(text).toBaseComponents());
              else
                bmeta.addPage(text);
              break;
            case "GENERATION":
              bmeta.setGeneration(BookMeta.Generation.valueOf(text.toUpperCase()));
              break;
          }
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §eBookMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    }
    if (ver.isAbove(v1_8))
      if (meta instanceof BannerMeta) {
        BannerMeta bmeta = (BannerMeta) meta;
        for (String[] s : remaining) {
          try {
            PatternType type = PatternType.getByIdentifier(s[0].toLowerCase());
            if (type == null) {
              if (s[0].equals("COLOR")) {
                bmeta.setBaseColor(DyeColor.valueOf(s[1].toUpperCase()));
              } else {
                PatternType pt = PatternType.getByIdentifier(s[0].toLowerCase());
                if (pt != null) {
                  bmeta.addPattern(new Pattern(DyeColor.valueOf(s[1].toUpperCase()), pt));
                }
              }
            } else {
              bmeta.addPattern(new Pattern(DyeColor.valueOf(s[1].toUpperCase()), type));
            }
          } catch (Throwable e) {
            log(Main.pl, "§cError on deserializing §eBannerMeta§c data of item \"§f" + in + "§c\"");
            error(cs, e, "SpigotLib", "gyurix");
          }
        }
      }
    if (meta instanceof LeatherArmorMeta) {
      LeatherArmorMeta bmeta = (LeatherArmorMeta) meta;
      for (String[] s : remaining) {
        try {
          if (s[0].equals("COLOR")) {
            String[] color = s[1].split(",", 3);
            if (color.length == 3)
              bmeta.setColor(Color.fromRGB(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
            else
              bmeta.setColor(Color.fromRGB(Integer.parseInt(color[0], 16)));
          }
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §eLeatherArmorMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    } else if (meta instanceof FireworkMeta) {
      FireworkMeta bmeta = (FireworkMeta) meta;
      for (String[] s : remaining) {
        try {
          if (s[0].equals("FPOWER")) {
            bmeta.setPower(Integer.valueOf(s[1]));
          } else {
            Type type = Type.valueOf(s[0]);
            Builder build = FireworkEffect.builder().with(type);
            for (String d : s[1].toUpperCase().split("\\|")) {
              String[] d2 = d.split(":", 2);
              switch (d2[0]) {
                case "COLORS":
                  for (String colors : d2[1].split(";")) {
                    String[] color = colors.split(",", 3);
                    build.withColor(Color.fromRGB(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2])));
                  }
                  break;
                case "FADES":
                  for (String fades : d2[1].split(";")) {
                    String[] fade = fades.split(",", 3);
                    build.withFade(Color.fromRGB(Integer.valueOf(fade[0]), Integer.valueOf(fade[1]), Integer.valueOf(fade[2])));
                  }
                  break;
                case "FLICKER":
                  build.withFlicker();
                  break;
                case "TRAIL":
                  build.withTrail();
                  break;
              }
            }
            bmeta.addEffect(build.build());

          }
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §eFireworkMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    } else if (meta instanceof PotionMeta) {
      PotionMeta bmeta = (PotionMeta) meta;
      for (String[] s : remaining) {
        try {
          if (ver.isAbove(v1_9) && s[0].equalsIgnoreCase("potion")) {
            String[] s2 = s[1].split(":");
            boolean ext = false;
            boolean upg = false;
            if (s2.length > 1) {
              if (s2[1].equals("E"))
                ext = true;
              else if (s2[1].equals("U"))
                upg = true;
            }
            if (s2.length > 2) {
              if (s2[2].equals("E"))
                ext = true;
              else if (s2[2].equals("U"))
                upg = true;
            }
            PotionData pd = new PotionData(PotionType.valueOf(s2[0].toUpperCase()), ext, upg);
            bmeta.setBasePotionData(pd);
            continue;
          }
          PotionEffectType type = PotionEffectType.getByName(s[0]);
          if (type != null) {
            String[] s2 = s[1].split(":");
            if (ver.isAbove(v1_8)) {
              bmeta.addCustomEffect(new PotionEffect(type, Integer.valueOf(s2[0]), Integer.valueOf(s2[1]),
                      !ArrayUtils.contains(s2, "na"), !ArrayUtils.contains(s2, "np")), false);
            } else {
              bmeta.addCustomEffect(new PotionEffect(type, Integer.valueOf(s2[0]), Integer.valueOf(s2[1]),
                      !ArrayUtils.contains(s2, "na")), false);
            }
          }
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §ePotionMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    } else if (meta instanceof SkullMeta) {
      SkullMeta bmeta = (SkullMeta) meta;
      for (String[] s : remaining) {
        try {
          if (s[0].equals("OWNER")) {
            bmeta.setOwner(s[1]);
          }
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §eSkullMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    } else if (meta instanceof EnchantmentStorageMeta) {
      EnchantmentStorageMeta bmeta = (EnchantmentStorageMeta) meta;
      for (String[] s : remaining) {
        try {
          String en = s[0].substring(1);
          Enchantment enc = getEnchant(en);
          if (enc != null)
            bmeta.addStoredEnchant(enc, Integer.valueOf(s[1]), true);
        } catch (Throwable e) {
          log(Main.pl, "§cError on deserializing §eEnchantmentStorageMeta§c data of item \"§f" + in + "§c\"");
          error(cs, e, "SpigotLib", "gyurix");
        }
      }
    }
    out.setItemMeta(meta);
    if (!attributes.isEmpty()) {
      ItemStackWrapper wr = new ItemStackWrapper(out);
      NBTList attrs = new NBTList();
      AtomicInteger id = new AtomicInteger();
      attributes.forEach((k) -> {
        String[] d = k.split(":", 2);
        char op = d[1].charAt(0);
        int operation = op == '*' ? 1 : op == 'o' ? 2 : 0;
        double am = Double.valueOf(d[1].substring(1));
        if (op == '-')
          am = -am;
        NBTCompound attr = new NBTCompound();
        attr.set("AttributeName", d[0]);
        attr.set("Name", "a" + (id.incrementAndGet()));
        attr.set("Operation", operation);
        attr.set("Amount", am);
        attr.set("UUIDLeast", rand.nextLong());
        attr.set("UUIDMost", rand.nextLong());
        attrs.add(attr);
      });
      wr.getMetaData().set("AttributeModifiers", attrs);
      out = wr.toBukkitStack();
    }
    return out;
  }
}
