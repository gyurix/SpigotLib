package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;

/**
 * Created by GyuriX on 2016.03.08..
 */
public class PacketPlayOutEntityStatus extends WrappedPacket {
    public int entityId;
    /**
     * 1 	Sent when resetting a mob spawn minecart's timer — appears to be unused by the client 2 	Living Entity hurt 3
     * Living Entity dead 4 	Iron Golem throwing up arms 6 	Wolf/Ocelot/Horse taming — Spawn “heart” particles 7
     * Wolf/Ocelot/Horse tamed — Spawn “smoke” particles 8 	Wolf shaking water — Trigger the shaking animation 9 	(of
     * self) Eating accepted by server 10 	Sheep eating grass or play TNT ignite sound 11 	Iron Golem handing over a
     * rose 12 	Villager mating — Spawn “heart” particles 13 	Spawn particles indicating that a villager is angry and
     * seeking revenge 14 	Spawn happy particles near a villager 15 	Witch animation — Spawn “magic” particles 16 	Play
     * zombie converting into a villager sound 17 	Firework exploding 18 	Animal in love (ready to mate) — Spawn “heart”
     * particles 19 	Reset squid rotation 20 	Spawn explosion particle — works for some living entities 21 	Play
     * guardian sound — works for every entity 22 	Enables reduced debug for players 23 	Disables reduced debug for
     * players 24–28 Sets op permission level 0–4 for players
     */
    public byte status;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.EntityStatus.newPacket(entityId, status);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] d = PacketOutType.EntityStatus.getPacketData(packet);
        entityId = (int) d[0];
        status = (byte) d[1];
    }
}
