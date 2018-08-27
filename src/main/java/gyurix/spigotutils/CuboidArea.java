package gyurix.spigotutils;

import com.sk89q.worldedit.bukkit.selections.Selection;
import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.utils.BlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static gyurix.spigotlib.SU.rand;
import static java.lang.Integer.MIN_VALUE;

/**
 * Class representing a CuboidArea, with a first and second block location and
 * with or without world parameter
 */
public class CuboidArea implements StringSerializable, Cloneable {
    /**
     * The first position of this CuboidArea, containing the minX, minY and minZ coordinates
     */
    public BlockLocation pos1;
    /**
     * The second position of this CuboidArea, containing the maxX, maxY and maxZ coordinates
     */
    public BlockLocation pos2;
    /**
     * The world of this CuboidArea, might be null, if it's not defined
     */
    public String world;

    /**
     * Constructs a new not properly set up CuboidArea
     */
    public CuboidArea() {
    }

    /**
     * Constructs a new CuboidArea from the given String
     *
     * @param in - The String from which the CuboidArea should be constructed
     *           Accepted input String formats:
     *           world
     *           minX minY minZ maxX maxY maxZ
     *           world minX minY minZ maxX maxY maxZ
     */
    public CuboidArea(String in) {
        try {
            String[] d = in.split(" ", 7);
            if (d.length == 1) {
                world = d[0];
                return;
            } else if (d.length == 6) {
                pos1 = new BlockLocation(Integer.valueOf(d[0]), Integer.valueOf(d[1]), Integer.valueOf(d[2]));
                pos2 = new BlockLocation(Integer.valueOf(d[3]), Integer.valueOf(d[4]), Integer.valueOf(d[5]));
            } else {
                world = d[0];
                pos1 = new BlockLocation(Integer.valueOf(d[1]), Integer.valueOf(d[2]), Integer.valueOf(d[3]));
                pos2 = new BlockLocation(Integer.valueOf(d[4]), Integer.valueOf(d[5]), Integer.valueOf(d[6]));
            }
            fix();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct a new CuboidArea from a WorldEdit selection
     *
     * @param sel       - The WorldEdit selection
     * @param saveWorld - True if not only the min, and max points, but also
     *                  the World should be saved
     */
    public CuboidArea(Selection sel, boolean saveWorld) {
        this(sel);
        if (saveWorld)
            world = sel.getWorld().getName();
    }

    /**
     * Construct a new CuboidArea from a WorldEdit selection WITHOUT saving the world of it
     *
     * @param sel - The WorldEdit selection
     */
    public CuboidArea(Selection sel) {
        pos1 = new BlockLocation(sel.getMinimumPoint());
        pos2 = new BlockLocation(sel.getMaximumPoint());
        fix();
    }

    public CuboidArea(String world, BlockLocation pos1, BlockLocation pos2) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public CuboidArea(LocationData pos1, LocationData pos2) {
        this.pos1 = pos1.getBlockLocation();
        this.pos2 = pos2.getBlockLocation();
    }


    public CuboidArea(BlockLocation pos1, BlockLocation pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public static void resetOutlineBlock(Block block, Player plr) {
        plr.sendBlockChange(block.getLocation(), block.getType(), block.getData());
    }

    /**
     * Makes a clone of the CuboidArea by keeping all of it's properties
     *
     * @return The cloned version of this CuboidArea
     */
    public CuboidArea clone() {
        return new CuboidArea(world, pos1.clone(), pos2.clone());
    }

    @Override
    public String toString() {
        return world == null ? pos1 + " " + pos2 : world + ' ' + pos1 + ' ' + pos2;
    }

    /**
     * Makes a clone of this CuboidArea and fixes it's pos1 and pos2 using the @see fix method
     *
     * @return The cloned and fixed version of this CuboidArea
     */
    public CuboidArea cloneFixed() {
        CuboidArea area = clone();
        area.fix();
        return area;
    }

    public boolean contains(Location loc) {
        return loc.getX() + 0.5 >= pos1.x && loc.getY() >= pos1.y && loc.getZ() + 0.5 >= pos1.z && loc.getX() - 1.5 <= pos2.x && loc.getY() <= pos2.y && loc.getZ() - 1.5 <= pos2.z;
    }

    public boolean contains(Block loc) {
        return loc.getX() >= pos1.x && loc.getY() >= pos1.y && loc.getZ() >= pos1.z
                && loc.getX() <= pos2.x && loc.getY() <= pos2.y && loc.getZ() <= pos2.z;
    }

    public boolean contains(LocationData loc) {
        return !(world != null && loc.world != null && !world.equals(loc.world)) && loc.x + 0.5 >= pos1.x && loc.y >= pos1.y && loc.z + 0.5 >= pos1.z && loc.x - 1.5 <= pos2.x && loc.y <= pos2.y && loc.z - 1.5 <= pos2.z;
    }

    public boolean contains(BlockLocation loc) {
        return loc.x >= pos1.x && loc.y >= pos1.y && loc.z >= pos1.z
                && loc.x <= pos2.x && loc.y <= pos2.y && loc.z <= pos2.z;
    }

    public boolean contains(int x, int z) {
        return pos1.x <= x && pos2.x >= x && pos1.z <= z && pos2.z >= z;
    }

    /**
     * Fix the min and max points for ensuring that the pos1 contains the minX, minY and minZ, while
     * the pos2 contains the maxX, maxY and maxZ and they does not contain these informations oppositely
     */
    public void fix() {
        int tmp;
        if (pos1.x > pos2.x) {
            tmp = pos1.x;
            pos1.x = pos2.x;
            pos2.x = tmp;
        }
        if (pos1.y > pos2.y) {
            tmp = pos1.y;
            pos1.y = pos2.y;
            pos2.y = tmp;
        }
        if (pos1.z > pos2.z) {
            tmp = pos1.z;
            pos1.z = pos2.z;
            pos2.z = tmp;
        }
    }

    public boolean isBorder(int x, int z) {
        return (x == pos1.x || z == pos1.z || x == pos2.x || z == pos2.z) && contains(x, z);
    }

    public boolean isDefined() {
        return pos1 != null && pos2 != null && pos1.isDefined() && pos2.isDefined();
    }

    public Location randomLoc(World w) {
        return new Location(w, rand(pos1.x, pos2.x), rand(pos1.y, pos2.y), rand(pos1.z, pos2.z));
    }

    public Location randomLoc() {
        return new Location(Bukkit.getWorld(world), rand(pos1.x, pos2.x), rand(pos1.y, pos2.y), rand(pos1.z, pos2.z));
    }

    public void resetOutlineWithBlock(Player plr) {
        if (world != null && !plr.getWorld().getName().equals(world))
            return;
        World w = plr.getWorld();
        for (int x = pos1.x + 1; x < pos2.x; x++) {
            resetOutlineBlock(w.getBlockAt(x, pos1.y, pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(x, pos1.y, pos2.z), plr);
            resetOutlineBlock(w.getBlockAt(x, pos2.y, pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(x, pos2.y, pos2.z), plr);
        }
        for (int y = pos1.y + 1; y < pos2.y; y++) {
            resetOutlineBlock(w.getBlockAt(pos1.x, y, pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(pos1.x, y, pos2.z), plr);
            resetOutlineBlock(w.getBlockAt(pos2.x, y, pos1.z), plr);
            resetOutlineBlock(w.getBlockAt(pos2.x, y, pos2.z), plr);
        }
        for (int z = pos1.z; z <= pos2.z; z++) {
            resetOutlineBlock(w.getBlockAt(pos1.x, pos1.y, z), plr);
            resetOutlineBlock(w.getBlockAt(pos1.x, pos2.y, z), plr);
            resetOutlineBlock(w.getBlockAt(pos2.x, pos1.y, z), plr);
            resetOutlineBlock(w.getBlockAt(pos2.x, pos2.y, z), plr);
        }
    }

    public void showOutlineWithBlock(Player plr, BlockData bd) {
        if (world != null && !plr.getWorld().getName().equals(world))
            return;
        World w = plr.getWorld();
        for (int x = pos1.x + 1; x < pos2.x; x++) {
            bd.sendChange(plr, new Location(w, x, pos1.y, pos1.z));
            bd.sendChange(plr, new Location(w, x, pos1.y, pos2.z));
            bd.sendChange(plr, new Location(w, x, pos2.y, pos1.z));
            bd.sendChange(plr, new Location(w, x, pos2.y, pos2.z));
        }
        for (int y = pos1.y + 1; y < pos2.y; y++) {
            bd.sendChange(plr, new Location(w, pos1.x, y, pos1.z));
            bd.sendChange(plr, new Location(w, pos1.x, y, pos2.z));
            bd.sendChange(plr, new Location(w, pos2.x, y, pos1.z));
            bd.sendChange(plr, new Location(w, pos2.x, y, pos2.z));
        }
        for (int z = pos1.z; z <= pos2.z; z++) {
            bd.sendChange(plr, new Location(w, pos1.x, pos1.y, z));
            bd.sendChange(plr, new Location(w, pos1.x, pos2.y, z));
            bd.sendChange(plr, new Location(w, pos2.x, pos1.y, z));
            bd.sendChange(plr, new Location(w, pos2.x, pos2.y, z));
        }
    }

    public int size() {
        return (pos2.x - pos1.x + 1) * (pos2.y - pos1.y + 1) * (pos2.z - pos1.z + 1);
    }

    /**
     * Converts this CuboidArea to an @see UnlimitedYArea
     *
     * @return The conversion result
     */
    public UnlimitedYArea toUnlimitedYArea() {
        return new UnlimitedYArea(pos1 == null ? MIN_VALUE : pos1.x, pos1 == null ? MIN_VALUE : pos1.z, pos2 == null ? MIN_VALUE : pos2.x, pos2 == null ? MIN_VALUE : pos2.z);
    }
}
