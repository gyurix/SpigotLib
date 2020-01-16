package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.Vector;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotutils.LocationData;
import gyurix.spigotutils.ServerVersion;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.IRegistry;

import java.util.UUID;

public class PacketPlayOutSpawnEntity extends WrappedPacket {
  public int entityId;
  public int entityTypeId;
  public UUID entityUUID;
  public int objectData;
  public float pitch;
  public float speedX;
  public float speedY;
  public float speedZ;
  public double x;
  public double y;
  public float yaw;
  public double z;

  public PacketPlayOutSpawnEntity() {

  }

  public PacketPlayOutSpawnEntity(int id, int type, UUID uid, LocationData loc, Vector vec) {
    entityId = id;
    entityTypeId = type;
    entityUUID = uid;
    setLocation(loc);
    if (vec != null)
      setVelocity(vec);
  }

  public int convertSpeed(float num) {
    return (int) (((double) num < -3.9 ? -3.9 : Math.min(num, 3.9)) * 8000.0);
  }

  @Override
  public Object getVanillaPacket() {
    return Reflection.ver.isAbove(ServerVersion.v1_10) ? PacketOutType.SpawnEntity.newPacket(entityId, entityUUID, x, y, z,
            convertSpeed(speedX), convertSpeed(speedY), convertSpeed(speedZ),
            (int) ((double) (pitch * 256.0f) / 360.0), (int) ((double) (yaw * 256.0f) / 360.0), entityTypeId, objectData) :
            PacketOutType.SpawnEntity.newPacket(entityId, (int) (x * 32), (int) (y * 32), (int) (z * 32),
                    convertSpeed(speedX), convertSpeed(speedY), convertSpeed(speedZ),
                    (int) ((double) (pitch * 256.0f) / 360.0), (int) ((double) (yaw * 256.0f) / 360.0),
                    Reflection.ver.isAbove(ServerVersion.v1_14) ? IRegistry.ENTITY_TYPE.fromId(entityTypeId) : entityTypeId, objectData);
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] o = PacketOutType.SpawnEntity.getPacketData(packet);
    entityId = (int) o[0];
    int st = 5;
    if (Reflection.ver.isAbove(ServerVersion.v1_10)) {
      entityUUID = (UUID) o[1];
      x = (double) o[2];
      y = (double) o[3];
      z = (double) o[4];
    } else {
      x = (double) (int) o[1] / 32.0;
      y = (double) (int) o[2] / 32.0;
      z = (double) (int) o[3] / 32.0;
      st = 4;
    }
    speedX = (float) (int) o[st] / 8000.0f;
    speedY = (float) (int) o[st + 1] / 8000.0f;
    speedZ = (float) (int) o[st + 2] / 8000.0f;
    pitch = (float) (int) o[st + 3] / 256.0f * 360.0f;
    yaw = (float) (int) o[st + 4] / 256.0f * 360.0f;
    if (Reflection.ver.isAbove(ServerVersion.v1_14))
      entityTypeId = IRegistry.ENTITY_TYPE.a((EntityTypes<?>) o[st + 5]);
    else
      entityTypeId = (int) o[st + 5];
    objectData = (int) o[st + 6];
  }

  public void setLocation(LocationData loc) {
    x = loc.x;
    y = loc.y;
    z = loc.z;
    yaw = loc.yaw;
    pitch = loc.pitch;
  }

  public void setVelocity(Vector vec) {
    speedX = (float) vec.x;
    speedY = (float) vec.y;
    speedZ = (float) vec.z;
  }
}

