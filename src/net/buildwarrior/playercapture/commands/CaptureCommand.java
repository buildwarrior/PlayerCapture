package net.buildwarrior.playercapture.commands;

import net.buildwarrior.playercapture.utils.ChatType;
import net.buildwarrior.playercapture.utils.Lang;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.PermissionLang;
import net.buildwarrior.playercapture.versions.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
			sender.sendMessage(Lang.NO_PERMISSION(PermissionLang.USE));
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

				sender.sendMessage(Lang.RELOAD);
				return;
			}

			if (args[0].equalsIgnoreCase("record") && args.length > 1) {

				if(!sender.hasPermission(PermissionLang.RECORD)) {
					sender.sendMessage(Lang.NO_PERMISSION(PermissionLang.RECORD));
					return;
				}

				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_EXISTS);
					return;
				}

				//{DisplayName[
				// BOB
				// ],SkinID[BOB],Player[BOB],ChatType[CHAT],Sneak[TRUE]}

				if(args.length == 3) {
					Player target = null;
					String displayName = null;
					SkinCatch skin = null;
					boolean sneak = true;
					ChatType chatType = ChatType.NONE;

					if(args[2].contains("Player")) {
						target = Bukkit.getPlayer(args[2].split("Player\\[")[1].split("]")[0]);

						if(target == null) {
							sender.sendMessage(Lang.PLAYER_NOT_FOUND);
							return;
						}
					}

					if(args[2].contains("ChatType")) {
						chatType = ChatType.valueOf(args[2].split("ChatType\\[")[1].split("]")[0].toUpperCase());
					}

					if(args[2].contains("Sneak")) {
						sneak = Boolean.parseBoolean(args[2].split("Sneak\\[")[1].split("]")[1]);
					}

					if(args[2].contains("DisplayName")) {
						displayName = args[2].split("DisplayName\\[")[1].split("]")[0];
					}

					if(args[2].contains("SkinID")) {
						OfflinePlayer skinPlayer = Bukkit.getOfflinePlayer(args[2].split("SkinID\\[")[1].split("]")[0]);

						skin = new SkinCatch(skinPlayer);
					}

					if(target == null) {

						if(player == null) {
							sender.sendMessage(Lang.PLAYERS_ONLY);
							return;
						}

						target = player;
					}

					if(displayName == null) {
						displayName = target.getDisplayName();
					}

					if(skin == null) {
						skin = PlayerCapture.getInstance().createSkin(player);
					}

					record(args[1], displayName, skin, target, chatType, sneak);
					return;
				}

				if(player == null) {
					sender.sendMessage(Lang.PLAYERS_ONLY);
					return;
				}

				if(args.length == 2) {
					record(args[1], player.getDisplayName(), PlayerCapture.getInstance().createSkin(player), player, ChatType.NONE, true);
					return;
				}
				return;
			}

			if (args[0].equalsIgnoreCase("play") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				if(PlayerCapture.getInstance().getRunning().containsKey(args[1])) {
					sender.sendMessage(Lang.ALREADY_RUNNING);
					return;
				}

				PlayTask playTask = new PlayTask(NPCModule.getInstance().getNPC(args[1]), NPCModule.getInstance().getNPC(args[1]).clone(0));

				playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

				PlayerCapture.getInstance().getRunning().put(args[1], playTask);
				sender.sendMessage(Lang.START);
				return;
			}

			if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				if(PlayerCapture.getInstance().getRunning().containsKey(args[1])) {
					PlayerCapture.getInstance().getRunning().get(args[1]).cancel();
					PlayerCapture.getInstance().getRunning().get(args[1]).getClone().remove();
					PlayerCapture.getInstance().getRunning().remove(args[1]);

					sender.sendMessage(Lang.END);

				} else {
					sender.sendMessage(Lang.NOT_PLAYING);
				}
				return;
			}

			if (args[0].equalsIgnoreCase("list")) {
				list(player, sender);
				return;
			}

			if (args[0].equalsIgnoreCase("remove") && args.length > 2) {
				if(!sender.hasPermission(PermissionLang.REMOVE)) {
					sender.sendMessage(Lang.NO_PERMISSION(PermissionLang.REMOVE));
					return;
				}

				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
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

					sender.sendMessage(Lang.CONFIG("Config was removed"));
					return;
				}

				try {
					Files.move(
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + args[1] + ".yml"),
							Paths.get(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml"));

				} catch(IOException e) {
					e.printStackTrace();
				}

				sender.sendMessage(Lang.CONFIG("Config was moved to archive"));
				return;
			}

			if (args[0].equalsIgnoreCase("info") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				sender.sendMessage(info(NPCModule.getInstance().getNPC(args[1])));
				return;
			}

			if (args[0].equalsIgnoreCase("setskin") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				SkinCatch skinCatch = PlayerCapture.getInstance().createSkin(Bukkit.getOfflinePlayer(args[2]));

				if(skinCatch.getValue() == null) {
					sender.sendMessage(Lang.PLAYER_NOT_FOUND);
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setSkinCatch(skinCatch);
				NPCModule.getInstance().getNPC(args[1]).save();
				NPCModule.getInstance().getNPC(args[1]).setUp();

				sender.sendMessage(ChatColor.GREEN + "Skin set!");
				return;
			}

			if (args[0].equalsIgnoreCase("setdisplayname") && args.length > 1) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setDisplayName(args[2]);

				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(Lang.NAME_SET);
				return;
			}

			if (args[0].equalsIgnoreCase("setloop") && args.length > 2) {
				if(!NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_NOT_FOUND);
					return;
				}

				NPCModule.getInstance().getNPC(args[1]).setLoop(Boolean.parseBoolean(args[2]));
				NPCModule.getInstance().getNPC(args[1]).save();

				sender.sendMessage(ChatColor.GREEN + "Looping set to " + Boolean.parseBoolean(args[2]) + "!");
				return;
			}

			if (args[0].equalsIgnoreCase("import") && args.length > 1) {
				if(NPCModule.getInstance().isNPC(args[1])) {
					sender.sendMessage(Lang.NPC_ALREADY_NAMED);
					return;
				}

				if(!new File(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml").exists()) {
					sender.sendMessage(Lang.FILE_NOT_FOUND);
					return;
				}


				convert(args[1]);

				new File(PlayerCapture.getInstance().getDataFolder() + "\\Archive/" + args[1] + ".yml").delete();

				PlayerCapture.getInstance().loadNPC(args[1]);
				sender.sendMessage(Lang.IMPORTED);
				return;
			}
		}

		sender.sendMessage(helpMessage());
	}
}