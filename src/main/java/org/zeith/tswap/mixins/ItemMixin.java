package org.zeith.tswap.mixins;

import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import org.spongepowered.asm.mixin.*;
import org.zeith.tswap.api.iface.IToolClassesItem;

import java.util.Map;

@Mixin(Item.class)
@Implements({
		@Interface(iface = IToolClassesItem.class, prefix = "IToolItemAccessor$")
})
public class ItemMixin
{
	@Shadow(remap = false)
	@Final
	private Map<ToolType, Integer> toolClasses;

	public Map<ToolType, Integer> IToolItemAccessor$ToolSwapMod_getToolClasses()
	{
		return toolClasses;
	}
}