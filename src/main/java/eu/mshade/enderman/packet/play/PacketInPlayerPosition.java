package eu.mshade.enderman.packet.play;

import eu.mshade.enderframe.protocol.ProtocolBuffer;

public class PacketInPlayerPosition extends PacketInPlayerGround {

    private double x, y, z;

    @Override
    public void deserialize(ProtocolBuffer protocolBuffer) {
        this.x = protocolBuffer.readDouble();
        this.y = protocolBuffer.readDouble();
        this.z = protocolBuffer.readDouble();
        super.deserialize(protocolBuffer);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }


    @Override
    public String toString() {
        return "PacketInPlayerPosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", onGround="+ this.isOnGround() +
                '}';
    }
}
