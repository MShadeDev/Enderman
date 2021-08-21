package eu.mshade.enderman.metadata.rewriters.buckets;

import eu.mshade.enderframe.metadata.MetadataMeaning;
import eu.mshade.enderman.metadata.rewriters.SizeMetadataRewriter;

public class SlimeMetadataRewriterBucket extends LivingEntityMetadataRewriterBucket {

    public SlimeMetadataRewriterBucket() {
        registerMetadataRewriter(MetadataMeaning.SLIME_SIZE, new SizeMetadataRewriter());
    }
}
