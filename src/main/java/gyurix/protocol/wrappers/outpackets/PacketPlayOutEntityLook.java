package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityLook extends WrappedPacket {
  public int entityId;
  public boolean onGround;
  public byte pitch;
  public byte yaw;
  private static final Constructor con = Reflection.getConstructor(Reflection.getNMSClass(
          "PacketPlayOutEntity$PacketPlayOutEntityLook"), int.class, byte.class, byte.class, boolean.class);

  public PacketPlayOutEntityLook() {

  }

  public PacketPlayOutEntityLook(int eid, float yaw, float pitch, boolean onGround) {
    entityId = eid;
    this.yaw = (byte) (yaw * 256.0 / 360.0);
    this.pitch = (byte) (pitch * 256.0 / 360.0);
    this.onGround = onGround;
  }

  @Override
  public Object getVanillaPacket() {
    try {
      return con.newInstance(entityId, yaw, pitch, onGround);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.EntityLook.getPacketData(packet);
    entityId = (int) d[0];
    yaw = (byte) d[1];
    pitch = (byte) d[2];
    onGround = (boolean) d[3];
  }
}
