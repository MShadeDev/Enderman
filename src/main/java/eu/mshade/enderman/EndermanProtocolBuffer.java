package eu.mshade.enderman;

import eu.mshade.enderframe.entity.Entity;
import eu.mshade.enderframe.item.*;
import eu.mshade.enderframe.metadata.*;
import eu.mshade.enderframe.metadata.entity.EntityMetadataBucket;
import eu.mshade.enderframe.protocol.ProtocolBuffer;
import eu.mshade.enderframe.wrapper.ContextWrapper;
import eu.mshade.enderframe.wrapper.Wrapper;
import eu.mshade.enderframe.wrapper.WrapperRepository;
import eu.mshade.enderman.metadata.EndermanEntityMetadataManager;
import eu.mshade.enderman.metadata.EndermanItemStackManager;
import eu.mshade.mwork.binarytag.entity.CompoundBinaryTag;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class EndermanProtocolBuffer extends ProtocolBuffer {

    private static Logger LOGGER = LoggerFactory.getLogger(EndermanProtocolBuffer.class);

    private final EndermanEntityMetadataManager entityMetadataManager;
    private final EndermanItemStackManager itemStackManager;
    private final Wrapper<MaterialKey, MaterialKey> materialKeyWrapper;

    public EndermanProtocolBuffer(EndermanEntityMetadataManager entityMetadataManager, EndermanItemStackManager itemStackManager, WrapperRepository wrapperRepository, ByteBuf byteBuf) {
        super(byteBuf);
        this.entityMetadataManager = entityMetadataManager;
        this.itemStackManager = itemStackManager;
        this.materialKeyWrapper = (Wrapper<MaterialKey, MaterialKey>) wrapperRepository.get(ContextWrapper.MATERIAL_KEY);
    }


    @Override
    public void writeItemStack(ItemStack itemStack) {
        MaterialKey materialKey;
        if (itemStack == null || (materialKey = materialKeyWrapper.wrap(itemStack.getMaterial())) == null) {
            writeShort(-1);
            return;
        }
        CompoundBinaryTag compoundBinaryTag = new CompoundBinaryTag();
        MetadataKeyValueBucket metadataKeyValueBucket = itemStack.getMetadataKeyValueBucket();
        for (MetadataKeyValue<?> metadataKeyValue : metadataKeyValueBucket.getMetadataKeyValues()) {
            if (itemStackManager.hasBuffer(metadataKeyValue.getMetadataKey())) {
                itemStackManager.getItemStackMetadataBuffer(metadataKeyValue.getMetadataKey()).write(compoundBinaryTag, itemStack);
            }
        }
        writeShort(materialKey.getId());
        writeByte(itemStack.getAmount() & 255);
        if (materialKey.inMaterialCategoryKey(MaterialCategory.ARMOR, MaterialCategory.TOOLS)) {
            writeShort(itemStack.getDurability());
        } else {
            writeShort(materialKey.getMetadata());
        }
        writeCompoundBinaryTag(compoundBinaryTag);

    }

    @Override
    public ItemStack readItemStack() {
        int id = readShort();
        if (id == -1) return new ItemStack(Material.AIR, 1, 0);

        byte count = readByte();
        int durability = readShort();
        MaterialKey parent = materialKeyWrapper.reverse(MaterialKey.from(id));
        MaterialKey materialKey;
        if (parent != null && parent.inMaterialCategoryKey(MaterialCategory.ARMOR, MaterialCategory.TOOLS)) {
            materialKey = parent;
        } else {
            materialKey = materialKeyWrapper.reverse(MaterialKey.from(id, durability));
        }
        ItemStack itemStack = new ItemStack(materialKey, count, durability);
        CompoundBinaryTag compoundBinaryTag = readCompoundBinaryTag();
        for (ItemStackMetadataBuffer itemStackMetadataBuffer : itemStackManager.getItemStackMetadataBuffers()) {
            itemStackMetadataBuffer.read(compoundBinaryTag, itemStack);
        }
        return itemStack;

    }

    @Override
    public void writeEntityMetadata(Entity entity, MetadataKey... entityMetadataKeys) {
        for (MetadataKey entityMetadataKey : entityMetadataKeys) {
            if (!entity.getMetadataKeyValueBucket().hasMetadataKeyValue(entityMetadataKey))
                continue;

            EntityMetadataBucket entityMetadataBucket = entityMetadataManager.getEntityMetadataBucket(entity);
            MetadataWrapper<Entity> entityMetadataWrapper = entityMetadataBucket.getEntityMetadataWrapper(entityMetadataKey);
            if (entityMetadataWrapper == null)
                continue;

            Metadata<?> metadata = entityMetadataWrapper.wrap(entity);
            int i = (entityMetadataManager.getMetadataIndex(metadata.getMetadataType())) << 5 | entityMetadataBucket.getIndexEntityMetadata(entityMetadataKey);
            this.writeByte(i);
            MetadataBuffer<Metadata<?>> metadataBuffer = (MetadataBuffer<Metadata<?>>) entityMetadataManager.getMetadataBuffer(metadata.getMetadataType());
            metadataBuffer.write(this, metadata);
        }
    }

    @Override
    public Collection<MetadataKey> getSupportedMetadataKeys(Entity entity) {
        EntityMetadataBucket entityMetadataBucket = entityMetadataManager.getEntityMetadataBucket(entity);
        return entityMetadataBucket.getEntityMetadataTypes();
    }
}
