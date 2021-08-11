package eu.mshade.enderman.metadata.rewriters;

import eu.mshade.enderframe.entity.ArmorStand;
import eu.mshade.enderframe.entity.Entity;
import eu.mshade.enderframe.metadata.MetadataType;
import eu.mshade.enderframe.metadata.type.RotationMetadata;
import eu.mshade.enderframe.metadata.MetadataManager;
import eu.mshade.enderframe.metadata.MetadataRewriter;
import eu.mshade.enderframe.protocol.ByteMessage;

public class HeadRotationMetadataRewriter extends MetadataRewriter {

    @Override
    public void write(MetadataManager metadataManager, ByteMessage byteMessage, Entity entity) {
        metadataManager.getMetadataTypeBuffer(MetadataType.ROTATION).write(byteMessage, new RotationMetadata(((ArmorStand)entity).getHeadPosition()));
    }

    @Override
    public MetadataType getMetadataType() {
        return MetadataType.ROTATION;
    }
}
