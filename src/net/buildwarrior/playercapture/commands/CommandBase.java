package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.Config;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.ChatType;
import net.buildwarrior.playercapture.versions.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class CommandBase {

	String helpMessage() {
		return 	 ChatColor.GRAY + ChatColor.BOLD.toString() + "----------------[PlayerCapture]----------------"
		+ "\n" + ChatColor.GREEN + "/pc record <name> " + ChatColor.GRAY + "Start recording your movement."
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

				npc.sendClickableMessage(player, ChatColor.GREEN + npc.getName() + ": ",
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

	void record(String name, String displayName, SkinCatch skinCatch, Player player, ChatType chat, boolean sneak) {
		NPCModule.getInstance().addNPC(name, player, skinCatch);

		NPCModule.getInstance().getNPC(name).setDisplayName(displayName);

		NPCModule.getInstance().getNPC(name).setUp();

		RecordTask recordTask = new RecordTask(NPCModule.getInstance().getNPC(name));
		recordTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

		recordTask.setChat(chat);
		recordTask.setSneak(sneak);

		NPCModule.getInstance().getNPC(name).setChatType(chat);

		PlayerCapture.getInstance().getCurrentlyRecording().put(player, recordTask);

		player.getInventory().setHeldItemSlot(1);
		player.sendMessage(ChatColor.GREEN + "Recording! press 1 to stop recording!");
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

		if(oldFile.getData().contains("ChatType")) {
			updatedFile.getData().set("ChatType", oldFile.getData().getString("ChatType"));
		} else {
			updatedFile.getData().set("ChatType", ChatType.NONE.name());
		}

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

			String[] mainHand = null;
			String[] offHand = null;
			String[] helmet = null;
			String[] chestplate = null;
			String[] leggings = null;
			String[] boots = null;

			String chat = null;

			boolean hit = false;
			boolean glid = false;
			boolean swim = false;
			boolean sleep = false;
			boolean sneak = false;

			if(s.contains("[") || s.contains("{") || splitFrame.length == 5) {//convert 1.3

				String[] values = null;
				String rawValue = "";
				String[] armor = null;
				String rawArmor = "";

				if(s.contains("[")) {
					values = splitFrame[5].replace("[", "").replace("]", "").split(",");
					rawValue = splitFrame[5].replace("[", "").replace("]", "");
				}

				if(s.contains("{") && values != null) {
					armor = splitFrame[6].replace("{", "").replace("}", "").split(",");
					rawArmor = splitFrame[6].replace("{", "").replace("}", "");

				} else if(s.contains("{") && values == null) {
					armor = splitFrame[5].replace("{", "").replace("}", "").split(",");
					rawArmor = splitFrame[5].replace("{", "").replace("}", "");
				}

				if(values != null && rawValue.contains("Chat")) {
					chat = values[0];
				}

				if(values != null && rawValue.contains("Sneak")) { sneak = true; }
				if(values != null && rawValue.contains("Sleep")) { sleep = true; }
				if(values != null && rawValue.contains("Gliding")) { glid = true; }
				if(values != null && rawValue.contains("Swim")) { swim = true; }
				if(values != null && rawValue.contains("Hit")) { hit = true; }

				if(armor != null && rawArmor.contains("Helmet")) {
					helmet = PlayerCapture.getInstance().getAmor(armor, "Helmet").split(":");
				}

				if(armor != null && rawArmor.contains("Chestplate")) {
					chestplate = PlayerCapture.getInstance().getAmor(armor, "Chestplate").split(":");
				}

				if(armor != null && rawArmor.contains("Leggings")) {
					leggings = PlayerCapture.getInstance().getAmor(armor, "Leggings").split(":");
				}

				if(armor != null && rawArmor.contains("Boots")) {
					boots = PlayerCapture.getInstance().getAmor(armor, "Boots").split(":");
				}

				if(armor != null && rawArmor.contains("MainHand")) {
					mainHand = PlayerCapture.getInstance().getAmor(armor, "MainHand").split(":");
				}

				if(armor != null && rawArmor.contains("OffHand")) {
					offHand = PlayerCapture.getInstance().getAmor(armor, "OffHand").split(":");
				}

			} else if(splitFrame.length >= 15) {//convert 1.2
				mainHand = splitFrame[9].split(":");
				offHand = splitFrame[10].split(":");
				helmet = splitFrame[11].split(":");
				chestplate = splitFrame[12].split(":");
				leggings = splitFrame[13].split(":");
				boots = splitFrame[14].split(":");

				sneak = Boolean.parseBoolean(splitFrame[5]);

				hit = Boolean.parseBoolean(splitFrame[15].replace("Hit:", ""));
				glid = Boolean.parseBoolean(splitFrame[7].replace("Fly:", ""));
				swim = Boolean.parseBoolean(splitFrame[8].replace("Swim:", ""));
				sleep = Boolean.parseBoolean(splitFrame[6].replace("Sleep:", ""));

			} else {//convert 1.1
				mainHand = splitFrame[6].split(":");
				offHand = splitFrame[7].split(":");
				helmet = splitFrame[8].split(":");
				chestplate = splitFrame[9].split(":");
				leggings = splitFrame[10].split(":");
				boots = splitFrame[11].split(":");

				sneak = Boolean.parseBoolean(splitFrame[5]);

				hit = Boolean.parseBoolean(splitFrame[12].replace("Hit:", ""));
			}

			String values = "";
			String armor = "";

			if(chat != null) {
				values += "Chat(" + chat + "),";
			}

			if(sneak) {
				values += "Sneak,";
			}

			if(sleep) {
				values += "Sleep,";
			}

			if(glid) {
				values += "Gliding,";
			}

			if(swim) {
				values += "Swim,";
			}

			if(hit) {
				values += "Hit,";
			}

			if(mainHand != null) {
				armor += "MainHand:" + mainHand[1] + ":" + mainHand[2] + ",";
			}

			if(offHand != null) {
				armor += "OffHand:" + offHand[1] + ":" + offHand[2] + ",";
			}

			if(helmet != null) {
				armor += "Helmet:" + helmet[1] + ":" + helmet[2] + ",";
			}

			if(chestplate != null) {
				armor += "Chestplate:" + chestplate[1] + ":" + chestplate[2] + ",";
			}

			if(leggings != null) {
				armor += "Leggings:" + leggings[1] + ":" + leggings[2] + ",";
			}

			if(boots != null) {
				armor += "Boots:" + boots[1] + ":" + boots[2] + ",";
			}

			String input = splitFrame[0] + "/" +
					splitFrame[1] + "/" +
					splitFrame[2] + "/" +
					splitFrame[3] + "/" +
					splitFrame[4];

			if(!values.equalsIgnoreCase("")) {
				input += "/[" +
						values +
						"]";
			}

			if(!armor.equalsIgnoreCase("")) {
				input += "/{" +
						armor +
						"}";
			}

			list.add(input);
		}

		updatedFile.getData().set("Frame", list);
		updatedFile.saveConfig();
	}
}