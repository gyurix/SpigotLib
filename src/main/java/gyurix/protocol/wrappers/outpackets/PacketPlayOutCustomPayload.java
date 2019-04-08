package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static gyurix.protocol.Reflection.*;

/**
 * Created by gyurix on 25/03/2017.
 */
public class PacketPlayOutCustomPayload extends WrappedPacket {
  private static final Constructor con = getConstructor(getNMSClass("PacketDataSerializer"), ByteBuf.class);
  private static final Field f = getField(getNMSClass("PacketDataSerializer"), "a");
  public String channel;
  public byte[] data;

  public PacketPlayOutCustomPayload() {

  }

  public PacketPlayOutCustomPayload(String channel, byte[] data) {
    this.channel = channel;
    this.data = data;
  }

  @Override
  public Object getVanillaPacket() {
    try {
      if (ver.isAbove(ServerVersion.v1_8))
        return PacketOutType.CustomPayload.newPacket(channel, con.newInstance(ByteBufAllocator.DEFAULT.buffer().writeBytes(data)));
      else
        return PacketOutType.CustomPayload.newPacket(channel, data);
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  @Override
  public void loadVanillaPacket(Object obj) {
    Object[] data = PacketOutType.CustomPayload.getPacketData(obj);
    channel = (String) data[0];
    try {
      if (ver.isAbove(ServerVersion.v1_8))
        this.data = ((ByteBuf) f.get(data[1])).array();
      else
        this.data = (byte[]) data[1];
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
  }
}
