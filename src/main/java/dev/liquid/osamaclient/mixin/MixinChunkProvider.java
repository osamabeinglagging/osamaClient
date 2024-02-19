package dev.liquid.osamaclient.mixin;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkProviderClient.class)
public interface MixinChunkProvider {
    @Accessor
    List<Chunk> getChunkListing();
}