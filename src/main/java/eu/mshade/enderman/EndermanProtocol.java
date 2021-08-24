package eu.mshade.enderman;

import eu.mshade.enderframe.EnderFrameProtocol;
import eu.mshade.enderframe.EnderFrameSession;
import eu.mshade.enderframe.EnderFrameSessionHandler;
import eu.mshade.enderframe.entity.EntityType;
import eu.mshade.enderframe.protocol.ByteMessage;
import eu.mshade.enderframe.protocol.ProtocolStatus;
import eu.mshade.enderframe.protocol.ProtocolVersion;
import eu.mshade.enderframe.protocol.packet.*;
import eu.mshade.enderman.listener.*;
import eu.mshade.enderman.packet.login.PacketInEncryption;
import eu.mshade.enderman.packet.login.PacketInLogin;
import eu.mshade.enderman.packet.login.PacketOutEncryption;
import eu.mshade.enderman.packet.login.PacketOutLoginSuccess;
import eu.mshade.enderman.packet.play.*;
import io.netty.buffer.ByteBuf;

public class EndermanProtocol extends EnderFrameProtocol {


    public EndermanProtocol() {
        this.getEventBus().subscribe(PacketInKeepAlive.class, new PacketKeepAliveListener());
        this.getEventBus().subscribe(PacketInLogin.class, new PacketLoginListener());
        this.getEventBus().subscribe(PacketInEncryption.class, new PacketEncryptionListener());
        this.getEventBus().subscribe(PacketInClientSettings.class, new PacketClientSettingsListener());
        this.getEventBus().subscribe(PacketInPlayerPosition.class, new PacketPlayerPositionListener());
        this.getEventBus().subscribe(PacketInPlayerGround.class, new PacketPlayerGroundListener());
        this.getEventBus().subscribe(PacketInPlayerLook.class, new PacketPlayerLookListener());
        this.getEventBus().subscribe(PacketInPlayerPositionAndLook.class, new PacketPlayerPositionAndLookListener());
        this.getEventBus().subscribe(PacketInChatMessage.class, new PacketChatMessageListener());
        this.getEventBus().subscribe(PacketInEntityAction.class, new PacketEntityActionListener());

        this.getProtocolRegistry().registerOut(ProtocolStatus.LOGIN, 0x00, PacketOutDisconnect.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.LOGIN, 0x01, PacketOutEncryption.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.LOGIN, 0x02, PacketOutLoginSuccess.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.LOGIN, 0x03, PacketOutSetCompression.class);

        this.getProtocolRegistry().registerIn(ProtocolStatus.LOGIN, 0x00, PacketInLogin.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.LOGIN, 0x01, PacketInEncryption.class);

        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x00, PacketInKeepAlive.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x01, PacketInChatMessage.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x03, PacketInPlayerGround.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x04, PacketInPlayerPosition.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x05, PacketInPlayerLook.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x06, PacketInPlayerPositionAndLook.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x08, PacketInPlayerBlockPlacement.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x0B, PacketInEntityAction.class);
        this.getProtocolRegistry().registerIn(ProtocolStatus.PLAY, 0x15, PacketInClientSettings.class);

        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x00, PacketOutKeepAlive.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x01, PacketOutJoinGame.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x02, PacketOutChatMessage.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x05, PacketOutSpawnPosition.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x07, PacketOutRespawn.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x08, PacketOutPlayerPositionAndLook.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x0C, PacketOutSpawnPlayer.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x0F,  PacketOutSpawnMob.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x12, PacketOutEntityVelocity.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x13,  PacketOutDestroyEntities.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x14,  PacketOutSpawnEntity.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x15,  PacketOutEntityRelativeMove.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x16,  PacketOutEntityLook.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x17,  PacketOutEntityLookRelativeMove.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x18,  PacketOutEntityTeleport.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x19, PacketOutEntityHeadLook.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x1C,  PacketOutEntityMetadata.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY,0x20,  PacketOutEntityProperties.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x21, PacketOutChunkData.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x38, PacketOutPlayerInfo.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x39, PacketOutPlayerAbilities.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x40, PacketOutDisconnect.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x46, PacketOutSetCompression.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x47, PacketOutPlayerList.class);
        this.getProtocolRegistry().registerOut(ProtocolStatus.PLAY, 0x2F, PacketOutSetSlot.class);

        this.getEntityRepository().registerEntityTypeId(50, EntityType.CREEPER);
        this.getEntityRepository().registerEntityTypeId(51, EntityType.SKELETON);
        this.getEntityRepository().registerEntityTypeId(52, EntityType.SPIDER);
        this.getEntityRepository().registerEntityTypeId(53, EntityType.GIANT);
        this.getEntityRepository().registerEntityTypeId(54, EntityType.ZOMBIE);
        this.getEntityRepository().registerEntityTypeId(55, EntityType.SLIME);
        this.getEntityRepository().registerEntityTypeId(56, EntityType.GHAST);
        this.getEntityRepository().registerEntityTypeId(57, EntityType.PIG_ZOMBIE);
        this.getEntityRepository().registerEntityTypeId(58, EntityType.ENDERMAN);
        this.getEntityRepository().registerEntityTypeId(59, EntityType.CAVE_SPIDER);
        this.getEntityRepository().registerEntityTypeId(60, EntityType.SILVERFISH);
        this.getEntityRepository().registerEntityTypeId(61, EntityType.BLAZE);
        this.getEntityRepository().registerEntityTypeId(62, EntityType.MAGMA_CUBE);
        this.getEntityRepository().registerEntityTypeId(63, EntityType.ENDER_DRAGON);
        this.getEntityRepository().registerEntityTypeId(64, EntityType.WITHER);
        this.getEntityRepository().registerEntityTypeId(65, EntityType.BAT);
        this.getEntityRepository().registerEntityTypeId(66, EntityType.WITCH);
        this.getEntityRepository().registerEntityTypeId(90, EntityType.PIG);
        this.getEntityRepository().registerEntityTypeId(91, EntityType.SHEEP);
        this.getEntityRepository().registerEntityTypeId(92, EntityType.COW);
        this.getEntityRepository().registerEntityTypeId(93, EntityType.CHICKEN);
        this.getEntityRepository().registerEntityTypeId(94, EntityType.SQUID);
        this.getEntityRepository().registerEntityTypeId(95, EntityType.WOLF);
        this.getEntityRepository().registerEntityTypeId(96, EntityType.MOOSHROOM);
        this.getEntityRepository().registerEntityTypeId(97, EntityType.SNOW_GOLEM);
        this.getEntityRepository().registerEntityTypeId(98, EntityType.OCELOT);
        this.getEntityRepository().registerEntityTypeId(99, EntityType.IRON_GOLEM);
        this.getEntityRepository().registerEntityTypeId(100, EntityType.HORSE);
        this.getEntityRepository().registerEntityTypeId(120, EntityType.VILLAGER);
    }

    @Override
    public ByteMessage getByteMessage(ByteBuf byteBuf) {
        return new EndermanByteMessage(byteBuf);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return ProtocolVersion.V1_8;
    }

    @Override
    public EnderFrameSession getEnderFrameSession(EnderFrameSessionHandler enderFrameSessionHandler) {
        return new EndermanSession(enderFrameSessionHandler);
    }
}
