package dev.koifih.client.mixin;

import dev.koifih.client.render.blocks.BlockIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void adin$blockChanged(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> info) {
        BlockState previous = info.getReturnValue();
        if (previous != null) BlockIndex.onBlockChanged((LevelChunk) (Object) this, pos, previous, state);
    }
}
