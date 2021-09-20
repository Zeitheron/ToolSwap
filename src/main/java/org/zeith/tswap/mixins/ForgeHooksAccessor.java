package org.zeith.tswap.mixins;

import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.zeith.tswap.TSwap;

@Mixin(value = ForgeHooks.class, remap = false)
public interface ForgeHooksAccessor
{
	@Accessor("blockToolSetter")
	static TriConsumer<Block, ToolType, Integer> getBlockToolSetter()
	{
		return (a, b, c) ->
		{
			TSwap.LOG.warn("Failed to apply block tool setter mixin!");
		};
	}
}