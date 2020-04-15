package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.utils.PermissionLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class CaptureCommand extends CommandBase implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (args.length > 0) {

			if (args[0].equalsIgnoreCase("record") && args.length > 1) {

				if(!sender.hasPermission(PermissionLang.RECORD)) {
					sender.sendMessage(ChatColor.RED + "You need the permission 'playercapture.command.record' to do this command!");
					return true;
				}

				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC already exists with that name!");
					return true;
				}

				if(args.length == 5) {
					Player target = Bukkit.getPlayer(args[4]);

					if(target == null) {
						sender.sendMessage(ChatColor.RED + "Unknown player!");
						return true;
					}

					record(args[1], args[2], Bukkit.getOfflinePlayer(args[3]), target);
					return true;
				}

				if(player == null) {
					sender.sendMessage(ChatColor.RED + "Only players can do this command!");
					return true;
				}

				if(PlayerCapture.getInstance().getCurrentlyRecording().containsKey(player)) {
					player.sendMessage(ChatColor.RED + "You cannot do this command while recording!");
					return true;
				}

				if(args.length == 2) {
					record(args[1], player.getDisplayName(), player, player);
					return true;
				}
				if(args.length == 3) {
					record(args[1], args[2], player, player);
					return true;
				}
				if(args.length == 4) {
					record(args[1], args[2], Bukkit.getOfflinePlayer(args[3]), player);
					return true;
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("play") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				PlayTask playTask = new PlayTask(NPCModule.getInstance().getNPC(args[1]), NPCModule.getInstance().getNPC(args[1]).clone());

				playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

				PlayerCapture.getInstance().getRunning().put(args[1], playTask);
				sender.sendMessage(ChatColor.GREEN + "NPC started!");
				return true;
			}

			if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				if(PlayerCapture.getInstance().getRunning().containsKey(args[1])) {
					PlayerCapture.getInstance().getRunning().get(args[1]).cancel();
					PlayerCapture.getInstance().getRunning().get(args[1]).getClone().remove();
					PlayerCapture.getInstance().getRunning().remove(args[1]);

					sender.sendMessage(ChatColor.GREEN + "NPC stopped!");

				} else {
					sender.sendMessage(ChatColor.RED + "NPC not playing!");
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("list")) {
				list(player, sender);
				return true;
			}

			if (args[0].equalsIgnoreCase("remove") && args.length > 2) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				NPC npc = NPCModule.getInstance().getNPC(args[1]);
				npc.remove();

				for(PlayTask playTask : PlayerCapture.getInstance().getRunning().values()) {
					if(playTask.getNpc().equals(npc)) {
						playTask.cancel();
						PlayerCapture.getInstance().getRunning().remove(args[1]);
					}
				}

				NPCModule.getInstance().removeNPC(npc.getName());

				if(Boolean.parseBoolean(args[2])) {
					new File(PlayerCapture.getInstance().getDataFolder() + "\\recordings/" + args[1] + ".yml").delete();

					sender.sendMessage(ChatColor.GREEN + "Animation removed! (Config was removed)");
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + "Animation removed! (Config was not removed)");
				return true;
			}

			if (args[0].equalsIgnoreCase("info") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				sender.sendMessage(info(NPCModule.getInstance().getNPC(args[1])));
				return true;
			}

			if (args[0].equalsIgnoreCase("setskin") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				NPCModule.getInstance().getNPC(args[1]).setSkinID(Bukkit.getOfflinePlayer(args[2]));
				NPCModule.getInstance().getNPC(args[1]).save();
				NPCModule.getInstance().getNPC(args[1]).setUp(true);

				sender.sendMessage(ChatColor.GREEN + "Skin set!");
				return true;
			}

			if (args[0].equalsIgnoreCase("setdisplayname") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				NPCModule.getInstance().getNPC(args[1]).setDisplayName(args[2]);
				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(ChatColor.GREEN + "Name set!");
				return true;
			}

			if (args[0].equalsIgnoreCase("setloop") && args.length > 2) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return true;
				}

				NPCModule.getInstance().getNPC(args[1]).setLoop(Boolean.parseBoolean(args[2]));
				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(ChatColor.GREEN + "Looping set to " + Boolean.parseBoolean(args[2]) + "!");
				return true;
			}

			if (args[0].equalsIgnoreCase("import") && args.length > 1) {
				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC already exists with that name!");
					return true;
				}

				if(!new File(PlayerCapture.getInstance().getDataFolder() + "\\recordings/" + args[1] + ".yml").exists()) {
					sender.sendMessage(ChatColor.RED + "File was not found!");
					return true;
				}

				PlayerCapture.getInstance().loadNPC(args[1]);
				sender.sendMessage(ChatColor.GREEN + "NPC imported!");
				return true;
			}
		}

		sender.sendMessage(helpMessage());

		return true;
	}
}