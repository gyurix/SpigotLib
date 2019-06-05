package gyurix.protocol.wrappers.outpackets;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.GameProfile;
import gyurix.protocol.utils.WorldType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import org.bukkit.GameMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PacketPlayOutPlayerInfo extends WrappedPacket {
  public PlayerInfoAction action = PlayerInfoAction.ADD_PLAYER;
  public ArrayList<PlayerInfoData> players = new ArrayList<>();

  public PacketPlayOutPlayerInfo() {

  }

  public PacketPlayOutPlayerInfo(PlayerInfoAction action, PlayerInfoData... pls) {
    this.action = action;
    for (PlayerInfoData pid : pls)
      players.add(pid);
  }

  @Override
  public Object getVanillaPacket() {
    return PacketOutType.PlayerInfo.newPacket(action.toNMS(), toVanillaDataList());
  }

  @Override
  public void loadVanillaPacket(Object packet) {
    Object[] d = PacketOutType.PlayerInfo.getPacketData(packet);
    action = PlayerInfoAction.valueOf(d[0].toString());
    loadVanillaDataList((List) d[1]);
  }

  private void loadVanillaDataList(List l) {
    players = new ArrayList<>();
    for (Object o : l) {
      players.add(new PlayerInfoData(o));
    }
  }

  private List toVanillaDataList() {
    List l = new ArrayList();
    for (PlayerInfoData p : players) {
      l.add(p.toNMS());
    }
    return l;
  }

  public enum PlayerInfoAction implements WrappedData {
    ADD_PLAYER,
    UPDATE_GAME_MODE,
    UPDATE_LATENCY,
    UPDATE_DISPLAY_NAME,
    REMOVE_PLAYER;
    private static final Method valueOf = Reflection.getMethod(
            Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"), "valueOf", String.class);

    public Object toNMS() {
      try {
        return valueOf.invoke(null, name());
      } catch (Throwable e) {
        return null;
      }
    }
  }

  public static class PlayerInfoData implements WrappedData {
    private static final Class vanillaParent = Reflection.getNMSClass("PacketPlayOutPlayerInfo");
    private static final Class vanillaCl = Reflection.getInnerClass(vanillaParent, "PlayerInfoData");
    private static final Field pingF = Reflection.getFirstFieldOfType(vanillaCl, int.class),
            gpF = Reflection.getFirstFieldOfType(vanillaCl, com.mojang.authlib.GameProfile.class),
            gmF = Reflection.getFirstFieldOfType(vanillaCl, WorldType.enumGmCl),
            icbcF = Reflection.getFirstFieldOfType(vanillaCl, ChatAPI.icbcClass);
    private static final Constructor vanillaConst = Reflection.getConstructor(vanillaCl,
            vanillaParent, com.mojang.authlib.GameProfile.class, int.class, WorldType.enumGmCl, ChatAPI.icbcClass);
    public ChatTag displayName;
    public GameMode gameMode;
    public GameProfile gameProfile;
    public int ping;

    public PlayerInfoData() {

    }

    public PlayerInfoData(int ping, GameMode gm, GameProfile gp, ChatTag dn) {
      this.ping = ping;
      gameMode = gm;
      gameProfile = gp;
      displayName = dn;
    }

    public PlayerInfoData(Object vd) {
      try {
        ping = pingF.getInt(vd);
        Object nmsGm = gmF.get(vd);
        gameMode = nmsGm == null ? null : GameMode.valueOf(nmsGm.toString());
        gameProfile = new GameProfile(gpF.get(vd));
        displayName = ChatTag.fromICBC(icbcF.get(vd));
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    public Object toNMS() {
      try {
        return vanillaConst.newInstance(null, gameProfile.toNMS(), ping,
                WorldType.toVanillaGameMode(gameMode), displayName == null ? null : displayName.toICBC());
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
      return null;
    }
  }
}
