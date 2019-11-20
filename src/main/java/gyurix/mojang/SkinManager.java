package gyurix.mojang;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gyurix.protocol.utils.GameProfile;
import gyurix.spigotlib.SU;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SkinManager {
  private static final Cache<String, GameProfile.Property> skinCache = CacheBuilder.newBuilder()
          .expireAfterAccess(10, TimeUnit.MINUTES)
          .build();

  public static GameProfile.Property getSkin(String name) {
    try {
      return skinCache.get(name, () -> MojangAPI.getProfileWithSkin(getUUID(name)).properties.get(0));
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
    }
    return null;
  }

  public static UUID getUUID(String name) {
    OfflinePlayer op = Bukkit.getOfflinePlayer(name);
    if (op == null) {
      System.out.println("[SpigotLib] [SkinManager] " + name + " player is NULL");
      return MojangAPI.getProfile(name).id;
    }
    if (op.getUniqueId() == null) {
      System.out.println("[SpigotLib] [SkinManager] " + name + " player.getUniqueId() is NULL");
      return MojangAPI.getProfile(name).id;
    }
    return op.getUniqueId();
  }
}
