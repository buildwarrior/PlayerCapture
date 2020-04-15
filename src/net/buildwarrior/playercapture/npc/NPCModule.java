package net.buildwarrior.playercapture.npc;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NPCModule {

	private static NPCModule instance;

	private HashMap<String, NPC> npcs = new HashMap<>();

	public void addNPC(String name, Player player, OfflinePlayer skin) {
		this.npcs.put(name, new NPC(name, player, skin));
	}

	public void addNPC(String name, NPC npc) {
		this.npcs.put(name, npc);
	}

	public void removeNPC(String name) {
		this.npcs.remove(name);
	}

	public NPC getNPC(String name) {
		return this.npcs.get(name);
	}

	public boolean isNPC(String name) {
		return this.npcs.containsKey(name);
	}

	public List<NPC> getAll() {
		return new ArrayList<>(this.npcs.values());
	}

	public void clear() {
		this.npcs.clear();
	}

	public static NPCModule getInstance() {
		if (instance == null) {
			instance = new NPCModule();
		}
		return instance;
	}
}