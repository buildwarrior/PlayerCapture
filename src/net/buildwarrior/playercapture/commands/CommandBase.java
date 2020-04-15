package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

class CommandBase {

	String helpMessage() {
		return 	 ChatColor.GRAY + ChatColor.BOLD.toString() + "----------------[PlayerCapture]----------------"
		+ "\n" + ChatColor.GREEN + "/pc record <name> <displayName> <skinID> <player> " + ChatColor.GRAY + "Start recording your movement, displayName, skinID and player are optional."
		+ "\n" + ChatColor.GREEN + "/pc play <name> " + ChatColor.GRAY + "Play npc recording."
		+ "\n" + ChatColor.GREEN + "/pc stop <name> " + ChatColor.GRAY + "Stop npc recording."
		+ "\n" + ChatColor.GREEN + "/pc list " + ChatColor.GRAY + "List all recordings."
		+ "\n" + ChatColor.GREEN + "/pc remove <name> <Config:true/false> " + ChatColor.GRAY + "Remove recording, optional to delete config as well."
		+ "\n" + ChatColor.GREEN + "/pc info <name> " + ChatColor.GRAY + "Show information about a recording."
		+ "\n" + ChatColor.GREEN + "/pc setskin <name> <skinID> " + ChatColor.GRAY + "Update a recordings skin."
		+ "\n" + ChatColor.GREEN + "/pc setdisplayname <name> <displayName> " + ChatColor.GRAY + "Update a recordings displayName."
		+ "\n" + ChatColor.GREEN + "/pc setloop <name> <true/false> " + ChatColor.GRAY + "Set looped a recording."
		+ "\n" + ChatColor.GREEN + "/pc import <name> " + ChatColor.GRAY + "Import a old config."
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
			player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "NPC's:");

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

	void record(String name, String displayName, OfflinePlayer skin, Player player) {
		NPCModule.getInstance().addNPC(name, player, skin);
		NPCModule.getInstance().getNPC(name).setUp(true);
		NPCModule.getInstance().getNPC(name).setDisplayName(displayName);

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
}