package com.eleksploded.forgeserverlock;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class LockCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("lock")
				.requires(source -> source.hasPermissionLevel(4))
				.then(Commands.literal("toggle").executes(command -> {
					ForgeServerLock.updateLock(!ForgeServerLock.locked);
					command.getSource().sendFeedback(new StringTextComponent("Server Lock is now " + (ForgeServerLock.locked ? "enabled" : "disabled")),true);
					return 0;
				}))
				.then(Commands.literal("enable").executes(command -> {
					ForgeServerLock.updateLock(true);
					command.getSource().sendFeedback(new StringTextComponent("Server Lock is now " + (ForgeServerLock.locked ? "enabled" : "disabled")),true);
					return 0;
				}))
				.then(Commands.literal("disable").executes(command -> {
					ForgeServerLock.updateLock(false);
					command.getSource().sendFeedback(new StringTextComponent("Server Lock is now " + (ForgeServerLock.locked ? "enabled" : "disabled")),true);
					return 0;
				}))
				.then(Commands.literal("status").executes(command -> {
					command.getSource().sendFeedback(new StringTextComponent("Server Lock is " + (ForgeServerLock.locked ? "enabled" : "disabled")), false);
					return 0;
				}))	
				.then(Commands.literal("help").executes(command -> {
					command.getSource().sendFeedback(new StringTextComponent("Valid commands are: enable, disable, status, message, help, & bypass"),false);
					return 0;
				}))
				.then(Commands.literal("message").then(Commands.argument("message", MessageArgument.message()).executes(command -> {
					ForgeServerLock.message = MessageArgument.getMessage(command, "message").getFormattedText();
					ForgeServerLock.updateConfig();
					command.getSource().sendFeedback(new StringTextComponent("Changed the kick message to: " + ForgeServerLock.message), true);
					return 0;
				})))
				.then(Commands.literal("bypass").executes(command -> {
					command.getSource().sendFeedback(new StringTextComponent("Valid commands are: list, populate, allow, allowName, remove, removeName"),false);
					return 0;
				})
						.then(Commands.literal("list").executes(command -> {
							StringBuilder builder = new StringBuilder();
							MinecraftServer server = command.getSource().getServer();
							builder.append("Player List: ");
							if(!ForgeServerLock.allowedPlayers.isEmpty()) {
								ForgeServerLock.allowedPlayers.forEach(uuid -> builder.append(server.getPlayerList().getPlayerByUUID(uuid).getDisplayName().getString() + "   "));
							} else {
								builder.append("None");
							}
							
							StringBuilder build3 = new StringBuilder();
							build3.append("Temp-Player List: ");
							if(!ForgeServerLock.allowedTemp.isEmpty()) {
								ForgeServerLock.allowedTemp.forEach(uuid -> build3.append(server.getPlayerList().getPlayerByUUID(uuid).getDisplayName().getString() + "   "));
							} else {
								builder.append("None");
							}
							
							StringBuilder build2 = new StringBuilder();
							build2.append("Username List: ");
							if(!ForgeServerLock.allowedUN.isEmpty()) {
								ForgeServerLock.allowedUN.forEach(un -> build2.append(un + "   "));
							} else {
								build2.append("None");
							}
							
							command.getSource().sendFeedback(new StringTextComponent(builder.toString()),false);
							command.getSource().sendFeedback(new StringTextComponent(build3.toString()),false);
							command.getSource().sendFeedback(new StringTextComponent(build2.toString()),false);
							return 0;
						}))
						.then(Commands.literal("populate").executes(command -> {
							command.getSource().sendFeedback(new StringTextComponent("Populating temporary bypass list with all online players"), true);
							command.getSource().getServer().getPlayerList().getPlayers().forEach(player -> {
								ForgeServerLock.allowedTemp.add(PlayerEntity.getUUID(player.getGameProfile()));
							});
							ForgeServerLock.updateConfig();
							command.getSource().sendFeedback(new StringTextComponent("Done populating temporary bypass list"), true);
							command.getSource().sendFeedback(new StringTextComponent("Reminder: This list is cleared on server restart"), true);
							return 0;
						}))
						.then(Commands.literal("allowName").then(Commands.argument("Username", StringArgumentType.string()).executes(command -> {
							String un = StringArgumentType.getString(command, "Username");
							if(!containsString(un, ForgeServerLock.allowedUN)) {
								ForgeServerLock.allowedUN.add(un.toLowerCase());
								ForgeServerLock.updateConfig();
								command.getSource().sendFeedback(new StringTextComponent("Added " + un + " to the Username bypass list."),true);
								command.getSource().sendFeedback(new StringTextComponent("It is recommended to use the base allow command for more robust filtering."), false);
							}
							return 0;
						})))
						.then(Commands.literal("removeName").then(Commands.argument("Username", StringArgumentType.string()).executes(command -> {
							String un = StringArgumentType.getString(command, "Username");
							if(containsString(un, ForgeServerLock.allowedUN)) {
								ForgeServerLock.allowedUN.remove(un.toLowerCase());
								ForgeServerLock.updateConfig();
								command.getSource().sendFeedback(new StringTextComponent("Removed " + un + " to the Username bypass list."),true);
							}
							return 0;
						})))
						.then(Commands.literal("clear")
								.then(Commands.literal("player").executes(command-> {
									ForgeServerLock.allowedPlayers.clear();
									ForgeServerLock.updateConfig();
									command.getSource().sendFeedback(new StringTextComponent("Cleared player bypass list"), true);
									return 0;
								}))
								.then(Commands.literal("username").executes(command -> {
									ForgeServerLock.allowedUN.clear();
									ForgeServerLock.updateConfig();
									command.getSource().sendFeedback(new StringTextComponent("Cleared username bypass list"), true);
									return 0;
								}))
								.then(Commands.literal("temp").executes(command -> {
									ForgeServerLock.allowedTemp.clear();
									command.getSource().sendFeedback(new StringTextComponent("Cleared temporary player bypass list"), true);
									return 0;
								})))
						.then(Commands.literal("allow").then(Commands.argument("player", EntityArgument.player()).executes(command -> {
							UUID id = PlayerEntity.getUUID(EntityArgument.getPlayer(command, "player").getGameProfile());
							if(!ForgeServerLock.allowedPlayers.contains(id)) {
								ForgeServerLock.allowedPlayers.add(id);
								ForgeServerLock.updateConfig();
								command.getSource().sendFeedback(new StringTextComponent("Added ").appendSibling(EntityArgument.getPlayer(command, "player").getName()).appendSibling(new StringTextComponent(" to the bypass list.")),true);
							}
							return 0;
						})))
						.then(Commands.literal("remove").then(Commands.argument("player", EntityArgument.player()).executes(command -> {
							UUID id = PlayerEntity.getUUID(EntityArgument.getPlayer(command, "player").getGameProfile());
							if(ForgeServerLock.allowedPlayers.contains(id)) {
								ForgeServerLock.allowedPlayers.remove(id);
								ForgeServerLock.updateConfig();
								command.getSource().sendFeedback(new StringTextComponent("Removed ").appendSibling(EntityArgument.getPlayer(command, "player").getName()).appendSibling(new StringTextComponent(" to the bypass list.")),true);
							}
							return 0;
						}))))
				.executes(command -> {
					command.getSource().sendFeedback(new StringTextComponent("Valid commands are: enable, disable, status, message, help, & bypass"),false);
					return 0;
				}));
	}
	
	static boolean result;
	static boolean containsString(String in, List<String> list) {
		result = false;
		list.forEach(str -> {
			if(str.equalsIgnoreCase(in)) {
				result = true;
			}
		});
		return result;
	}
}
