package gyurix.nbt;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

import static gyurix.nbt.NBTTagType.List;

public class NBTList extends ArrayList<NBTTag> implements NBTTag {
    public NBTList() {
    }

    @Override
    public Object toNMS() {
        try {

            Object o = List.getNmsClass().newInstance();
            ArrayList<Object> l = new ArrayList<>();
            for (NBTTag t : this)
                l.add(t.toNMS());
            List.getNmsDataField().set(o, l);
            if (!l.isEmpty())
                NBTTagType.getNmsListTypeField().set(o, (byte) NBTTagType.of(l.get(0)).ordinal());
            return o;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return "[§b" + StringUtils.join(this, ", §b") + "§b]";
    }

    @Override
    public void write(ByteBuf buf) {
        if (isEmpty()) {
            buf.writeByte(0);
            buf.writeInt(0);
            return;
        }
        buf.writeByte(NBTTagType.of(get(0)).ordinal());
        buf.writeInt(size());
        for (NBTTag nbtTag : this)
            nbtTag.write(buf);
    }
}

