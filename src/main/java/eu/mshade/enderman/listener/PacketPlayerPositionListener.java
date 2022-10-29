package eu.mshade.enderman.listener;

import eu.mshade.enderframe.EnderFrame;
import eu.mshade.enderframe.packetevent.DefaultPacketMoveEvent;
import eu.mshade.enderman.packet.play.move.PacketInPlayerPosition;
import eu.mshade.mwork.ParameterContainer;
import eu.mshade.mwork.event.EventListener;

public class PacketPlayerPositionListener implements EventListener<PacketInPlayerPosition> {

    @Override
    public void onEvent(PacketInPlayerPosition event) {
        EnderFrame.get().getPacketEventBus().publishAsync(new DefaultPacketMoveEvent(event.getSessionWrapper().getPlayer(), event.getX(), event.getY(), event.getZ(), event.isOnGround()));
    }

}
