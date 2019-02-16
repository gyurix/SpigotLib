package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.Items;
import gyurix.spigotlib.SU;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class used for storing the data of a block, or an item
 */
public class BlockData implements StringSerializable, Comparable<BlockData> {
  /**
   * True if the BlockData can be applied to any subtype of the block
   */
  public boolean anydata = true;
  /**
   * The requested subtype of the block
   */
  public short data;
  /**
   * The requested type of the block
   */
  public int id;


  /**
   * Constructs a new BlockData representing the type of the given block
   *
   * @param b - Target Block
   */
  public BlockData(Block b) {
    id = b.getType().getId();
    data = b.getData();
    anydata = false;
  }

  /**
   * Constructs a new BlockData representing the type of the given block state
   *
   * @param b - Target Block
   */
  public BlockData(BlockState b) {
    id = b.getType().getId();
    data = b.getRawData();
    anydata = false;
  }

  /**
   * Constructs a new BlockData with the allowing any subtypes of the given item/block id
   *
   * @param id - The wanted block / item id
   */
  public BlockData(int id) {
    this.id = id;
  }

  /**
   * Constructs a new BlockData of the given item/block id and subtype
   *
   * @param id   - The items / blocks id
   * @param data - The items / blocks subtype
   */
  public BlockData(int id, short data) {
    this.id = id;
    this.data = data;
    anydata = false;
  }

  /**
   * Makes a new Block data from a String, which should have format [itemId|itemName][:subType]
   *
   * @param in - The convertable String
   */
  public BlockData(String in) {
    String[] s = in.split(":", 2);
    try {
      try {
        id = Material.getMaterial(s[0].toUpperCase()).getId();
      } catch (Throwable e) {
        id = Integer.valueOf(s[0]);
      }
    } catch (Throwable e) {
      SU.cs.sendMessage("§cInvalid item: §e" + in);
    }
    if (s.length == 2)
      try {
        data = Short.valueOf(s[1]);
        anydata = false;
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
  }

  /**
   * Makes a new BlockData from an ItemStack
   *
   * @param is - The convertable ItemStack
   */
  public BlockData(ItemStack is) {
    if (is == null || is.getType() == Material.AIR)
      return;
    id = is.getType().getId();
    data = is.getDurability();
    anydata = false;
  }

  public BlockData(Material type) {
    id = type.getId();
  }

  public BlockData(Material type, short durability) {
    id = type.getId();
    data = durability;
    anydata = false;
  }

  @Override
  public int compareTo(BlockData o) {
    return ((Integer) hashCode()).compareTo(o.hashCode());
  }

  public Material getType() {
    return Items.getMaterial(id);
  }

  public int hashCode() {
    return (id << 5) + (anydata ? 16 : data);
  }

  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != BlockData.class) {
      return false;
    }
    BlockData bd = (BlockData) obj;
    return bd.id == id && (bd.data == data || bd.anydata || anydata);
  }

  /**
   * Makes a copy of the BlockData, storing all it's parameters
   *
   * @return The copy of the BlockData
   */
  public BlockData clone() {
    return anydata ? new BlockData(id) : new BlockData(id, data);
  }

  @Override
  public String toString() {
    Material m = Items.getMaterial(id);
    String sid = m == null ? "" + id : m.name();
    return anydata ? sid : sid + ':' + data;
  }

  /**
   * Checks if the given block has the same type as this block data.
   *
   * @param b - Checkable block
   * @return True if the block's type is the same as this block data
   */
  public boolean isBlock(Block b) {
    int bid = b.getType().getId();
    byte bdata = b.getData();
    return id == bid && (anydata || bdata == data);
  }

  public void sendChange(Player plr, Location loc) {
    plr.sendBlockChange(loc, getType(), (byte) data);
  }

  /**
   * Sets the given blocks type and id to the one stored by this BlockData with allowing Minecraft physics calculations.
   *
   * @param b - Setable block
   */
  @SneakyThrows
  public void setBlock(Block b) {
    b.setType(getType());
    if (Reflection.ver.isBellow(ServerVersion.v1_12))
      Reflection.getMethod(Block.class, "setData", byte.class).invoke(b, (byte) data);
  }

  /**
   * Sets the given blocks type and id to the one stored by this BlockData without allowing Minecraft physics calculations.
   *
   * @param b - Setable block
   */
  @SneakyThrows
  public void setBlockNoPhysics(Block b) {
    b.setType(getType());
    if (Reflection.ver.isBellow(ServerVersion.v1_12))
      Reflection.getMethod(Block.class, "setData", byte.class, boolean.class).invoke(b, (byte) data, false);
  }

  /**
   * Converts this block data to an item
   *
   * @return The conversion result
   */
  public ItemStack toItem() {
    return new ItemStack(getType(), 1, (short) data);
  }
}

