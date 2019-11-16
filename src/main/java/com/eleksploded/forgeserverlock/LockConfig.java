package com.eleksploded.forgeserverlock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraftforge.common.ForgeConfigSpec;

final class LockConfig {
	final ForgeConfigSpec.BooleanValue isLocked;
	final ForgeConfigSpec.ConfigValue<String> message;
	final ForgeConfigSpec.ConfigValue<List<String>> playerWhitelist;
	final ForgeConfigSpec.ConfigValue<List<String>> unWhitelist;

	LockConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		isLocked = builder
				.comment("Is the server currently locked")
				.translation("forgeserverlock.locked")
				.define("isLocked", false);
		message = builder
				.comment("The message shown on kick")
				.translation("forgeserverlock.message")
				.define("message", "Server is currently locked. Please try again later");
		playerWhitelist = builder
				.comment("Player UUIDs that are whitelisted to bypass the lock")
				.translation("forgeserverlock.playerwhitelist")
				.define("playerwhitelist", new ArrayList<String>());
		unWhitelist = builder
				.comment("Player Usernames that are whitelisted to bypass the lock. It is recommended to use the UUID whitelist instead")
				.translation("forgeserverlock.unwhitelist")
				.define("unwhitelist", new ArrayList<String>());
		builder.pop();
	}
	
	public Boolean getLocked() {
		return isLocked.get();
	}
	
	public void setLocked(boolean in) {
		isLocked.set(in);
		isLocked.save();
	}
	
	public String getMessage() {
		return message.get();
	}
	
	public void setMessage(String messageIn) {
		message.set(messageIn);
		message.save();
	}
	
	public List<UUID> getPlayerWhitelist() {
		List<UUID> uuids = new ArrayList<UUID>();
		playerWhitelist.get().forEach(id -> uuids.add(UUID.fromString(id)));
		return uuids;
	}
	
	public void setPlayerWhitelist(List<UUID> in) {
		List<String> uuids = new ArrayList<String>();
		in.forEach(id -> uuids.add(id.toString()));
		playerWhitelist.set(uuids);
		playerWhitelist.save();
	}
	
	public List<String> getUnWhitelist() {
		return unWhitelist.get();
	}
	
	public void setUnWhitelist(List<String> in) {
		unWhitelist.set(in);
		unWhitelist.save();
	}
}
