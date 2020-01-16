package gyurix.hologram;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.utils.DataWatcher;
import gyurix.protocol.utils.DataWatcher.WrappedItem;
import gyurix.protocol.utils.Vector;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutEntityDestroy;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutEntityMetadata;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutEntityTeleport;
import gyurix.protocol.wrappers.outpackets.PacketPlayOutSpawnEntityLiving;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.LocationData;
import gyurix.spigotutils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import static gyurix.api.VariableAPI.fillVariables;

public class HologramLine {
  public Object destroyP;
  public int id = HologramAPI.nextHologramId++;
  public LocationData loc;
  public ArrayList<WrappedItem> metaData = new ArrayList<>();
  public String text;
  public UUID uid = UUID.randomUUID();
  public HashMap<String, String> viewers = new HashMap<>();

  public HologramLine(String text, LocationData loc) {
    this.text = text;
    this.loc = loc;
    if (Reflection.ver.isAbove(ServerVersion.v1_14))
      makeMeta1_14();
    else if (Reflection.ver.isAbove(ServerVersion.v1_9))
      makeMeta1_9();
    else
      makeMeta1_8();

    destroyP = new PacketPlayOutEntityDestroy(id).getVanillaPacket();
  }

  private void applyText(String plText) {
    metaData.set(2, new WrappedItem(2, Reflection.ver.isAbove(ServerVersion.v1_14) ? Optional.of(ChatTag.fromColoredText(plText).toICBC()) : plText));
  }

  public void destroy() {
    for (String s : viewers.keySet()) {
      Player p = Bukkit.getPlayerExact(s);
      if (p != null)
        SU.tp.sendPacket(p, destroyP);
    }
  }

  public String fixEmpty(String in) {
    return in.isEmpty() ? " " : in;
  }

  private Object getSpawnPacket() {
    return new PacketPlayOutSpawnEntityLiving(id, uid, 1, loc, 0, new Vector(), new DataWatcher(metaData)).getVanillaPacket();
  }

  public void hide(Player plr) {
    SU.tp.sendPacket(plr, destroyP);
    viewers.remove(plr.getName());
  }

  public void makeMeta1_14() {
        /*
         0x01 On Fire
         0x02 Crouched
         0x08 Sprinting
         0x10 Eating/drinking/blocking
         0x20 Invisible
         0x40 Glowing effect
         0x80 Flying with elytra
         */
    metaData.add(new WrappedItem(0, (byte) 0x20)); // 0 Byte - flags
    metaData.add(new WrappedItem(1, 300));   // 1 VarInt - Air
    metaData.add(new WrappedItem(2, Optional.of(ChatTag.fromColoredText(text).toICBC())));        // 2 String - Custom name
    metaData.add(new WrappedItem(3, true));  // 3 Boolean - Custom name visible
    metaData.add(new WrappedItem(4, true));  // 4 Boolean - Silent
    metaData.add(new WrappedItem(5, true));  // 5 Boolean - No gravity
        /*
          0x01 Is hand active
          0x02 Active hand (0 = main hand, 1 = offhand)
         */
    /*metaData.add(new WrappedItem(7, (byte) 0));    // 7 Byte - hand flags
    metaData.add(new WrappedItem(8, 20f));   // 8 Float - Health
    metaData.add(new WrappedItem(9, 0));     // 9 VarInt - Potion effect color
    metaData.add(new WrappedItem(10, false)); // 10 Boolean - Is potion effect ambient
    metaData.add(new WrappedItem(11, 0));    // 11 VarInt - Number of arrows in entity*/
        /*
         0x01 is Small
         0x04 has Arms
         0x08 no BasePlate
         0x10 set Marker
         */
    /*metaData.add(new WrappedItem(13, (byte) (0x01 + 0x08 + 0x10))); // 13 Byte- flags*/
  }

  public void makeMeta1_8() {
        /*
         0x01 On Fire
         0x02 Crouched
         0x08 Sprinting
         0x10 Eating/drinking/blocking
         0x20 Invisible
         */
    metaData.add(new WrappedItem(0, (byte) 0x20)); // 0 Byte - flags
    metaData.add(new WrappedItem(1, (short) 300)); // 1 VarInt - Air
    metaData.add(new WrappedItem(2, text));        // 2 String - Custom name
    metaData.add(new WrappedItem(3, (byte) 1));    // 3 Byte - Custom name visible
    metaData.add(new WrappedItem(6, 20f));   // 6 Float - Health
    metaData.add(new WrappedItem(8, (byte) 0));    // 8 Byte - Is potion effect ambient
    metaData.add(new WrappedItem(9, (byte) 0));    // 9 Byte - Number of arrows in entity
        /*
         0x01 is Small
         0x02 has Gravity
         0x04 has Arms
         0x08 no BasePlate
         0x16 zero bounding box
         */
    metaData.add(new WrappedItem(10, (byte) (0x08 + 0x16))); // 10 Byte- flags
  }

  public void makeMeta1_9() {
        /*
         0x01 On Fire
         0x02 Crouched
         0x08 Sprinting
         0x10 Eating/drinking/blocking
         0x20 Invisible
         0x40 Glowing effect
         0x80 Flying with elytra
         */
    metaData.add(new WrappedItem(0, (byte) 0x20)); // 0 Byte - flags
    metaData.add(new WrappedItem(1, 300));   // 1 VarInt - Air
    metaData.add(new WrappedItem(2, text));        // 2 String - Custom name
    metaData.add(new WrappedItem(3, true));  // 3 Boolean - Custom name visible
    metaData.add(new WrappedItem(4, true));  // 4 Boolean - Silent
    metaData.add(new WrappedItem(5, true));  // 5 Boolean - No gravity
        /*
          0x01 Is hand active
          0x02 Active hand (0 = main hand, 1 = offhand)
         */
    metaData.add(new WrappedItem(6, (byte) 0));    // 6 Byte - hand flags
    metaData.add(new WrappedItem(7, 20f));   // 7 Float - Health
    metaData.add(new WrappedItem(8, 0));     // 8 VarInt - Potion effect color
    metaData.add(new WrappedItem(9, false)); // 9 Boolean - Is potion effect ambient
    metaData.add(new WrappedItem(10, 0));    // 10 VarInt - Number of arrows in entity
        /*
         0x01 is Small
         0x04 has Arms
         0x08 no BasePlate
         0x10 set Marker
         */
    metaData.add(new WrappedItem(11, (byte) (0x01 + 0x08 + 0x10))); // 11 Byte- flags
  }

  public void show(final Player plr) {
    String plText = fixEmpty(fillVariables(text, plr));
    SU.tp.sendPacket(plr, getSpawnPacket());
    viewers.put(plr.getName(), plText);
  }

  public void teleport(LocationData ld,boolean skipPackets) {
    this.loc = ld;
    if (!skipPackets) {
      Object teleport = new PacketPlayOutEntityTeleport(id, this.loc).getVanillaPacket();
      for (String s : viewers.keySet()) {
        Player p = Bukkit.getPlayerExact(s);
        if (p != null)
          SU.tp.sendPacket(p, teleport);
      }
    }
  }

  public void update() {
    for (Entry<String, String> e : viewers.entrySet()) {
      Player p = Bukkit.getPlayerExact(e.getKey());
      if (p != null) {
        String oldText = e.getValue();
        String newText = fixEmpty(fillVariables(text, p));
        if (oldText.equals(newText))
          continue;
        e.setValue(newText);
        applyText(newText);
        SU.tp.sendPacket(p, new PacketPlayOutEntityMetadata(id, metaData));
      }
    }
  }
}
