package gyurix.protocol.event;

import com.google.common.collect.Lists;
import gyurix.protocol.Reflection;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public enum PacketInType {
    HandshakingInSetProtocol,
    LoginInEncryptionBegin,
    LoginInStart,
    Abilities,
    Advancements,
    AdvancementTab,
    ArmAnimation,
    AutoRecipe,
    BlockDig,
    BlockPlace,
    BoatMove,
    Chat,
    ClientCommand,
    CloseWindow,
    CustomPayload,
    EnchantItem,
    EntityAction,
    Flying,
    HeldItemSlot,
    KeepAlive,
    RecipeDisplayed,
    ResourcePackStatus,
    SetCreativeSlot,
    Settings,
    Spectate,
    SteerVehicle,
    TabComplete,
    TeleportAccept,
    Transaction,
    UpdateSign,
    UseEntity,
    UseItem,
    VehicleMove,
    WindowClick,
    StatusInPing,
    StatusInStart;

    private static final HashMap<Class, PacketInType> packets = new HashMap<>();
    public Class<? extends WrappedPacket> wrapper;
    ArrayList<Field> fs = new ArrayList<>();
    private Constructor emptyConst;
    private boolean supported;

    PacketInType() {

    }


    /**
     * Get the type of an incoming packet
     *
     * @param packet - The incoming packet
     * @return The type of the given packet
     */
    public static PacketInType getType(Object packet) {
        Class cl = packet.getClass();
        while (cl != null && cl != Object.class) {
            String cn = cl.getName();
            while (cn.contains("$")) {
                try {
                    cl = Class.forName(cn.substring(0, cn.indexOf("$")));
                    cn = cl.getName();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            PacketInType type = packets.get(cl);
            if (type != null)
                return type;
            cl = cl.getSuperclass();
        }
        return null;
    }

    /**
     * Initializes the PacketInType, DO NOT USE THIS METHOD
     */
    public static void init() {
        for (PacketInType t : PacketInType.values()) {
            String name = t.name();
            String cln = "Packet" + (name.startsWith("Login") || name.startsWith("Status") || name.startsWith("Handshaking") ? name : "PlayIn" + name);
            try {
                Class cl = Reflection.getNMSClass(cln);
                if (cl == null)
                    continue;
                packets.put(cl, t);
                t.emptyConst = cl.getConstructor();
                t.fs = new ArrayList();
                for (Field f : cl.getDeclaredFields()) {
                    if ((f.getModifiers() & 8) != 0) continue;
                    Reflection.setFieldAccessible(f);
                    t.fs.add(f);
                }
                t.supported = true;
            } catch (Throwable ignored) {
            }
            try {
                t.wrapper = (Class<? extends WrappedPacket>) Class.forName("gyurix.protocol.wrappers.inpackets." + cln);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Fills the given packet with the given data
     *
     * @param packet - The fillable packet
     * @param data   - The filling data
     */
    public void fillPacket(Object packet, Object... data) {
        ArrayList<Field> fields = Lists.newArrayList(fs);
        for (Object d : data) {
            for (int f = 0; f < fields.size(); f++) {
                try {
                    Field ff = fields.get(f);
                    ff.set(packet, d);
                    fields.remove(f--);
                    break;
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * Returns the packet data of a packet
     *
     * @param packet - The packet
     * @return The contents of all the non static fields of the packet
     */
    public Object[] getPacketData(Object packet) {
        Object[] out = new Object[fs.size()];
        try {
            for (int i = 0; i < fs.size(); ++i) {
                out[i] = fs.get(i).get(packet);
            }
            return out;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }

    /**
     * Tells if this packet is supported or not by the current server version.
     *
     * @return True if it is supported, false otherwise
     */
    public boolean isSupported() {
        return supported;
    }

    /**
     * Creates a new packet of this type and fills its fields with the given data
     *
     * @param data - Data to fill packet fields with
     * @return The crafted packet
     */
    public Object newPacket(Object... data) {
        try {
            Object out = emptyConst.newInstance();
            fillPacket(out, data);
            return out;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    /**
     * Get the wrapper of the given NMS packet.
     *
     * @param nmsPacket - The NMS packet
     * @return The wrapper of the given NMS packet
     */
    public WrappedPacket wrap(Object nmsPacket) {
        try {
            WrappedPacket wp = wrapper.newInstance();
            wp.loadVanillaPacket(nmsPacket);
            return wp;
        } catch (Throwable e) {
            SU.log(Main.pl, "§4[§cPacketAPI§4] §eError on wrapping §c" + name() + "§e out packet.");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}

