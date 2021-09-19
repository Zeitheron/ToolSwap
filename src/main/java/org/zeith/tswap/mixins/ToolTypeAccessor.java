package org.zeith.tswap.mixins;

import net.minecraftforge.common.ToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = ToolType.class, remap = false)
public interface ToolTypeAccessor
{
	@Accessor("VALUES")
	static Map<String, ToolType> getValues()
	{
		return new HashMap<>();
	}
}