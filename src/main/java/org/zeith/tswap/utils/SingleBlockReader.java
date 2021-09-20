package org.zeith.tswap.utils;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SingleBlockReader
	implements IBlockReader
{
	final BlockState state;

	public SingleBlockReader(BlockState state)
	{
		this.state = state;
	}

	@Override
	public BlockState getBlockState(BlockPos p_180495_1_)
	{
		return state;
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos p_175625_1_)
	{
		return null;
	}

	@Override
	public FluidState getFluidState(BlockPos p_204610_1_)
	{
		return Fluids.EMPTY.defaultFluidState();
	}
}