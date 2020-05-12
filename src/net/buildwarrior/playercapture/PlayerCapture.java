package net.buildwarrior.playercapture;

import lombok.Getter;
import net.buildwarrior.playercapture.commands.CaptureCommand;
import net.buildwarrior.playercapture.listeners.AnimationListener;
import net.buildwarrior.playercapture.listeners.ChatListener;
import net.buildwarrior.playercapture.listeners.PlayerJoinLister;
import net.buildwarrior.playercapture.listeners.PlayerQuitListener;
import net.buildwarrior.playercapture.listeners.SleepListener;
import net.buildwarrior.playercapture.listeners.SlotListener;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.tasks.PlayTask;
import net.buildwarrior.playercapture.tasks.RecordTask;
import net.buildwarrior.playercapture.utils.ChatType;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.ItemBuilder;
import net.buildwarrior.playercapture.versions.NPC_1_14;
import net.buildwarrior.playercapture.versions.NPC;
import net.buildwarrior.playercapture.versions.NPC_1_15;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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

	@Getter private HashMap<OfflinePlayer, SkinCatch> skinCatchs = new HashMap<>();

	//TODO (Bugs)
	//hitting glich
	//1.15+ sleeping in beds wrong
	//1.15.2 skin layer not working

	//TODO (goals)
	//chatting (over head)
	//taking damage
	//firing projectiles
	//on fire
	//opening chests

	// /pc record <NPC-Name> {
	// DisplayName[NAME TEXT HERE],
	// SkinID[SKIN NAME HERE],
	// Player[PLAYER TO RECORD ON HERE]
	// ChatType[OVER-HEAD | CHAT | NONE],
	// Sneak[TRUE | FALSE]
	// }
	// /pc record test2 {DisplayName[TEST],SkinID[hypixel],Player[Buildwarrior01],ChatType[CHAT],Sneak[False]}

	//TODO (maybe)
	//1.13 support
	//1.12 support

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
		this.getServer().getPluginManager().registerEvents(new ChatListener(), this);

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

		NPC npc = getNPCClass(name, world, skinCatch, b);

		npc.setLoop(config.getData().getBoolean("Loop"));
		npc.setDisplayName(config.getData().getString("DisplayName"));

		List<Frame> frames = new ArrayList<>();
		for(String frame : config.getData().getStringList("Frame")) {

			String[] splits = frame.split("/");

			String[] values = null;
			String rawValue = "";
			String[] armor = null;
			String rawArmor = "";

			if(frame.contains("[")) {
				values = splits[5].replace("[", "").replace("]", "").split(",");
				rawValue = splits[5].replace("[", "").replace("]", "");
			}

			if(frame.contains("{") && values != null) {
				armor = splits[6].replace("{", "").replace("}", "").split(",");
				rawArmor = splits[6].replace("{", "").replace("}", "");

			} else if(frame.contains("{") && values == null) {
				armor = splits[5].replace("{", "").replace("}", "").split(",");
				rawArmor = splits[5].replace("{", "").replace("}", "");
			}

			String chat = null;
			if(values != null && rawValue.contains("Chat")) {
				chat = values[0];
			}

			boolean sneak = false;
			if(values != null && rawValue.contains("Sneak")) { sneak = true; }
			boolean sleep = false;
			if(values != null && rawValue.contains("Sleep")) { sleep = true; }
			boolean gliding = false;
			if(values != null && rawValue.contains("Gliding")) { gliding = true; }
			boolean swimming = false;
			if(values != null && rawValue.contains("Swim")) { swimming = true; }
			boolean hit = false;
			if(values != null && rawValue.contains("Hit")) { hit = true; }

			ItemBuilder helmet = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("Helmet")) {
				String[] h = getAmor(armor, "Helmet").split(":");
				helmet.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			ItemBuilder chestplate = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("Chestplate")) {
				String[] h = getAmor(armor, "Chestplate").split(":");
				chestplate.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			ItemBuilder leggings = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("Leggings")) {
				String[] h = getAmor(armor, "Leggings").split(":");
				leggings.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			ItemBuilder boots = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("Boots")) {
				String[] h = getAmor(armor, "Boots").split(":");
				boots.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			ItemBuilder mainHand = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("MainHand")) {
				String[] h = getAmor(armor, "MainHand").split(":");
				mainHand.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			ItemBuilder offHand = new ItemBuilder().type(Material.AIR);
			if(armor != null && rawArmor.contains("OffHand")) {
				String[] h = getAmor(armor, "OffHand").split(":");
				offHand.type(Material.valueOf(h[1])).damage(Integer.parseInt(h[2]));
			}

			frames.add(new Frame(new Location(world,
					Double.parseDouble(splits[0].replace("X:", "")),
					Double.parseDouble(splits[1].replace("Y:", "")),
					Double.parseDouble(splits[2].replace("Z:", "")),
					Float.parseFloat(splits[4].replace("Yaw:", "")),
					Float.parseFloat(splits[3].replace("Pitch:", ""))),
					sneak,
					sleep,
					gliding,
					swimming,
					hit,

					helmet.build(),
					chestplate.build(),
					leggings.build(),
					boots.build(),
					mainHand.build(),
					offHand.build(),
					chat
			));
		}
		npc.setFrames(frames);
		npc.setStart(frames.get(0).getLocation());
		npc.setChatType(ChatType.valueOf(config.getData().getString("ChatType").toUpperCase()));

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

	public String getAmor(String[] armor, String value) {

		for(String i : armor) {

			if(i.contains(value)) {
				return i;
			}
		}

		return null;
	}

	public NPC getNPCClass(String name, Player player, SkinCatch skin) {
		switch(getServer().getBukkitVersion()) {
			case "1.14.4-R0.1-SNAPSHOT":
			case "1.14.3-R0.1-SNAPSHOT":
			case "1.14.2-R0.1-SNAPSHOT":
			case "1.14.1-R0.1-SNAPSHOT":
			case "1.14-R0.1-SNAPSHOT":
				return new NPC_1_14(name, player, skin);
			case "1.15.2-R0.1-SNAPSHOT":
			case "1.15.1-R0.1-SNAPSHOT":
			case "1.15-R0.1-SNAPSHOT":
				return new NPC_1_15(name, player, skin);
		}

		System.out.println("\033[0;33m[PlayerCapture] \033[0;31mCould not load version: " + getServer().getBukkitVersion() + "\033[0m");
		return null;
	}

	public NPC getNPCClass(String name, World world, SkinCatch skin, byte b) {
		switch(getServer().getBukkitVersion()) {
			case "1.14.4-R0.1-SNAPSHOT":
			case "1.14.3-R0.1-SNAPSHOT":
			case "1.14.2-R0.1-SNAPSHOT":
			case "1.14.1-R0.1-SNAPSHOT":
			case "1.14-R0.1-SNAPSHOT":
				return new NPC_1_14(name, world, skin, b);
			case "1.15.2-R0.1-SNAPSHOT":
			case "1.15.1-R0.1-SNAPSHOT":
			case "1.15-R0.1-SNAPSHOT":
				return new NPC_1_15(name, world, skin, b);
		}

		System.out.println("\033[0;33m[PlayerCapture] \033[0;31mCould not load version: " + getServer().getBukkitVersion() + "\033[0m");
		return null;
	}
}