package eu.mshade.enderman.metadata.rewriters;

import eu.mshade.enderframe.entity.Entity;
import eu.mshade.enderframe.entity.Horse;
import eu.mshade.enderframe.metadata.MetadataType;
import eu.mshade.enderframe.metadata.type.IntegerMetadata;
import eu.mshade.enderframe.metadata.MetadataManager;
import eu.mshade.enderframe.metadata.MetadataRewriter;
import eu.mshade.enderframe.protocol.ByteMessage;

public class HorseStyleMetadataWriter extends MetadataRewriter {

    @Override
    public void write(MetadataManager metadataManager, ByteMessage byteMessage, Entity entity) {
        metadataManager.getMetadataTypeBuffer(MetadataType.INTEGER).write(byteMessage, new IntegerMetadata(((Horse)entity).getHorseStyle().ordinal()));
    }

    @Override
    public MetadataType getMetadataType() {
        return MetadataType.INTEGER;
    }
}
