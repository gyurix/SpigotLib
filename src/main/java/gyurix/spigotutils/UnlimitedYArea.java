package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

import static gyurix.spigotutils.BlockUtils.getYMax;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

/**
 * Created by GyuriX on 2016.05.15..
 */
public class UnlimitedYArea extends Area implements StringSerializable {
  public int minx = MIN_VALUE, minz = MIN_VALUE, maxx = MIN_VALUE, maxz = MIN_VALUE;

  public UnlimitedYArea(String in) {
    String[] d = in.split(" ", 4);
    minx = Integer.parseInt(d[0]);
    minz = Integer.parseInt(d[1]);
    maxx = Integer.parseInt(d[2]);
    maxz = Integer.parseInt(d[3]);
  }

  public UnlimitedYArea(Location loc, int radius) {
    this(loc.getBlockX(), loc.getBlockZ(), radius);
  }

  public UnlimitedYArea(int x, int z, int radius) {
    minx = x - radius;
    minz = z - radius;
    maxx = x + radius;
    maxz = z + radius;
  }

  public UnlimitedYArea(UnlimitedYArea area) {
    minx = area.minx;
    minz = area.minz;
    maxx = area.maxx;
    maxz = area.maxz;
  }

  public UnlimitedYArea(LocationData pos1, LocationData pos2) {
    this((int) pos1.x, (int) pos1.z, (int) pos2.x, (int) pos2.z);
  }

  public UnlimitedYArea(int minX, int minZ, int maxX, int maxZ) {
    minx = minX;
    minz = minZ;
    maxx = maxX;
    maxz = maxZ;
    fix();
  }


  public static Block getTopY(World w, int x, int z) {
    Block b = null;
    for (int y = 255; y > 0; y--) {
      b = w.getBlockAt(x, y, z);
      if (b.getType() != Material.AIR)
        return b;
    }
    return b;
  }

  public UnlimitedYArea cloneFixed() {
    UnlimitedYArea area = new UnlimitedYArea(this);
    area.fix();
    return area;
  }

  public boolean contains(Location loc) {
    return contains(loc.getBlockX(), loc.getBlockZ());
  }

  public boolean contains(int x, int z) {
    return x >= minx && z >= minz && x <= maxx && z <= maxz;
  }

  public void fix() {
    int tmp;
    if (minx > maxx) {
      tmp = minx;
      minx = maxx;
      maxx = tmp;
    }
    if (minz > maxz) {
      tmp = minz;
      minz = maxz;
      maxz = tmp;
    }
  }

  @Override
  public List<Block> getBlocks(World w) {
    List<Block> blocks = new ArrayList<>();
    for (int x = minx; x <= maxx; ++x) {
      for (int z = minx; z <= maxz; ++z) {
        for (int y = 0; y <= 255; ++y) {
          blocks.add(w.getBlockAt(x, y, z));
        }
      }
    }
    return blocks;
  }

  @Override
  public List<Block> getOutlineBlocks(World w) {
    List<Block> out = new ArrayList<>();
    for (int x = minx; x <= maxx; ++x) {
      out.add(getTopY(w, x, minz));
      out.add(getTopY(w, x, maxz));
    }
    for (int z = minz + 1; z < maxz; ++z) {
      out.add(getTopY(w, minx, z));
      out.add(getTopY(w, maxx, z));
    }
    return out;
  }

  public Location getYMaxPos1(World world) {
    return getYMax(world, minx, minz);
  }

  public Location getYMaxPos2(World world) {
    return getYMax(world, maxx, maxz);
  }

  public boolean isBorder(int x, int z) {
    return x == minx || z == minz || x == maxx || z == maxz;
  }

  public boolean isDefined() {
    return minx != MIN_VALUE && minz != MIN_VALUE && maxx != MIN_VALUE && maxz != MAX_VALUE;
  }

  public boolean isPos1Defined() {
    return minx != MIN_VALUE && minz != MIN_VALUE;
  }

  public boolean isPos2Defined() {
    return maxx != MIN_VALUE && maxz != MIN_VALUE;
  }

  public void pos1(int x, int z) {
    minx = x;
    minz = z;
  }

  public void pos2(int x, int z) {
    maxx = x;
    maxz = z;
  }


  public int size() {
    return (maxx - minx + 1) * (maxz - minz + 1);
  }

  @Override
  public String toString() {
    return minx + " " + minz + " " + maxx + " " + maxz;
  }
}
