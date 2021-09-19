package org.zeith.tswap.client;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zeith.tswap.TSwap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SwapEvents
{
	public static final Predicate<PlayerEntity> CLIENT_PLAYER = pl -> Objects.equals(pl.getGameProfile().getId(), Minecraft.getInstance().getUser().getGameProfile().getId());

	public static int swapTime = 0;
	public static IntList slotHistory = new IntArrayList();

	public static void setSwapped()
	{
		swapTime = TSwap.CONFIG.getSwapDelay();
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent e)
	{
		// Skip end phase
		if(e.phase == TickEvent.Phase.END)
			return;

		if(swapTime == 0 && slotHistory.size() > 0)
		{
			Minecraft.getInstance().player.inventory.selected = slotHistory.getInt(0);
			slotHistory.clear();
		}

		if(swapTime > 0)
			--swapTime;

		PlayerEntity player = Minecraft.getInstance().player;
		RayTraceResult result = Minecraft.getInstance().hitResult;
		if(player != null && result instanceof EntityRayTraceResult && ((EntityRayTraceResult) result).getEntity() instanceof LivingEntity)
		{
			if(!TSwap.DATA.shouldSwap() || !CLIENT_PLAYER.test(player))
				return;

			PlayerInventory p = player.inventory;

			Map<Integer, SwordItem> swords = new HashMap<>();

			for(int i = 0; i < 9; ++i)
			{
				ItemStack stack = p.getItem(i);
				if(stack.isEmpty()) continue;
				if(stack.getItem() instanceof SwordItem) swords.put(i, (SwordItem) stack.getItem());
			}

			int maxS = -1;
			float maxHL = Integer.MIN_VALUE;
			for(Integer slot : swords.keySet())
			{
				if(slot == null || swords.get(slot) == null)
					continue;
				float hl = swords.get(slot).getDamage();
				if(hl > maxHL)
				{
					maxHL = hl;
					maxS = slot;
				}
			}

			if(maxS != -1)
			{
				if(player.inventory.selected != maxS)
					slotHistory.add(player.inventory.selected);
				player.inventory.selected = maxS;
				swapTime = TSwap.CONFIG.getSwordSwapDelay();
			}
		}
	}

	@SubscribeEvent
	public static void breakSpeed(PlayerEvent.BreakSpeed e)
	{
		PlayerEntity player = e.getPlayer();

		if(!TSwap.DATA.shouldSwap() || player == null || !CLIENT_PLAYER.test(player))
			return;

		int maxS = -1;

		try
		{
			maxS = getSlotForBlock(player, e.getPos(), e.getState());
		} catch(Throwable err)
		{
		}

		if(maxS != -1)
		{
			int prev = player.inventory.selected;
			player.inventory.selected = maxS;
			boolean c = e.getState().canHarvestBlock(player.level, e.getPos(), player);
			player.inventory.selected = prev;

			if(c)
			{
				if(player.inventory.selected != maxS)
					slotHistory.add(player.inventory.selected);
				player.inventory.selected = maxS;
				setSwapped();
			}
		}
	}

	public static int getSlotForBlock(PlayerEntity player, BlockPos pos, BlockState state)
	{
		if(player == null || state == null)
			return -1;

		PlayerInventory p = player.inventory;

		Map<Integer, Integer> harvestLevels = new HashMap<>();

		for(int i = 0; i < 9; ++i)
		{
			ItemStack stack = p.getItem(i);
			if(stack.isEmpty()) continue;
			Set<ToolType> tools = stack.getItem().getToolTypes(stack);
			for(ToolType tool : tools)
			{
				int hl = stack.getItem().getHarvestLevel(stack, tool, player, state);
				if(state.getBlock().isToolEffective(state, tool))
					harvestLevels.put(i, harvestLevels.getOrDefault(i, 0) + hl);
			}
		}

		int maxS = -1;
		int maxHL = Integer.MIN_VALUE;
		for(Integer slot : harvestLevels.keySet())
		{
			if(slot == null || harvestLevels.get(slot) == null)
				continue;
			int hl = harvestLevels.get(slot);
			if(hl > maxHL)
			{
				maxHL = hl;
				maxS = slot;
			}
		}

		if(maxS == -1)
		{
			harvestLevels.clear();

			for(int i = 0; i < 9; ++i)
			{
				ItemStack stack = p.getItem(i);
				if(stack.isEmpty()) continue;
				ToolType tool = TSwap.CONFIG.getEffectiveTool(stack, state);
				if(tool != null)
					harvestLevels.put(i, harvestLevels.getOrDefault(i, 0) + stack.getItem().getHarvestLevel(stack, tool, player, state));
			}

			maxHL = Integer.MIN_VALUE;
			for(Integer slot : harvestLevels.keySet().toArray(new Integer[0]))
			{
				if(slot == null || harvestLevels.get(slot) == null)
					continue;
				int hl = harvestLevels.get(slot);
				if(hl > maxHL)
				{
					maxHL = hl;
					maxS = slot;
				}
			}
		}

		return maxS;
	}
}