package com.eleksploded.forgeserverlock;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class LockCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("lock")
				.requires(source -> source.hasPermissionLevel(4))
				.executes((command) -> {
					ForgeServerLock.locked = !ForgeServerLock.locked;
					command.getSource().getServer().sendMessage(new StringTextComponent("Server locking has been " + (ForgeServerLock.locked ? "enabled" : "disabled")));
					return 0;
				}));
	}
}
