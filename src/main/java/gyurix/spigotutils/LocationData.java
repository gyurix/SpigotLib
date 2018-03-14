package gyurix.spigotutils;

import gyurix.configfile.ConfigSerialization.StringSerializable;
import gyurix.protocol.utils.BlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class LocationData implements StringSerializable {
    public float pitch;
    public String world;
    public double x;
    public double y;
    public float yaw;
    public double z;

    public LocationData(String in) {
        String[] d = in.split(" ");
        switch (d.length) {
            case 3:
                x = Double.valueOf(d[0]);
                y = Double.valueOf(d[1]);
                z = Double.valueOf(d[2]);
                return;
            case 4:
                world = d[0];
                x = Double.valueOf(d[1]);
                y = Double.valueOf(d[2]);
                z = Double.valueOf(d[3]);
                return;
            case 5:
                x = Double.valueOf(d[0]);
                y = Double.valueOf(d[1]);
                z = Double.valueOf(d[2]);
                yaw = Float.valueOf(d[3]).floatValue();
                pitch = Float.valueOf(d[4]).floatValue();
                return;
            case 6:
                world = d[0];
                x = Double.valueOf(d[1]);
                y = Double.valueOf(d[2]);
                z = Double.valueOf(d[3]);
                yaw = Float.valueOf(d[4]).floatValue();
                pitch = Float.valueOf(d[5]).floatValue();
                return;
        }
    }

    public LocationData() {
    }

    public LocationData(LocationData ld) {
        this(ld.world, ld.x, ld.y, ld.z, ld.yaw, ld.pitch);
    }

    public LocationData(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LocationData(BlockLocation bl) {
        this(bl.x, bl.y, bl.z);
    }

    public LocationData(double x, double y, double z) {
        this(null, x, y, z, 0.0f, 0.0f);
    }

    public LocationData(Vector v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public LocationData(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }

    public LocationData(double x, double y, double z, float yaw, float pitch) {
        this(null, x, y, z, yaw, pitch);
    }

    public LocationData(Block bl) {
        this(bl.getWorld().getName(), bl.getX(), bl.getY(), bl.getZ(), 0.0f, 0.0f);
    }

    public LocationData(Location loc) {
        this(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public LocationData add(LocationData ld) {
        return ld == null ? this : add(ld.x, ld.y, ld.z);
    }

    public LocationData add(double nx, double ny, double nz) {
        x += nx;
        y += ny;
        z += nz;
        return this;
    }

    public LocationData add(double num) {
        x += num;
        y += num;
        z += num;
        return this;
    }

    public double dist(LocationData loc) {
        return Math.sqrt((loc.x - x) * (loc.x - x) + (loc.y - y) * (loc.y - y) + (loc.z - z) * (loc.z - z));
    }

    public double distNoY(LocationData loc) {
        return Math.sqrt((loc.x - x) * (loc.x - x) + (loc.z - z) * (loc.z - z));
    }

    public Block getBlock() {
        if (!isAvailable()) {
            return null;
        }
        return Bukkit.getWorld(world).getBlockAt((int) (x < 0 ? x - 0.999 : x), (int) y, (int) (z < 0 ? z - 0.999 : z));
    }

    public BlockLocation getBlockLocation() {
        return new BlockLocation((int) x, (int) y, (int) z);
    }

    public Vector getDirection() {
        Vector vector = new Vector();
        vector.setY(-Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
        return vector;
    }

    public LocationData setDirection(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        if (x == 0.0 && z == 0.0) {
            pitch = vector.getY() > 0.0 ? -90 : 90;
            return this;
        }
        double theta = Math.atan2(-x, z);
        yaw = (float) Math.toDegrees((theta + 6.283185307179586) % 6.283185307179586);
        double x2 = x * x;
        double z2 = z * z;
        double xz = Math.sqrt(x2 + z2);
        pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));
        return this;
    }

    public Location getLocation() {
        return new Location(world == null ? null : Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public Location getLocation(World w) {
        return new Location(w, x, y, z, yaw, pitch);
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public int hashCode() {
        int hash = 57 + (world != null ? world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(x) ^ Double.doubleToLongBits(x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(y) ^ Double.doubleToLongBits(y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(z) ^ Double.doubleToLongBits(z) >>> 32);
        hash = 19 * hash + Float.floatToIntBits(pitch);
        hash = 19 * hash + Float.floatToIntBits(yaw);
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LocationData)) {
            return false;
        }
        LocationData ld = (LocationData) obj;
        return (world == null && ld.world == null || world != null && ld.world != null && world.equals(ld.world)) && x == ld.x && y == ld.y && z == ld.z && yaw == ld.yaw && pitch == ld.pitch;
    }

    public LocationData clone() {
        return new LocationData(this);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (world != null) {
            out.append(' ').append(world);
        }
        out.append(' ').append(x).append(' ').append(y).append(' ').append(z);
        if (yaw != 0.0f || pitch != 0.0f)
            out.append(' ').append(yaw).append(' ').append(pitch);
        return out.substring(1);
    }

    public boolean isAvailable() {
        return world != null && Bukkit.getWorld(world) != null;
    }

    public LocationData multiple(LocationData ld) {
        return ld == null ? this : multiple(ld.x, ld.y, ld.z);
    }

    public LocationData multiple(double nx, double ny, double nz) {
        x *= nx;
        y *= ny;
        z *= nz;
        return this;
    }

    public LocationData multiple(double num) {
        x *= num;
        y *= num;
        z *= num;
        return this;
    }

    public LocationData subtract(LocationData ld) {
        return ld == null ? this : subtract(ld.x, ld.y, ld.z);
    }

    public LocationData subtract(double nx, double ny, double nz) {
        x -= nx;
        y -= ny;
        z -= nz;
        return this;
    }

    public LocationData subtract(double num) {
        x -= num;
        y -= num;
        z -= num;
        return this;
    }

    public String toLongString() {
        StringBuilder out = new StringBuilder();
        out.append(world).append(' ').append(x).append(' ').append(y).append(' ').append(z).append(' ').append(yaw).append(' ').append(pitch);
        return out.substring(1);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}

