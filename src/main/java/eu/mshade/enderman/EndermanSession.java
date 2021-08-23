package eu.mshade.enderman;

import eu.mshade.enderframe.EnderFrameSession;
import eu.mshade.enderframe.EnderFrameSessionHandler;
import eu.mshade.enderframe.GameMode;
import eu.mshade.enderframe.PlayerInfoBuilder;
import eu.mshade.enderframe.entity.Entity;
import eu.mshade.enderframe.entity.EntityRepository;
import eu.mshade.enderframe.entity.Player;
import eu.mshade.enderframe.event.entity.PacketMoveType;
import eu.mshade.enderframe.metadata.MetadataMeaning;
import eu.mshade.enderframe.mojang.GameProfile;
import eu.mshade.enderframe.mojang.chat.TextComponent;
import eu.mshade.enderframe.mojang.chat.TextPosition;
import eu.mshade.enderframe.protocol.ProtocolStatus;
import eu.mshade.enderframe.protocol.ProtocolVersion;
import eu.mshade.enderframe.protocol.packet.PacketOutDisconnect;
import eu.mshade.enderframe.protocol.packet.PacketOutPlayerInfo;
import eu.mshade.enderframe.protocol.packet.PacketOutSetCompression;
import eu.mshade.enderframe.world.*;
import eu.mshade.enderman.packet.login.PacketOutEncryption;
import eu.mshade.enderman.packet.login.PacketOutLoginSuccess;
import eu.mshade.enderman.packet.play.*;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EndermanSession implements EnderFrameSession {

    private static final Random random = new Random();
    private final String sessionId;
    private final byte[] verifyToken = new byte[4];
    private EnderFrameSessionHandler enderFrameSessionHandler;
    private ProtocolVersion protocolVersion = ProtocolVersion.UNKNOWN;
    private GameProfile gameProfile;
    private SocketAddress socketAddress;
    private Location location;
    private Queue<ChunkBuffer> observeChunks = new ConcurrentLinkedQueue<>();
    private Player player;

    public EndermanSession(EnderFrameSessionHandler enderFrameSessionHandler) {
        this.sessionId = Long.toString(random.nextLong(), 16).trim();
        this.enderFrameSessionHandler = enderFrameSessionHandler;
        this.socketAddress = enderFrameSessionHandler.getChannel().remoteAddress();
        random.nextBytes(verifyToken);
    }


    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void setPlayer(Player player) {
        if (this.player != null || player != null)
            this.player = player;
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    @Override
    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }


    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public byte[] getVerifyToken() {
        return verifyToken;
    }

    @Override
    public Queue<ChunkBuffer> getChunkBuffers() {
        return observeChunks;
    }

    @Override
    public void sendKeepAlive(int threshold) {
        enderFrameSessionHandler.sendPacket(new PacketOutKeepAlive(threshold));
    }

    @Override
    public void sendPlayerInfo(PlayerInfoBuilder playerInfoBuilder) {
        enderFrameSessionHandler.sendPacket(new PacketOutPlayerInfo(playerInfoBuilder));
    }

    @Override
    public void sendEncryption(PublicKey publicKey) {
        enderFrameSessionHandler.sendPacket(new PacketOutEncryption(sessionId, publicKey, verifyToken));
    }

    @Override
    public void sendCompression(int threshold) {
        enderFrameSessionHandler.sendPacket(new PacketOutSetCompression(threshold));
        enderFrameSessionHandler.enableCompression(threshold);
    }

    @Override
    public void sendLoginSuccess() {
        enderFrameSessionHandler.sendPacket(new PacketOutLoginSuccess(this.getGameProfile()));
        enderFrameSessionHandler.toggleProtocolStatus(ProtocolStatus.PLAY);
    }

    @Override
    public void sendJoinGame(GameMode gameMode, Dimension dimension, Difficulty difficulty, int maxPlayers, LevelType levelType, boolean reducedDebugInfo) {
        player.setGameMode(gameMode);
        enderFrameSessionHandler.sendPacket(new PacketOutJoinGame(gameMode, dimension, difficulty, maxPlayers, levelType.getName(), reducedDebugInfo));
    }

    @Override
    public void sendPosition(double x, double y, double z) {
        this.sendPosition(x, y, z, 0, 0);
    }

    @Override
    public void sendPosition(double x, double y, double z, float yaw, float pitch) {
        enderFrameSessionHandler.sendPacket(new PacketOutPlayerPositionAndLook(x, y, z, yaw, pitch));
    }

    @Override
    public void sendMessage(TextComponent textComponent, TextPosition position) {
        enderFrameSessionHandler.sendPacket(new PacketOutChatMessage(textComponent, position));
    }

    @Override
    public void sendDisconnect(String message) {
        enderFrameSessionHandler.sendPacket(new PacketOutDisconnect(TextComponent.of(message)));
    }

    @Override
    public void sendChunk(ChunkBuffer chunkBuffer) {
        chunkBuffer.getViewers().add(player);
        observeChunks.add(chunkBuffer);
        List<SectionBuffer> sectionBuffers = new ArrayList<>();
        for (SectionBuffer sectionBuffer : chunkBuffer.getSectionBuffers()) {
            if (sectionBuffer != null && sectionBuffer.getRealBlock() != 0) {
                sectionBuffers.add(sectionBuffer);
            }
        }
        int capacity = sectionBuffers.size() * (4096 * 4) + 256;

        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        for (SectionBuffer sectionBuffer : sectionBuffers) {
            for (int i = 0; i < 4096; i++) {
                byteBuffer.put((byte) (sectionBuffer.getBlocks()[i] << 4 | sectionBuffer.getData().get(i)));
                byteBuffer.put((byte) (sectionBuffer.getBlocks()[i] >> 4));
            }
        }

        for (SectionBuffer sectionBuffer : sectionBuffers) {
            byteBuffer.put(sectionBuffer.getBlockLight().getRawData());
        }

        for (SectionBuffer sectionBuffer : sectionBuffers) {
            byteBuffer.put(sectionBuffer.getSkyLight().getRawData());
        }

        byteBuffer.put(chunkBuffer.getBiomes());

        enderFrameSessionHandler.sendPacket(new PacketOutChunkData(chunkBuffer.getX(), chunkBuffer.getZ(), true, chunkBuffer.getBitMask(), byteBuffer.array()));
    }

    @Override
    public void sendUnloadChunk(ChunkBuffer chunkBuffer) {
        chunkBuffer.getViewers().remove(player);

        observeChunks.remove(chunkBuffer);
        enderFrameSessionHandler.sendPacket(new PacketOutChunkData(chunkBuffer.getX(), chunkBuffer.getZ(), true, 0, new byte[0]));
    }

    @Override
    public void sendMetadata(Entity entity, MetadataMeaning... metadataMeanings) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityMetadata(entity, metadataMeanings));
    }

    @Override
    public <T extends Entity> void sendEntity(T entity) {
        if(entity instanceof Player) {
            enderFrameSessionHandler.sendPacket(new PacketOutSpawnPlayer((Player)entity));
        } else {
            EntityRepository repository = enderFrameSessionHandler.getEnderFrameProtocol().getEntityRepository();
            enderFrameSessionHandler.sendPacket(new PacketOutSpawnMob(repository.getIdByEntityType(entity.getType()), entity));
        }
    }

    @Override
    public void moveTo(Entity entity, PacketMoveType packetMoveType, boolean ground) {
        Location now = entity.getLocation();
        Location before = entity.getBeforeLocation();

        boolean teleport = hasOverflow(floor(now.getX() * 32) - floor(before.getX() * 32)) || hasOverflow(floor(now.getY() * 32) - floor(before.getY() * 32)) || hasOverflow(floor(now.getZ() * 32) - floor(before.getZ() * 32));

        if (packetMoveType == PacketMoveType.LOOK || packetMoveType == PacketMoveType.POSITION_AND_LOOK) {
            this.sendLook(entity, ground);
            this.sendHeadLook(entity);
        }
        if (!teleport) {
            if (packetMoveType == PacketMoveType.POSITION_AND_LOOK) {
                this.sendMoveAndLook(entity, ground);
            } else if (packetMoveType.equals(PacketMoveType.POSITION)) {
                this.sendMove(entity, ground);
            }
        } else {
            this.sendTeleport(entity, ground);
        }
    }

    private boolean hasOverflow(int value) {
        return value > 3 || value < -3;
    }

    private int floor(double d0) {
        int i = (int) d0;

        return d0 < (double) i ? i - 1 : i;
    }

    @Override
    public void sendTeleport(Entity entity, boolean onGround) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityTeleport(entity, onGround));
    }

    @Override
    public void sendMove(Entity entity, boolean onGround) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityRelativeMove(entity.getEntityId(), entity.getLocation(), entity.getBeforeLocation(), onGround));
    }

    @Override
    public void sendMoveAndLook(Entity entity, boolean onGround) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityLookRelativeMove(entity.getEntityId(), entity.getLocation(), entity.getBeforeLocation(), onGround));
    }

    @Override
    public void sendLook(Entity entity, boolean onGround) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityLook(entity.getEntityId(), entity.getLocation().getYaw(), entity.getLocation().getPitch(), onGround));
    }

    @Override
    public void sendHeadLook(Entity entity) {
        enderFrameSessionHandler.sendPacket(new PacketOutEntityHeadLook(entity.getEntityId(), entity.getLocation().getYaw()));
    }

    @Override
    public void removeEntities(Entity... entities) {
        enderFrameSessionHandler.sendPacket(new PacketOutDestroyEntities(entities));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndermanSession that = (EndermanSession) o;
        return Objects.equals(enderFrameSessionHandler, that.enderFrameSessionHandler) && protocolVersion == that.protocolVersion && Objects.equals(socketAddress, that.socketAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enderFrameSessionHandler, protocolVersion, socketAddress);
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("FLUSH");
    }
}
