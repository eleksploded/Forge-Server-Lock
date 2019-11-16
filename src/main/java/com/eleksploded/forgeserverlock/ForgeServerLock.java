package com.eleksploded.forgeserverlock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod("forgeserverlock")
@Mod.EventBusSubscriber
public class ForgeServerLock
{
	public static boolean locked;
	public static List<UUID> allowedPlayers = new ArrayList<UUID>();
	public static List<String> allowedUN = new ArrayList<String>();
	public static List<UUID> allowedTemp = new ArrayList<UUID>();
	public static String message = "Server is currently locked. Please try again later";
	static Pair<LockConfig, ForgeConfigSpec> specPair;
	
    public ForgeServerLock() {
    	specPair = new ForgeConfigSpec.Builder().configure(LockConfig::new);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, specPair.getRight());
    	
    }
    
    @SubscribeEvent
    public static void serverStart(FMLServerStartingEvent e) {
    	LockCommand.register(e.getCommandDispatcher());
    }
    
    @SubscribeEvent
    public static void serverStarted(FMLServerStartedEvent e) {
    	LockConfig config = specPair.getLeft();
    	
    	message = config.getMessage();
    	allowedPlayers = config.getPlayerWhitelist();
    	allowedUN = config.getUnWhitelist();
    	locked = config.getLocked();
    }
    
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
    	if(locked && !isAllowed(event.getPlayer())) {
    		MinecraftServer server = event.getEntityLiving().world.getServer();
    		try {
				CommandDispatcher<CommandSource> cmd = server.getCommandManager().getDispatcher();
				ParseResults<CommandSource> result = cmd.parse("kick " + event.getPlayer().getName().getString() + " " + message, server.getCommandSource());
				cmd.execute(result);
    		} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public static void updateConfig() {
    	LockConfig config = specPair.getLeft();
    	
    	config.setMessage(message);
    	config.setPlayerWhitelist(allowedPlayers);
    	config.setUnWhitelist(allowedUN);
    }
    
    public static void updateLock(boolean status) {
    	locked = status;
    	specPair.getLeft().setLocked(locked);
    }
    
    static boolean isAllowed(PlayerEntity player) {
    	if(allowedPlayers.contains(PlayerEntity.getUUID(player.getGameProfile()))) {
    		return true;
    	} else if(allowedUN.contains(player.getDisplayName().getString().toLowerCase())) {
    		return true;
    	} else if(allowedTemp.contains(PlayerEntity.getUUID(player.getGameProfile()))){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    @SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
		final ModConfig config = event.getConfig();
		if (config.getSpec() == specPair.getRight()) {
			LockConfig lock = specPair.getLeft();
	    	
	    	message = lock.getMessage();
	    	allowedPlayers = lock.getPlayerWhitelist();
	    	allowedUN = lock.getUnWhitelist();
	    	locked = lock.getLocked();
		}
	}
}