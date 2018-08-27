package gyurix.spigotutils;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;

public abstract class Area {
    public List<Block> getBlocks() {
        throw new RuntimeException("No world argument provided");
    }

    public abstract List<Block> getBlocks(World w);
}
