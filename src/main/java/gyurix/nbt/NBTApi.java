package gyurix.nbt;

import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.bukkit.entity.Entity;

import java.io.DataInputStream;
import java.lang.reflect.Method;

import static gyurix.nbt.NBTTagType.Compound;
import static gyurix.nbt.NBTTagType.tag;
import static gyurix.protocol.Reflection.getNMSClass;
import static gyurix.spigotlib.SU.utf8;

public final class NBTApi {
    static Method entityFillNBTTag;
    static Method getEntityHandle;
    static Class nmsEntityClass;
    static Method setEntityNBTData;

    static {
        getEntityHandle = Reflection.getMethod(Reflection.getOBCClass("entity.CraftEntity"), "getHandle");
        nmsEntityClass = getNMSClass("Entity");
        entityFillNBTTag = Reflection.getMethod(nmsEntityClass, "c", Compound.getNmsClass());
        setEntityNBTData = Reflection.getMethod(nmsEntityClass, "f", Compound.getNmsClass());
    }

    public static NBTCompound getNbtData(Entity ent) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            Object tag = Compound.getNmsConstructor().newInstance();
            entityFillNBTTag.invoke(nmsEntity, tag);
            return (NBTCompound) Compound.makeTag(tag);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    public static String readString(DataInputStream bis) throws Throwable {
        short s = bis.readShort();
        byte[] d = new byte[s];
        bis.read(d);
        return new String(d, utf8);
    }

    public static NBTTag readTag(DataInputStream bis, byte type) throws Throwable {
        switch (type) {
            case 0:
                return tag(null);
            case 1:
                return tag(bis.readByte());
            case 2:
                return tag(bis.readShort());
            case 3:
                return tag(bis.readInt());
            case 4:
                return tag(bis.readLong());
            case 5:
                return tag(bis.readFloat());
            case 6:
                return tag(bis.readDouble());
            case 7: {
                int len = bis.readInt();
                byte[] ar = new byte[len];
                bis.read(ar);
                return tag(ar);
            }
            case 8:
                return tag(readString(bis));
            case 9: {
                byte listType = bis.readByte();
                int len = bis.readInt();
                NBTList out = new NBTList();
                if (listType == 0 || len == 0)
                    return out;
                for (int i = 0; i < len; ++i)
                    out.add(readTag(bis, listType));
                return out;
            }
            case 10: {
                NBTCompound out = new NBTCompound();
                while (true) {
                    byte compType = bis.readByte();
                    if (compType == 0)
                        return out;
                    out.put(readString(bis), readTag(bis, compType));
                }
            }
            case 11: {
                int len = bis.readInt();
                int[] ar = new int[len];
                for (int i = 0; i < len; ++i)
                    ar[i] = bis.readInt();
                return tag(ar);
            }
        }
        throw new RuntimeException("§cUnknown NBT tag type - " + type);
    }

    public static ByteBuf save(String title, NBTCompound comp) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer();
        buf.writeByte(10);
        buf.writeShort(title.length());
        buf.writeBytes(title.getBytes());
        comp.write(buf);
        return buf;
    }

    public static void setNbtData(Entity ent, NBTCompound data) {
        try {
            Object nmsEntity = getEntityHandle.invoke(ent);
            setEntityNBTData.invoke(nmsEntity, data.toNMS());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

