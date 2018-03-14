package gyurix.nbt;

import gyurix.spigotlib.SU;
import io.netty.buffer.ByteBuf;

import java.lang.Byte;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import static gyurix.nbt.NBTTagType.*;
import static gyurix.spigotlib.SU.utf8;

public class NBTCompound extends HashMap<String, NBTTag> implements NBTTag {
    public NBTCompound() {
        super();
    }

    public boolean getBoolean(String key) {
        NBTTag tag = get(key);
        return tag != null && tag instanceof NBTPrimitive && (Byte) ((NBTPrimitive) tag).getData() == 1;
    }

    public NBTCompound getCompound(String key) {
        NBTTag tag = get(key);
        if (tag == null || !(tag instanceof NBTCompound)) {
            tag = new NBTCompound();
            put(key, tag);
        }
        return (NBTCompound) tag;
    }

    public NBTList getList(String key) {
        NBTTag tag = get(key);
        if (tag == null || !(tag instanceof NBTList)) {
            tag = new NBTList();
            put(key, tag);
        }
        return (NBTList) tag;
    }

    public NBTCompound set(String key, Object value) {
        if (value == null) {
            remove(key);
        } else {
            put(key, tag(value));
        }
        return this;
    }

    @Override
    public Object toNMS() {
        try {
            Object tag = Compound.getNmsConstructor().newInstance();
            Map m = (Map) Compound.getNmsDataField().get(tag);
            for (Entry<String, NBTTag> e : entrySet())
                m.put(e.getKey(), e.getValue().toNMS());
            return tag;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, NBTTag> e : entrySet()) {
            sb.append("\n\u00a7e").append((Object) e.getKey()).append(":\u00a7b ").append(e.getValue());
        }
        return sb.length() == 0 ? "{}" : "{" + sb.substring(1) + "}";
    }

    public void write(ByteBuf buf) {
        for (Map.Entry<String, NBTTag> e : entrySet()) {
            buf.writeByte(of(e.getValue()).ordinal());
            byte[] a = e.getKey().getBytes(utf8);
            buf.writeShort(a.length);
            buf.writeBytes(a);
            e.getValue().write(buf);
        }
        buf.writeByte(0);
    }
}

