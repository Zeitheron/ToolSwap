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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import org.zeith.hammerlib.util.cfg.ConfigFile;
import org.zeith.hammerlib.util.cfg.entries.ConfigEntryCategory;
import org.zeith.tswap.TSwap;
import org.zeith.tswap.mixins.ToolTypeAccessor;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class TSConfig
{
	public final File cfgFile;
	public final ConfigFile cfg;

	private final Function<String, Set<BlockState>> toolStatesComp = tool -> new HashSet<>();
	private final Function<BlockState, Set<String>> toolStatesCompF = tool -> new HashSet<>();

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

	public void transpose(Map<String, Set<BlockState>> raw)
	{
		long start = System.currentTimeMillis();
		toolStates.clear();
		for(String tool : raw.keySet())
			for(BlockState state : raw.get(tool))
				if(!toolStates.computeIfAbsent(state, toolStatesCompF).add(tool))
					TSwap.LOG.info("Duplicate state " + state + " for tool " + tool);
		TSwap.LOG.info("State transposing complete in " + (System.currentTimeMillis() - start) + " ms. Cached " + toolStates.size() + " block states.");
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

		Map<String, Set<BlockState>> toolStates = new HashMap<>();

		for(Block block : ForgeRegistries.BLOCKS.getValues())
			for(BlockState state : block.getStateDefinition().getPossibleStates())
			{
				ToolType tt = state.getHarvestTool();
				if(tt != null)
					toolStates.computeIfAbsent(tt.getName(), toolStatesComp).add(state);
			}

		BlockStateArgument blkState = BlockStateArgument.block();

		for(Map.Entry<String, ToolType> tool : ToolTypeAccessor.getValues().entrySet())
		{
			String[] entry = tools
					.getStringArrayEntry(tool.getKey())
					.setDescription("List of extra blocks that should be harvested by " + tool)
					.getValue();

			for(String e : entry)
			{
				try
				{
					Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e));
					if(block != Blocks.AIR && block != null)
						for(BlockState state : block.getStateDefinition().getPossibleStates())
							toolStates.computeIfAbsent(tool.getKey(), toolStatesComp).add(state);
					else
						throw new RuntimeException();
				} catch(Throwable e2)
				{
					try
					{
						toolStates.computeIfAbsent(tool.getKey(), toolStatesComp).add(blkState.parse(new StringReader(e)).getState());
					} catch(Throwable ex)
					{
						TSwap.LOG.warn("Failed to decode " + e + " into something meaningful.");
					}
				}
			}
		}

		TSwap.LOG.info("Transposing block state map...");

		transpose(toolStates);

		TSwap.LOG.info("Saving configs...");

		if(cfg.hasChanged()) cfg.save();

		TSwap.LOG.info("Configs updated.");
	}

	public ToolType getEffectiveTool(ItemStack stack, BlockState state)
	{
		Set<ToolType> tools = stack.getItem().getToolTypes(stack);
		return tools.stream().filter(tt -> getToolStates().computeIfAbsent(state, toolStatesCompF).contains(tt.getName())).findFirst().orElse(null);
	}
}