package com.eleksploded.forgeserverlock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod("forgeserverlock")
@Mod.EventBusSubscriber
public class ForgeServerLock
{
	public static boolean locked;
	
    public ForgeServerLock() {
    	locked = false;
    }
    
    @SubscribeEvent
    public static void serverStart(FMLServerStartingEvent e) {
    	LockCommand.register(e.getCommandDispatcher());
    }
    
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(locked) {
    		MinecraftServer server = event.getEntityLiving().world.getServer();
    		try {
				CommandDispatcher<CommandSource> cmd = server.getCommandManager().getDispatcher();
				ParseResults<CommandSource> result = cmd.parse("kick " + event.getPlayer().getName().getString() + " Server is currently locked. Please try again later", server.getCommandSource());
				cmd.execute(result);
    		} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
        	
    	}
    }
}