package gyurix.spigotutils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Area {
  /**
   * The world of this Area, might be null, if it's not defined
   */
  public String world;

  public List<Block> getBlocks() {
    if (world == null)
      throw new RuntimeException("No world argument provided");
    return getBlocks(Bukkit.getWorld(world));
  }

  public abstract List<Block> getBlocks(World w);

  public List<Block> getOutlineBlocks() {
    if (world == null)
      throw new RuntimeException("No world argument provided");
    return getOutlineBlocks(Bukkit.getWorld(world));
  }

  public abstract List<Block> getOutlineBlocks(World w);

  public void resetOutlineWithBlock(Player plr) {
    getBlocks(plr.getWorld()).forEach(b -> new BlockData(b).sendChange(plr, b.getLocation()));
  }

  public void showOutlineWithBlock(Player plr, BlockData bd) {
    getBlocks(plr.getWorld()).forEach(b -> bd.sendChange(plr, b.getLocation()));
  }
}
