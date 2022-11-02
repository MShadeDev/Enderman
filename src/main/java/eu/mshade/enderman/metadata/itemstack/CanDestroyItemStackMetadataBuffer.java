package eu.mshade.enderman.metadata.itemstack;

import eu.mshade.enderframe.item.ItemStack;
import eu.mshade.enderframe.item.ItemStackMetadataBuffer;
import eu.mshade.enderframe.item.MaterialKey;
import eu.mshade.enderframe.item.metadata.CanDestroyItemStackMetadata;
import eu.mshade.enderframe.metadata.MetadataKeyValueBucket;
import eu.mshade.enderframe.metadata.itemstack.ItemStackMetadataKey;
import eu.mshade.enderframe.mojang.NamespacedKey;
import eu.mshade.enderframe.wrapper.Wrapper;
import eu.mshade.mwork.binarytag.BinaryTag;
import eu.mshade.mwork.binarytag.BinaryTagType;
import eu.mshade.mwork.binarytag.StringBinaryTag;
import eu.mshade.mwork.binarytag.entity.CompoundBinaryTag;
import eu.mshade.mwork.binarytag.entity.ListBinaryTag;

import java.util.ArrayList;
import java.util.List;

public class CanDestroyItemStackMetadataBuffer implements ItemStackMetadataBuffer {

    private final Wrapper<MaterialKey, NamespacedKey> namespacedKeyWrapper;

    public CanDestroyItemStackMetadataBuffer(Wrapper<MaterialKey, NamespacedKey> namespacedKeyWrapper) {
        this.namespacedKeyWrapper = namespacedKeyWrapper;
    }

    @Override
    public void write(CompoundBinaryTag compoundBinaryTag, ItemStack itemStack) {
        MetadataKeyValueBucket metadataKeyValueBucket = itemStack.getMetadataKeyValueBucket();
        CanDestroyItemStackMetadata canDestroyItemStackMetadata = metadataKeyValueBucket.getMetadataKeyValue(ItemStackMetadataKey.CAN_DESTROY, CanDestroyItemStackMetadata.class);
        List<MaterialKey> materialKeys = canDestroyItemStackMetadata.getMetadataValue();
        ListBinaryTag listBinaryTag = new ListBinaryTag(BinaryTagType.STRING);
        materialKeys.forEach(materialKey -> {
            NamespacedKey namespacedKey = namespacedKeyWrapper.wrap(materialKey);
            if (namespacedKey != null) {
                listBinaryTag.add(new StringBinaryTag(namespacedKey.toString()));
            }
        });
        compoundBinaryTag.putBinaryTag("CanDestroy", listBinaryTag);
    }

    @Override
    public void read(CompoundBinaryTag compoundBinaryTag, ItemStack itemStack) {
        MetadataKeyValueBucket metadataKeyValueBucket = itemStack.getMetadataKeyValueBucket();
        if (!compoundBinaryTag.containsKey("CanDestroy")) return;
        ListBinaryTag canDestroy = (ListBinaryTag) compoundBinaryTag.getBinaryTag("CanDestroy");
        if (canDestroy.isEmpty()) return;
        CanDestroyItemStackMetadata canDestroyItemStackMetadata = new CanDestroyItemStackMetadata(new ArrayList<>());
        for (BinaryTag<?> binaryTag : canDestroy.getValue()) {
            NamespacedKey namespacedKey = NamespacedKey.fromString((String) binaryTag.getValue());
            canDestroyItemStackMetadata.getMetadataValue().add(namespacedKeyWrapper.reverse(namespacedKey));
        }
        metadataKeyValueBucket.setMetadataKeyValue(canDestroyItemStackMetadata);
    }
}
