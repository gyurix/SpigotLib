package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.BlockLocation;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.BlockData;
import gyurix.spigotutils.BlockUtils;

/**
 * Created by GyuriX, on 2017. 02. 05..
 */
public class PacketPlayOutBlockChange extends WrappedPacket {
  public BlockData bd;
  public BlockLocation loc;

  public PacketPlayOutBlockChange() {
  }

  public PacketPlayOutBlockChange(BlockLocation loc, BlockData bd) {
    this.loc = loc;
    this.bd = bd;
  }

  @Override
  public Object getVanillaPacket() {
    return PacketOutType.BlockChange.newPacket(loc.toNMS(), BlockUtils.combinedIdToNMSBlockData(BlockUtils.getCombinedId(bd)));
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.BlockChange.getPacketData(packet);
    loc = new BlockLocation(d[0]);
    bd = BlockUtils.combinedIdToBlockData(BlockUtils.getCombinedId(d[1]));
    System.out.println(d[1] + " == " + BlockUtils.getCombinedId(d[1]) + " == " + bd);
  }
}
