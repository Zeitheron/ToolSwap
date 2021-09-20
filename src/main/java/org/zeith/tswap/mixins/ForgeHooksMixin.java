package org.zeith.tswap.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.tswap.api.CustomToolTypes;

@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin
{
	@Shadow private static TriConsumer<Block, ToolType, Integer> blockToolSetter;

	@Inject(
			method = "initTools",
			at = @At("TAIL")
	)
	private static void initTSwapTools(CallbackInfo ci)
	{
		// Initializes shears type

		blockToolSetter.accept(Blocks.COBWEB, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.WHITE_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.ORANGE_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.MAGENTA_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.LIGHT_BLUE_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.YELLOW_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.LIME_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.PINK_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.GRAY_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.LIGHT_GRAY_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.CYAN_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.PURPLE_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.BLUE_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.BROWN_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.GREEN_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.RED_WOOL, CustomToolTypes.SHEARS, 0);
		blockToolSetter.accept(Blocks.BLACK_WOOL, CustomToolTypes.SHEARS, 0);
	}
}