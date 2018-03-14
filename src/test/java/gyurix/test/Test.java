package gyurix.test;

import com.google.common.collect.Lists;
import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.nbt.NBTCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static gyurix.nbt.NBTTagType.tag;

/**
 * Created by GyuriX on 2016. 07. 31..
 */
public class Test {
    public static void main(String[] args) throws Throwable {
        ConfigData cd = new ConfigData("String data");
        cd.listData = new ArrayList<>();
        cd.listData.add(new ConfigData("le1"));
        cd.listData.add(new ConfigData("le2"));
        ConfigData cd2 = new ConfigData();
        cd2.mapData = new LinkedHashMap<>();
        cd2.mapData.put(new ConfigData("key"), cd);
        System.out.println(cd2);

        ItemStack is = new ItemStack(Material.STONE);
        NBTCompound compound = (NBTCompound) tag(new HashMap<>());
        compound.put("byte", tag((byte) 1));
        compound.put("short", tag((short) 1));
        compound.put("int", tag(1));
        compound.put("long", tag(1L));
        compound.put("float", tag(1.0f));
        compound.put("double", tag(1.0));
        compound.put("string", tag("Some text"));
        compound.put("byte[]", tag(new byte[]{1, 2, 3}));
        compound.put("int[]", tag(new int[]{1, 2, 3}));
        compound.put("List", tag(Lists.newArrayList(tag("First"), tag("Second"), tag("Third"))));
        String s1 = new ConfigData(compound).toString();
        NBTCompound compound2 = new ConfigFile(s1).data.deserialize(NBTCompound.class);
        String s2 = new ConfigData(compound2).toString();
        System.out.println("S1:\n" + s1 + "\n\n\nS2:\n" + s2 + "\n\n\nEquals: " + s1.equals(s2) + "\nCompounds equals: " + compound.equals(compound2));
        //System.out.println(ChatTag.fromColoredText("null"));
    }
}
