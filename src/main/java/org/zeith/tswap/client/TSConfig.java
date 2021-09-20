package org.zeith.tswap.client;

import com.mojang.brigadier.StringReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import org.zeith.hammerlib.util.cfg.ConfigFile;
import org.zeith.hammerlib.util.cfg.entries.ConfigEntryCategory;
import org.zeith.tswap.TSwap;
import org.zeith.tswap.mixins.ToolTypeAccessor;
import org.zeith.tswap.utils.SingleBlockReader;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class TSConfig
{
	public final File cfgFile;
	public final ConfigFile cfg;

	private final Function<BlockState, Set<String>> toolStatesComp = tool -> new HashSet<>();
	private final Map<BlockState, Set<String>> toolStates = new HashMap<>();
	private final Map<BlockState, Set<String>> toolStatesView = Collections.unmodifiableMap(toolStates);

	private final List<EntityMatcher> blacklistedEntities = new ArrayList<>();

	private int swapDelay, swordSwapDelay;

	public TSConfig(File cfgFile)
	{
		this.cfgFile = cfgFile;
		this.cfg = new ConfigFile(cfgFile);
	}

	public boolean isEntityBlacklisted(Entity entity)
	{
		return !(entity instanceof LivingEntity) || blacklistedEntities.stream().anyMatch(e -> e.test(entity));
	}

	public Map<BlockState, Set<String>> getToolStates()
	{
		return toolStatesView;
	}

	public int getSwapDelay()
	{
		return swapDelay;
	}

	public int getSwordSwapDelay()
	{
		return swordSwapDelay;
	}

	public void reload()
	{
		TSwap.LOG.info("Reloading configs...");

		cfg.load();

		ConfigEntryCategory swapping = cfg.getCategory("Swapping");

		swapDelay = swapping.getIntEntry("SwapDelay", 7, 3, 40).setDescription("How many ticks will pass after you're done mining till the slot selected reverts back to original (before mining)").getValue();
		swordSwapDelay = swapping.getIntEntry("SwordSwapDelay", 20, 3, 80).setDescription("How many ticks will pass after you're done looking at entity till the slot selected reverts back to original (before looking at the entity)").getValue();

		blacklistedEntities.clear();
		for(String e : swapping.getStringArrayEntry("AttackEntityBlacklist", "minecraft:player")
				.setDescription("List of all blacklisted entities from being targeted. Optionally, you can use '?' to match any character or '*' to match any sequence of characters.\nExample: \"minecraft:s*\" will target sheep & salmon, while \"minecraft:s*p\" will target sheep, but not salmon.")
				.getValue())
			blacklistedEntities.add(new EntityMatcher(e));

		ConfigEntryCategory tools = cfg.getCategory("Tools")
				.setDescription("List of all registered tools");

		toolStates.clear();

		for(Block block : ForgeRegistries.BLOCKS.getValues())
			for(BlockState state : block.getStateDefinition().getPossibleStates())
			{
				ToolType tt = state.getHarvestTool();
				float breakSpeed = state.getDestroySpeed(new SingleBlockReader(state), BlockPos.ZERO);
				if(breakSpeed >= 0 && tt == null) tt = ToolType.PICKAXE;
				if(tt != null)
					toolStates.computeIfAbsent(state, toolStatesComp).add(tt.getName());
			}

		BlockStateArgument blkState = BlockStateArgument.block();

		for(Map.Entry<String, ToolType> tool : ToolTypeAccessor.getValues().entrySet())
		{
			String[] entry = tools
					.getStringArrayEntry(tool.getKey())
					.setDescription("List of extra blocks that should be harvested by " + tool)
					.getValue();

			for(String e : entry)
				if(!e.trim().isEmpty())
				{
					try
					{
						Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e));
						if(block != Blocks.AIR && block != null)
							for(BlockState state : block.getStateDefinition().getPossibleStates())
								toolStates.computeIfAbsent(state, toolStatesComp).add(tool.getValue().getName());
						else
							throw new RuntimeException();
					} catch(Throwable e2)
					{
						try
						{
							toolStates.computeIfAbsent(blkState.parse(new StringReader(e)).getState(), toolStatesComp).add(tool.getValue().getName());
						} catch(Throwable ex)
						{
							TSwap.LOG.warn("Failed to decode " + e + " into something meaningful.");
						}
					}
				}
		}

		TSwap.LOG.info("Indexed " + toolStates.size() + " harvestable block states.");

		TSwap.LOG.info("Saving configs...");

		if(cfg.hasChanged()) cfg.save();

		TSwap.LOG.info("Configs updated.");
	}

	public ToolType getEffectiveTool(ItemStack stack, BlockState state)
	{
		Set<ToolType> tools = stack.getItem().getToolTypes(stack);
		return tools.stream().filter(tt -> toolStates.getOrDefault(state, Collections.emptySet()).contains(tt.getName())).findFirst().orElse(null);
	}
}