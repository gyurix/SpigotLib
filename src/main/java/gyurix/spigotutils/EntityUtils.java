package gyurix.spigotutils;

import gyurix.nbt.NBTApi;
import gyurix.nbt.NBTCompound;
import gyurix.nbt.NBTPrimitive;
import gyurix.protocol.utils.DataWatcher;
import gyurix.protocol.utils.WorldType;
import gyurix.spigotlib.SU;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static gyurix.protocol.Reflection.*;

/**
 * Created by GyuriX on 2016. 06. 09..
 */
public class EntityUtils {
    public static final Class craftEntity = getOBCClass("entity.CraftEntity"),
            nmsEntityCL = getNMSClass("Entity"),
            craftWorldCL = getOBCClass("CraftWorld"),
            nmsWorldCL = getNMSClass("World"),
            nmsWorldDataCL = getNMSClass("WorldData");
    public static final Method setLocationM = getMethod(nmsEntityCL, "setLocation", double.class, double.class, double.class, float.class, float.class),
            bukkitEntityM = getMethod(nmsEntityCL, "getBukkitEntity");
    public static Field killerField = getField(getNMSClass("EntityLiving"), "killer"),
            nmsEntityF = getField(craftEntity, "entity"),
            nmsWorldF = getField(craftWorldCL, "world"),
            nmsWorldTypeF = getFirstFieldOfType(nmsWorldDataCL, WorldType.worldTypeCl),
            nmsWorldDataF = getField(nmsWorldCL, "worldData"),
            dataWatcherF = getField(nmsEntityCL, "datawatcher"),
            craftWorldF = getField(nmsWorldCL, "world");

    /**
     * Converts the given NMS entity to a Bukkit entity
     *
     * @param ent - The NMS entity
     * @return The Bukkit entity
     */
    public static Entity getBukkitEntity(Object ent) {
        try {
            return (Entity) bukkitEntityM.invoke(ent);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Converts the given NMS World or WorldServer to Bukkit World
     *
     * @param world - The Bukkit world
     * @return The NMS entity
     */
    public static World getBukkitWorld(Object world) {
        try {
            return (World) craftWorldF.get(world);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Wraps the data watcher of the given entity
     *
     * @param ent - The Bukkit entity
     * @return The wrapped data watcher of the entity
     */
    public static DataWatcher getDataWatcher(Entity ent) {
        try {
            return new DataWatcher(dataWatcherF.get(nmsEntityF.get(ent)));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static Entity getEntityDamager(Entity ent) {
        if (ent instanceof Projectile) {
            ProjectileSource src = ((Projectile) ent).getShooter();
            if (src instanceof Entity)
                return (Entity) src;
            return null;
        }
        return ent;
    }

    /**
     * Converts the given Bukkit entity to an NMS entity
     *
     * @param ent - The Bukkit entity
     * @return The NMS entity
     */
    public static Object getNMSEntity(Entity ent) {
        try {
            return nmsEntityF.get(ent);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Converts the given Bukkit world to an NMS WorldServer
     *
     * @param world - The Bukkit world
     * @return The NMS entity
     */
    public static Object getNMSWorld(World world) {
        try {
            return nmsWorldF.get(world);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    public static Player getPlayerDamager(Entity ent) {
        if (ent instanceof Player)
            return (Player) ent;
        else if (ent instanceof Projectile) {
            ProjectileSource src = ((Projectile) ent).getShooter();
            if (src instanceof Player)
                return (Player) src;
        }
        return null;
    }

    /**
     * Get the nms WorldData of a world
     *
     * @param w - The world
     * @return The nms WorldData of the world
     */
    public static Object getWorldData(World w) {
        try {
            return nmsWorldDataF.get(nmsWorldF.get(w));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Get the type of a world
     *
     * @param worldData - The world data
     * @return The type of the given world data
     */
    public static WorldType getWorldType(Object worldData) {
        try {
            return WorldType.fromVanillaWorldType(nmsWorldTypeF.get(worldData));
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Checks if the given entity is in NoAI mode
     *
     * @param ent - Target entity
     * @return The NoAI mode of the given entity
     */
    public static boolean hasNoAI(LivingEntity ent) {
        if (ent == null)
            return false;
        NBTCompound nbt = NBTApi.getNbtData(ent);
        NBTPrimitive noAI = (NBTPrimitive) nbt.get("NoAI");
        return noAI != null && (int) noAI.getData() == 1;
    }

    /**
     * Sets the data watcher of the given entity
     *
     * @param ent - The Bukkit entity
     * @param dw  - The DataWatcher
     */
    public static void setDataWatcher(Entity ent, DataWatcher dw) {
        try {
            dataWatcherF.set(nmsEntityF.get(ent), dw.toNMS());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    /**
     * Sets the killer of the given entity
     *
     * @param ent    - The entity
     * @param killer - The new killer of the entity
     */
    public static void setKiller(LivingEntity ent, Player killer) {
        Object nmsEnt = getNMSEntity(ent);
        try {
            killerField.set(nmsEnt, killer == null ? null : getNMSEntity(killer));
        } catch (IllegalAccessException e) {

        }
    }

    /**
     * Sets the NoAI mode of the given entity
     *
     * @param ent  - The entity
     * @param noAi - The new NoAI mode
     */
    public static void setNoAI(LivingEntity ent, boolean noAi) {
        if (ent == null)
            return;
        NBTCompound nbt = NBTApi.getNbtData(ent);
        nbt.set("NoAI", noAi ? 1 : 0);
        NBTApi.setNbtData(ent, nbt);
    }

    /**
     * Teleport an entity to the given location without being blocked by passengers
     *
     * @param ent - The entity
     * @param loc - Teleport destination
     */
    public static void teleport(Entity ent, Location loc) {
        try {
            setLocationM.invoke(nmsEntityF.get(ent), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
