package net.buildwarrior.playercapture;

import lombok.Getter;
import net.buildwarrior.playercapture.commands.CaptureCommand;
import net.buildwarrior.playercapture.listeners.AnimationListener;
import net.buildwarrior.playercapture.listeners.PlayerJoinLister;
import net.buildwarrior.playercapture.listeners.PlayerQuitListener;
import net.buildwarrior.playercapture.listeners.SleepListener;
import net.buildwarrior.playercapture.listeners.SlotListener;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerCapture extends JavaPlugin {

	@Getter private static PlayerCapture instance;

	@Getter private HashMap<Player, RecordTask> currentlyRecording = new HashMap<>();
	@Getter private HashMap<String, PlayTask> running = new HashMap<>();

	@Getter private HashMap<OfflinePlayer, SkinCatch> skinCatchs = new HashMap<>();

	

	public void onEnable() {
		instance = this;

		if(!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

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
		this.getServer().getPluginManager().registerEvents(new SleepListener(), this);

		this.getCommand("playercapture").setExecutor(new CaptureCommand());

		System.out.println("\033[0;33m[PlayerCapture] \033[0;32mPlayerCapture is enabled!\033[0m");
	}

	public void onDisable() {
		for(PlayTask playTask : running.values()) {
			playTask.getClone().remove();
		}

		System.out.println("\033[0;33m[PlayerCapture] \033[0;31mPlayerCapture is disabled!\033[0m");
	}

	public void loadNPC(String name) {

		Config config = new Config(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + name + ".yml");

		World world = Bukkit.getWorld(config.getData().getString("World"));
		byte b = 0x00;

		if(config.getData().getBoolean("ShowHat")) {
			b += 0x40;
		}
		if(config.getData().getBoolean("ShowLeftArm")) {
			b += 0x08;
		}
		if(config.getData().getBoolean("ShowRightArm")) {
			b += 0x04;
		}
		if(config.getData().getBoolean("ShowLeftLeg")) {
			b += 0x10;
		}
		if(config.getData().getBoolean("ShowRightLeg")) {
			b += 0x20;
		}
		if(config.getData().getBoolean("ShowBody")) {
			b += 0x02;
		}
		if(config.getData().getBoolean("ShowCape")) {
			b += 0x01;
		}

		SkinCatch skinCatch = new SkinCatch(config.getData().getString("Value"), config.getData().getString("Signature"),
				Bukkit.getOfflinePlayer(config.getData().getString("Skin")));

		skinCatchs.put(skinCatch.getPlayer(), skinCatch);

		NPC npc = new NPC(name, world, skinCatch, b);

		npc.setLoop(config.getData().getBoolean("Loop"));
		npc.setDisplayName(config.getData().getString("DisplayName"));

		List<Frame> frames = new ArrayList<>();
		for(String frame : config.getData().getStringList("Frame")) {

			String[] splitFrame = frame.split("/");
			String[] mainHand = splitFrame[9].split(":");
			String[] offHand = splitFrame[10].split(":");
			String[] helmet = splitFrame[11].split(":");
			String[] chestplate = splitFrame[12].split(":");
			String[] leggings = splitFrame[13].split(":");
			String[] boots = splitFrame[14].split(":");

			frames.add(new Frame(new Location(world,
					Double.parseDouble(splitFrame[0].replace("X:", "")),
					Double.parseDouble(splitFrame[1].replace("Y:", "")),
					Double.parseDouble(splitFrame[2].replace("Z:", "")),
					Float.parseFloat(splitFrame[4].replace("Yaw:", "")),
					Float.parseFloat(splitFrame[3].replace("Pitch:", ""))),

					Boolean.parseBoolean(splitFrame[5].replace("Sneak:", "")),
					Boolean.parseBoolean(splitFrame[6].replace("Sleep:", "")),
					Boolean.parseBoolean(splitFrame[7].replace("Fly:", "")),
					Boolean.parseBoolean(splitFrame[8].replace("Swim:", "")),
					Boolean.parseBoolean(splitFrame[15].replace("Hit:", "")),

					new ItemBuilder().type(Material.valueOf(helmet[1])).damage(Integer.parseInt(helmet[2])).build(),
					new ItemBuilder().type(Material.valueOf(chestplate[1])).damage(Integer.parseInt(chestplate[2])).build(),
					new ItemBuilder().type(Material.valueOf(leggings[1])).damage(Integer.parseInt(leggings[2])).build(),
					new ItemBuilder().type(Material.valueOf(boots[1])).damage(Integer.parseInt(boots[2])).build(),
					new ItemBuilder().type(Material.valueOf(mainHand[1])).damage(Integer.parseInt(mainHand[2])).build(),
					new ItemBuilder().type(Material.valueOf(offHand[1])).damage(Integer.parseInt(offHand[2])).build()));
		}
		npc.setFrames(frames);
		npc.setStart(frames.get(0).getLocation());

		npc.setUp();

		NPCModule.getInstance().addNPC(name, npc);

		if(npc.isLoop()) {

			PlayTask playTask = new PlayTask(NPCModule.getInstance().getNPC(name), NPCModule.getInstance().getNPC(name).clone(0));

			playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

			PlayerCapture.getInstance().getRunning().put(name, playTask);
		}
	}

	public SkinCatch createSkin(OfflinePlayer player) {
		SkinCatch skinCatch;
		if(skinCatchs.containsKey(player)) {
			skinCatch = skinCatchs.get(player);

		} else {
			skinCatch = new SkinCatch(player);
			skinCatchs.put(skinCatch.getPlayer(), skinCatch);
		}

		return skinCatch;
	}
}