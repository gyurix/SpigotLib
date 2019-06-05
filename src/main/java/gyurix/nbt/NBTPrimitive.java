package gyurix.nbt;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.spigotlib.SU;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import static gyurix.spigotlib.SU.utf8;

@Data
public class NBTPrimitive implements NBTTag, StringSerializable {
  private Object data;

  public NBTPrimitive() {
  }

  public NBTPrimitive setData(Object data) {
    this.data = data;
    return this;
  }

  @Override
  public Object toNMS() {
    try {
      return NBTTagType.of(data).getNmsConstructor().newInstance(data);
    } catch (Throwable e) {
      SU.cs.sendMessage("§eError on converting " + data + " " + data.getClass() + " to NMS.");
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return null;
    }
  }

  public String toString() {
    return data.toString();
  }

  public void write(ByteBuf buf) {
    if (data instanceof byte[]) {
      byte[] d = (byte[]) data;
      buf.writeInt(d.length);
      buf.writeBytes(d);
    } else if (data instanceof int[]) {
      int[] d = (int[]) data;
      buf.writeInt(d.length);
      for (int i : d)
        buf.writeInt(i);
    } else if (data instanceof String) {
      String d = (String) data;
      byte[] bytes = d.getBytes(utf8);
      buf.writeShort(bytes.length);
      buf.writeBytes(bytes);
    } else if (data instanceof Byte)
      buf.writeByte((byte) data);
    else if (data instanceof Short)
      buf.writeShort((short) data);
    else if (data instanceof Integer)
      buf.writeInt((int) data);
    else if (data instanceof Long)
      buf.writeLong((long) data);
    else if (data instanceof Float)
      buf.writeFloat((float) data);
    else if (data instanceof Double)
      buf.writeDouble((double) data);

  }
}

