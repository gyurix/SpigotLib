package gyurix.protocol.wrappers.inpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.utils.HandType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.ServerVersion;

import java.util.HashMap;

public class PacketPlayInArmAnimation extends WrappedPacket {
  public int entityId;
  public HandType hand;
  public long timestamp;
  public AnimationType type;

  @Override
  public Object getVanillaPacket() {
    return Reflection.ver.isAbove(ServerVersion.v1_9) ? PacketInType.ArmAnimation.newPacket(hand.toNMS()) : PacketInType.ArmAnimation.newPacket(timestamp);
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketInType.ArmAnimation.getPacketData(packet);
    entityId = 0;
    hand = HandType.MAIN_HAND;
    timestamp = System.currentTimeMillis();
    type = AnimationType.DAMAGE_ANIMATION;
    if (Reflection.ver.isAbove(ServerVersion.v1_9))
      hand = HandType.valueOf(d[0].toString());
    else if (Reflection.ver.isAbove(ServerVersion.v1_8))
      timestamp = (long) d[0];
    else if (Reflection.ver.isBellow(ServerVersion.v1_7)) {
      entityId = (int) d[0];
      type = AnimationType.getById((int) d[1]);
    }

  }

  public enum AnimationType {
    SWING_ARM(0), DAMAGE_ANIMATION(1), LEAVE_BED(2), EAT_FOOD(3), CRITICAL_EFFECT(4), MAGIC_CRITICAL_EFFECT(5), UNKNOWN(102), CROUCH(104), UNCROUCH(105);
    private static final HashMap<Integer, AnimationType> idMap = new HashMap<>();

    static {
      for (AnimationType at : values())
        idMap.put(at.id, at);
    }

    private int id;

    AnimationType(int id) {
      this.id = id;
    }

    public static AnimationType getById(int id) {
      return idMap.get(id);
    }

    public int getId() {
      return id;
    }
  }
}

