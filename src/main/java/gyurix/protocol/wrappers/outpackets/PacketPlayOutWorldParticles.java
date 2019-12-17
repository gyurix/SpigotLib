package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;

import java.lang.reflect.Method;

public class PacketPlayOutWorldParticles extends WrappedPacket {
  private static final Method enumParticleValueOf = Reflection.getMethod(Reflection.getNMSClass("EnumParticle"), "valueOf", String.class);
  public int count;
  public int[] extraData;
  public boolean longDistance;
  public String particle;
  public Object particleSettings1_14;
  public float x, y, z, offsetX, offsetY, offsetZ, data;

  public PacketPlayOutWorldParticles() {

  }

  public PacketPlayOutWorldParticles(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, int count, float data) {
    this.particle = particle;
    this.count = count;
    this.x = x;
    this.y = y;
    this.z = z;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
    this.data = data;
  }

  public PacketPlayOutWorldParticles(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, int count, float data, int[] extraData, boolean longDistance) {
    this.particle = particle;
    this.count = count;
    this.extraData = extraData;
    this.longDistance = longDistance;
    this.x = x;
    this.y = y;
    this.z = z;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
    this.data = data;
  }

  public PacketPlayOutWorldParticles(float x, float y, float z, float offsetX, float offsetY, float offsetZ, int count, float data, boolean longDistance, Object particleSettings1_14) {
    this.count = count;
    this.x = x;
    this.y = y;
    this.z = z;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
    this.data = data;
    this.longDistance = longDistance;
    this.particleSettings1_14 = particleSettings1_14;
  }


  @Override
  public Object getVanillaPacket() {
    try {
      if (Reflection.ver.isAbove(ServerVersion.v1_14))
        return PacketOutType.WorldParticles.newPacket(x, y, z, offsetX, offsetY, offsetZ, data, count, longDistance, particleSettings1_14);
      return PacketOutType.WorldParticles.newPacket(enumParticleValueOf.invoke(null, particle), x, y, z, offsetX, offsetY, offsetZ, data, count, longDistance, extraData);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.WorldParticles.getPacketData(packet);
    int from = 0;
    if (Reflection.ver.isBellow(ServerVersion.v1_13))
      particle = d[from++].toString();
    x = (float) d[from++];
    y = (float) d[from++];
    z = (float) d[from++];
    offsetX = (float) d[from++];
    offsetY = (float) d[from++];
    offsetZ = (float) d[from++];
    data = (float) d[from++];
    count = (int) d[from++];
    if (Reflection.ver.isAbove(ServerVersion.v1_8)) {
      longDistance = (boolean) d[from++];
      if (Reflection.ver.isBellow(ServerVersion.v1_13))
        extraData = (int[]) d[from++];
      else {
        particleSettings1_14 = d[from++];
      }
    }
  }
}
