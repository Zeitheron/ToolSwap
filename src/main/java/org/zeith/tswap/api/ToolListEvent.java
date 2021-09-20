package org.zeith.tswap.api;

import net.minecraft.block.BlockState;
import net.minecraftforge.common.ToolType;

import java.util.function.BiConsumer;

public class ToolListEvent
{
	final BiConsumer<BlockState, ToolType> register;

	public ToolListEvent(BiConsumer<BlockState, ToolType> register)
	{
		this.register = register;
	}

	public void register(BlockState state, ToolType tool)
	{
		register.accept(state, tool);
	}
}