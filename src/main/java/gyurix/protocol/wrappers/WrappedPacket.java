package gyurix.protocol.wrappers;

import gyurix.protocol.utils.WrappedData;

/**
 * Represents a wrapped (user friendly) form of a Vanilla/NMS packet.
 */
public abstract class WrappedPacket implements WrappedData {
    /**
     * Converts this wrapped packet to a Vanilla/NMS packet
     *
     * @return The conversion result, NMS packet
     */
    public abstract Object getVanillaPacket();

    /**
     * Loads a Vanilla/NMS packet to this wrapper
     *
     * @param packet - The loadable packet
     */
    public abstract void loadVanillaPacket(Object packet);

    @Override
    public Object toNMS() {
        return getVanillaPacket();
    }
}

