package eu.mshade.enderman.packet.play;

import eu.mshade.enderframe.protocol.ByteMessage;
import eu.mshade.enderframe.protocol.PacketOut;

public class PacketOutEntityHeadLook extends PacketOut {

    private final int id;
    private final int rotation;

    public PacketOutEntityHeadLook(int id, int rotation) {
        this.id = id;
        this.rotation = rotation;
    }

    @Override
    public void serialize(ByteMessage byteMessage) {
        byteMessage.writeVarInt(id);
        byteMessage.writeByte(rotation);
    }
}
