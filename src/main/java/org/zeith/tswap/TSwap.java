package org.zeith.tswap;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.tswap.client.TSConfig;
import org.zeith.tswap.client.TSData;
import org.zeith.tswap.proxy.TSClientProxy;
import org.zeith.tswap.proxy.TSCommonProxy;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@Mod("tswap")
public class TSwap
{
	public static final Logger LOG = LogManager.getLogger("ToolSwap");
	public static final TSCommonProxy PROXY = DistExecutor.unsafeRunForDist(() -> TSClientProxy::new, () -> TSCommonProxy::new);

	public final Path cfgDirPath = FMLPaths.CONFIGDIR.get().resolve("tswap");

	public static final TSConfig CONFIG = new TSConfig(FMLPaths.CONFIGDIR.get().resolve("tswap").resolve("main.cfg").toFile());
	public static final TSData DATA = new TSData(FMLPaths.CONFIGDIR.get().resolve("tswap").resolve("data.xml").toFile());

	public TSwap()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		PROXY.clinit();

		File file = cfgDirPath.toFile();
		if(!file.isDirectory()) file.mkdirs();
		DATA.load();
	}

	private void loadComplete(FMLLoadCompleteEvent event)
	{
		CONFIG.reload();
	}

	private void enqueueIMC(InterModEnqueueEvent event)
	{
		Consumer<CommandDispatcher<CommandSource>> handler = this::registerCommands;
		InterModComms.sendTo("clientcommands", "register_commands", () -> handler);
	}

	private void registerCommands(CommandDispatcher<CommandSource> cmds)
	{
		cmds.register(Commands.literal("tswap")
				.then(Commands.literal("toggle")
						.executes(cs ->
						{
							DATA.shouldSwap(!DATA.shouldSwap());
							DATA.save();
							return Command.SINGLE_SUCCESS;
						})
				)
				.then(Commands.literal("reload")
						.executes(cs ->
						{
							DATA.load();
							CONFIG.reload();
							cs.getSource().sendSuccess(new StringTextComponent("Configs reloaded!"), false);
							return Command.SINGLE_SUCCESS;
						})
				)
		);
	}
}