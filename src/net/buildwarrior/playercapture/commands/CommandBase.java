package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.Config;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.ItemBuilder;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class CommandBase {

	String helpMessage() {
		return 	 ChatColor.GRAY + ChatColor.BOLD.toString() + "----------------[PlayerCapture]----------------"
		+ "\n" + ChatColor.GREEN + "/pc record <name> <displayName> <skinID> <player> " + ChatColor.GRAY + "Start recording your movement, displayName, skinID and player are optional."
		+ "\n" + ChatColor.GREEN + "/pc play <name> " + ChatColor.GRAY + "Play npc recording."
		+ "\n" + ChatColor.GREEN + "/pc stop <name> " + ChatColor.GRAY + "Stop npc recording."
		+ "\n" + ChatColor.GREEN + "/pc list " + ChatColor.GRAY + "List all recordings."
		+ "\n" + ChatColor.GREEN + "/pc remove <name> <Config:true/false> " + ChatColor.GRAY + "Remove recording optional to archive config or delete it for good."
		+ "\n" + ChatColor.GREEN + "/pc info <name> " + ChatColor.GRAY + "Show information about a recording."
		+ "\n" + ChatColor.GREEN + "/pc setskin <name> <skinID> " + ChatColor.GRAY + "Update a npc skin."
		+ "\n" + ChatColor.GREEN + "/pc setdisplayname <name> <displayName> " + ChatColor.GRAY + "Update a npc displayName."
		+ "\n" + ChatColor.GREEN + "/pc setloop <name> <true/false> " + ChatColor.GRAY + "Set looping for a npc."
		+ "\n" + ChatColor.GREEN + "/pc import <name> " + ChatColor.GRAY + "Import a config from an old plugin version or reimport an removed config"
		+ "\n" + ChatColor.GREEN + "/pc reload " + ChatColor.GRAY + "Reload plugin."
		+ "\n" + " "
		+ "\n" + ChatColor.BLUE + "<skinID> is a player name"
		+ "\n" + ChatColor.GRAY + ChatColor.BOLD.toString() + "---------------------------------------------";
	}

	String info(NPC npc) {
		return 	 ChatColor.GRAY + "----------------------------------------------------"
		+ "\n" + ChatColor.GOLD + ChatColor.BOLD.toString() + npc.getName()
		+ "\n" + ChatColor.GREEN + "Display name: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', npc.getDisplayName())
		+ "\n" + ChatColor.GREEN + "World: " + ChatColor.WHITE + npc.getWorld().getName()
		+ "\n" + ChatColor.GREEN + "Number of frames: " + ChatColor.WHITE + npc.getFrames().size()
		+ "\n" + ChatColor.GREEN + "Loop: " + ChatColor.WHITE + npc.isLoop()
		+ "\n" + ChatColor.GRAY + "----------------------------------------------------";
	}

	void list(Player player, CommandSender sender) {
		if(sender instanceof Player) {
			player.sendMessage(ChatColor.GRAY + "----------------------------------------------------");
			player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "NPC's: " + ChatColor.WHITE + "(" + NPCModule.getInstance().getAll().size() + ")");

			for(NPC npc : NPCModule.getInstance().getAll()) {
				sendClickableMessage(player, ChatColor.GREEN + npc.getName() + ": ",
						ChatColor.WHITE + "[View information]", "pc info " + npc.getName());
			}
			player.sendMessage(ChatColor.GRAY + "----------------------------------------------------");

		} else {
			sender.sendMessage(ChatColor.GRAY + "----------------------------------------------------");
			sender.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "NPC's:");

			for(NPC npc : NPCModule.getInstance().getAll()) {
				sender.sendMessage(ChatColor.GREEN + npc.getName());
			}
			sender.sendMessage(ChatColor.GRAY + "----------------------------------------------------");
		}
	}

	void record(String name, String displayName, SkinCatch skinCatch, Player player) {
		NPCModule.getInstance().addNPC(name, player, skinCatch);

		NPCModule.getInstance().getNPC(name).setDisplayName(displayName);

		NPCModule.getInstance().getNPC(name).setUp();

		RecordTask recordTask = new RecordTask(NPCModule.getInstance().getNPC(name));
		recordTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

		PlayerCapture.getInstance().getCurrentlyRecording().put(player, recordTask);

		player.getInventory().setHeldItemSlot(1);
		player.sendMessage(ChatColor.GREEN + "Recording! press 1 to stop recording!");
	}

	private void sendClickableMessage(Player player, String text, String clickableText, String runCommand) {
		IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\",\"extra\":" + "[{\"text\":\"" + clickableText + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":" + "\"/" + runCommand + "\"}}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(chat);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	void convert(String old) {
		Config updatedFile = new Config(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + old + ".yml");
		Config oldFile = new Config(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + old + ".yml");

		List<String> list = new ArrayList<>();

		updatedFile.getData().set("DisplayName", oldFile.getData().getString("DisplayName"));
		updatedFile.getData().set("World", oldFile.getData().getString("World"));
		updatedFile.getData().set("Skin", oldFile.getData().getString("Skin"));
		updatedFile.getData().set("Loop", oldFile.getData().getBoolean("Loop"));
		updatedFile.getData().set("Value", oldFile.getData().getString("Value"));
		updatedFile.getData().set("Signature", oldFile.getData().getString("Signature"));

		if(oldFile.getData().contains("ShowHat")) {
			updatedFile.getData().set("ShowHat", oldFile.getData().getBoolean("ShowHat"));
		} else {
			updatedFile.getData().set("ShowHat", true);
		}
		if(oldFile.getData().contains("ShowLeftArm")) {
			updatedFile.getData().set("ShowLeftArm", oldFile.getData().getBoolean("ShowLeftArm"));
		} else {
			updatedFile.getData().set("ShowLeftArm", true);
		}
		if(oldFile.getData().contains("ShowRightArm")) {
			updatedFile.getData().set("ShowRightArm", oldFile.getData().getBoolean("ShowRightArm"));
		} else {
			updatedFile.getData().set("ShowRightArm", true);
		}
		if(oldFile.getData().contains("ShowLeftLeg")) {
			updatedFile.getData().set("ShowLeftLeg", oldFile.getData().getBoolean("ShowLeftLeg"));
		} else {
			updatedFile.getData().set("ShowLeftLeg", true);
		}
		if(oldFile.getData().contains("ShowRightLeg")) {
			updatedFile.getData().set("ShowRightLeg", oldFile.getData().getBoolean("ShowRightLeg"));
		} else {
			updatedFile.getData().set("ShowRightLeg", true);
		}
		if(oldFile.getData().contains("ShowBody")) {
			updatedFile.getData().set("ShowBody", oldFile.getData().getBoolean("ShowBody"));
		} else {
			updatedFile.getData().set("ShowBody", true);
		}
		if(oldFile.getData().contains("ShowCape")) {
			updatedFile.getData().set("ShowCape", oldFile.getData().getBoolean("ShowCape"));
		} else {
			updatedFile.getData().set("ShowCape", true);
		}

		for(String s : oldFile.getData().getStringList("Frame")) {

			String[] splitFrame = s.split("/");

			String[] mainHand;
			String[] offHand;
			String[] helmet;
			String[] chestplate;
			String[] leggings;
			String[] boots;

			boolean hit;
			boolean fly;
			boolean swim;
			boolean sleep;

			if(splitFrame.length >= 15) {
				mainHand = splitFrame[9].split(":");
				offHand = splitFrame[10].split(":");
				helmet = splitFrame[11].split(":");
				chestplate = splitFrame[12].split(":");
				leggings = splitFrame[13].split(":");
				boots = splitFrame[14].split(":");

				hit = Boolean.parseBoolean(splitFrame[15].replace("Hit:", ""));
				fly = Boolean.parseBoolean(splitFrame[7].replace("Fly:", ""));
				swim = Boolean.parseBoolean(splitFrame[8].replace("Swim:", ""));
				sleep = Boolean.parseBoolean(splitFrame[6].replace("Sleep:", ""));

			} else {
				mainHand = splitFrame[6].split(":");
				offHand = splitFrame[7].split(":");
				helmet = splitFrame[8].split(":");
				chestplate = splitFrame[9].split(":");
				leggings = splitFrame[10].split(":");
				boots = splitFrame[11].split(":");

				hit = Boolean.parseBoolean(splitFrame[12].replace("Hit:", ""));
				fly = false;
				swim = false;
				sleep = false;
			}

			list.add(
					splitFrame[0] + "/" +
					splitFrame[1] + "/" +
					splitFrame[2] + "/" +
					splitFrame[3] + "/" +
					splitFrame[4] + "/" +
					splitFrame[5] + "/" +
					"Sleep:" + sleep +
					"/Fly:" + fly +
					"/Swim:" + swim +
					"/MainHand:" + mainHand[1] + ":" + mainHand[2] +
					"/OffHand:" + offHand[1] + ":" + offHand[2] +
					"/Helmet:" + helmet[1] + ":" + helmet[2] +
					"/Chestplate:" + chestplate[1] + ":" + chestplate[2] +
					"/Leggings:" + leggings[1] + ":" + leggings[2] +
					"/Boots:" + boots[1] + ":" + boots[2] +
					"/Hit:" + hit);
		}

		updatedFile.getData().set("Frame", list);
		updatedFile.saveConfig();
	}
}