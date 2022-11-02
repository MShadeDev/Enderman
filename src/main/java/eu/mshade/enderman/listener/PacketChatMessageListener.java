package eu.mshade.enderman.listener;

import eu.mshade.enderframe.EnderFrame;
import eu.mshade.enderframe.packetevent.PacketChatMessageEvent;
import eu.mshade.enderman.packet.play.MinecraftPacketInChatMessage;
import eu.mshade.mwork.event.EventListener;

public class PacketChatMessageListener implements EventListener<MinecraftPacketInChatMessage> {

    @Override
    public void onEvent(MinecraftPacketInChatMessage event) {
        EnderFrame.get().getPacketEventBus().publishAsync(new PacketChatMessageEvent(event.getSessionWrapper().getPlayer(), event.getMessage()));
    }

}
