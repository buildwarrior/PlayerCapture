package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.PermissionLang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CaptureCommand extends CommandBase implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		execute(sender, args);
		return true;
	}

	private void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission(PermissionLang.USE)) {
			sender.sendMessage(ChatColor.RED + "You need the permission 'playercapture.command.use' to do this command!");
			return;
		}

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (args.length > 0) {

			if(args[0].equalsIgnoreCase("reload")) {

				for(RecordTask recordTask : PlayerCapture.getInstance().getCurrentlyRecording().values()) {
					recordTask.cancel();
					PlayerCapture.getInstance().getCurrentlyRecording().remove(recordTask.getNpc().getRecordingPlayer());
				}

				for(PlayTask playTask : PlayerCapture.getInstance().getRunning().values()) {
					playTask.cancel();
					playTask.getClone().remove();
					PlayerCapture.getInstance().getRunning().remove(playTask.getNpc().getName());
				}

				NPCModule.getInstance().clear();

				File recordings = new File(PlayerCapture.getInstance().getDataFolder() + "\\Recordings");
				for(File name : recordings.listFiles()) {
					PlayerCapture.getInstance().loadNPC(name.getName().replace(".yml", ""));
				}

				sender.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
				return;
			}

			if (args[0].equalsIgnoreCase("record") && args.length > 1) {

				if(!sender.hasPermission(PermissionLang.RECORD)) {
					sender.sendMessage(ChatColor.RED + "You need the permission 'playercapture.command.record' to do this command!");
					return;
				}

				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC already exists with that name!");
					return;
				}

				if(args.length == 5) {
					Player target = Bukkit.getPlayer(args[4]);

					if(target == null) {
						sender.sendMessage(ChatColor.RED + "Unknown player!");
						return;
					}

					record(args[1], args[2], Bukkit.getOfflinePlayer(args[3]), target);
					return;
				}

				if(player == null) {
					sender.sendMessage(ChatColor.RED + "Only players can do this command!");
					return;
				}

				if(PlayerCapture.getInstance().getCurrentlyRecording().containsKey(player)) {
					player.sendMessage(ChatColor.RED + "You cannot do this command while recording!");
					return;
				}

				if(args.length == 2) {
					record(args[1], player.getDisplayName(), player, player);
					return;
				}
				if(args.length == 3) {
					record(args[1], args[2], player, player);
					return;
				}
				if(args.length == 4) {
					record(args[1], args[2], Bukkit.getOfflinePlayer(args[3]), player);
					return;
				}
				return;
			}

			if (args[0].equalsIgnoreCase("play") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				PlayTask playTask = new PlayTask(NPCModule.getInstance().getNPC(args[1]), NPCModule.getInstance().getNPC(args[1]).clone(0));

				playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

				PlayerCapture.getInstance().getRunning().put(args[1], playTask);
				sender.sendMessage(ChatColor.GREEN + "NPC started!");
				return;
			}

			if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				if(PlayerCapture.getInstance().getRunning().containsKey(args[1])) {
					PlayerCapture.getInstance().getRunning().get(args[1]).cancel();
					PlayerCapture.getInstance().getRunning().get(args[1]).getClone().remove();
					PlayerCapture.getInstance().getRunning().remove(args[1]);

					sender.sendMessage(ChatColor.GREEN + "NPC stopped!");

				} else {
					sender.sendMessage(ChatColor.RED + "NPC not playing!");
				}
				return;
			}

			if (args[0].equalsIgnoreCase("list")) {
				list(player, sender);
				return;
			}

			if (args[0].equalsIgnoreCase("remove") && args.length > 2) {
				if(!sender.hasPermission(PermissionLang.REMOVE)) {
					sender.sendMessage(ChatColor.RED + "You need the permission 'playercapture.command.remove' to do this command!");
					return;
				}

				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				NPC npc = NPCModule.getInstance().getNPC(args[1]);
				npc.remove();

				for(PlayTask playTask : PlayerCapture.getInstance().getRunning().values()) {
					if(playTask.getNpc().equals(npc)) {
						playTask.cancel();
						playTask.getClone().remove();
					}
				}
				PlayerCapture.getInstance().getRunning().remove(args[1]);

				NPCModule.getInstance().removeNPC(npc.getName());

				if(Boolean.parseBoolean(args[2])) {
					new File(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + args[1] + ".yml").delete();

					sender.sendMessage(ChatColor.GREEN + "Animation removed! (Config was removed)");
					return;
				}

				try {
					Files.move(
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + args[1] + ".yml"),
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml"));

				} catch(IOException e) {
					e.printStackTrace();
				}

				sender.sendMessage(ChatColor.GREEN + "Animation removed! (Config was moved to archive)");
				return;
			}

			if (args[0].equalsIgnoreCase("info") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				sender.sendMessage(info(NPCModule.getInstance().getNPC(args[1])));
				return;
			}

			if (args[0].equalsIgnoreCase("setskin") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setSkinID(Bukkit.getOfflinePlayer(args[2]));
				NPCModule.getInstance().getNPC(args[1]).save();
				NPCModule.getInstance().getNPC(args[1]).setUp(true);

				sender.sendMessage(ChatColor.GREEN + "Skin set!");
				return;
			}

			if (args[0].equalsIgnoreCase("setdisplayname") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setDisplayName(args[2]);

				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(ChatColor.GREEN + "Name set!");
				return;
			}

			if (args[0].equalsIgnoreCase("setloop") && args.length > 2) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC not found!");
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setLoop(Boolean.parseBoolean(args[2]));
				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(ChatColor.GREEN + "Looping set to " + Boolean.parseBoolean(args[2]) + "!");
				return;
			}

			if (args[0].equalsIgnoreCase("import") && args.length > 1) {
				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(ChatColor.RED + "NPC already exists with that name!");
					return;
				}

				if(!new File(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml").exists()) {
					sender.sendMessage(ChatColor.RED + "File was not found!");
					return;
				}

				try {
					Files.move(
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml"),
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + args[1] + ".yml"));

				} catch(IOException e) {
					e.printStackTrace();
				}

				PlayerCapture.getInstance().loadNPC(args[1]);
				sender.sendMessage(ChatColor.GREEN + "NPC config imported!");
				return;
			}
		}

		sender.sendMessage(helpMessage());
	}
}