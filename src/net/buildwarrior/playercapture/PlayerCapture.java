package net.buildwarrior.playercapture;

import lombok.Getter;
import net.buildwarrior.playercapture.commands.CaptureCommand;
import net.buildwarrior.playercapture.listeners.AnimationListener;
import net.buildwarrior.playercapture.listeners.PlayerJoinLister;
import net.buildwarrior.playercapture.listeners.PlayerQuitListener;
import net.buildwarrior.playercapture.listeners.SlotListener;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerCapture extends JavaPlugin {

	@Getter private static PlayerCapture instance;

	@Getter private HashMap<Player, RecordTask> currentlyRecording = new HashMap<>();
	@Getter private HashMap<String, PlayTask> running = new HashMap<>();

	//TODO: Support 2nd lay skin option
	//TODO: Support flying, swimming and sleeping
	//TODO: Support opening chests
	//TODO: Support shooting projectile
	//TODO: add more permissions

	//cash skins to fix recording command
	//option for no name tag
	//tab support
	//note folder being made outside of datafolder

	public void onEnable() {
		instance = this;

		File recordings = new File(getDataFolder() + "\\Recordings");
		if(!recordings.exists()) {
			recordings.mkdir();
		}

		File archive = new File(getDataFolder() + "\\Archive");
		if(!archive.exists()) {
			archive.mkdir();
		}

		for(File name : recordings.listFiles()) {
			loadNPC(name.getName().replace(".yml", ""));
		}

		this.getServer().getPluginManager().registerEvents(new AnimationListener(), this);
		this.getServer().getPluginManager().registerEvents(new SlotListener(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinLister(), this);

		this.getCommand("playercapture").setExecutor(new CaptureCommand());

		System.out.println("\033[0;33m[MovementCapture] \033[0;32mMovementCapture is enabled!\033[0m");
	}

	public void onDisable() {
		for(PlayTask playTask : running.values()) {
			playTask.getClone().remove();
		}

		System.out.println("\033[0;33m[MovementCapture] \033[0;31mMovementCapture is disabled!\033[0m");
	}

	public void loadNPC(String name) {

		Config config = new Config(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + name + ".yml");

		World world = Bukkit.getWorld(config.getData().getString("World"));

		NPC npc = new NPC(name, world, Bukkit.getOfflinePlayer(config.getData().getString("Skin")),
				config.getData().getString("Value"), config.getData().getString("Signature"));

		npc.setLoop(config.getData().getBoolean("Loop"));
		npc.setDisplayName(config.getData().getString("DisplayName"));

		List<Frame> frames = new ArrayList<>();
		for(String frame : config.getData().getStringList("Frame")) {

			String[] splitFrame = frame.split("/");
			String[] mainHand = splitFrame[6].split(":");
			String[] offHand = splitFrame[7].split(":");
			String[] helmet = splitFrame[8].split(":");
			String[] chestplate = splitFrame[9].split(":");
			String[] leggings = splitFrame[10].split(":");
			String[] boots = splitFrame[11].split(":");

			frames.add(new Frame(new Location(world,
					Double.parseDouble(splitFrame[0].replace("X:", "")),
					Double.parseDouble(splitFrame[1].replace("Y:", "")),
					Double.parseDouble(splitFrame[2].replace("Z:", "")),
					Float.parseFloat(splitFrame[4].replace("Yaw:", "")),
					Float.parseFloat(splitFrame[3].replace("Pitch:", ""))),

					Boolean.parseBoolean(splitFrame[5].replace("Sneak:", "")),
					Boolean.parseBoolean(splitFrame[12].replace("Hit:", "")),

					new ItemBuilder().type(Material.valueOf(mainHand[1])).damage(Integer.parseInt(mainHand[2])).build(),
					new ItemBuilder().type(Material.valueOf(offHand[1])).damage(Integer.parseInt(offHand[2])).build(),
					new ItemBuilder().type(Material.valueOf(helmet[1])).damage(Integer.parseInt(helmet[2])).build(),
					new ItemBuilder().type(Material.valueOf(chestplate[1])).damage(Integer.parseInt(chestplate[2])).build(),
					new ItemBuilder().type(Material.valueOf(leggings[1])).damage(Integer.parseInt(leggings[2])).build(),
					new ItemBuilder().type(Material.valueOf(boots[1])).damage(Integer.parseInt(boots[2])).build()));
		}
		npc.setFrames(frames);
		npc.setStart(frames.get(0).getLocation());

		npc.setUp(false);

		NPCModule.getInstance().addNPC(name, npc);

		if(npc.isLoop()) {

			PlayTask playTask = new PlayTask(NPCModule.getInstance().getNPC(name), NPCModule.getInstance().getNPC(name).clone());

			playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

			PlayerCapture.getInstance().getRunning().put(name, playTask);
		}
	}
}