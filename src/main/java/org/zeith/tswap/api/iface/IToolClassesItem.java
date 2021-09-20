package org.zeith.tswap.api.iface;

import net.minecraftforge.common.ToolType;

import java.util.Map;

public interface IToolClassesItem
{
	Map<ToolType, Integer> ToolSwapMod_getToolClasses();
}