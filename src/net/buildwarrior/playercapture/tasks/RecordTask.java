package net.buildwarrior.playercapture.tasks;

import lombok.Getter;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.npc.NPCModule;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.listeners.AnimationListener;
import net.buildwarrior.playercapture.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class RecordTask extends BukkitRunnable {

	@Getter private final NPC npc;

	public RecordTask(NPC npc) {
		this.npc = npc;
	}

	@Override
	public void run() {

		if(!npc.getRecordingPlayer().isOnline()) {
			cancel();
			NPCModule.getInstance().removeNPC(npc.getName());
			return;
		}

		boolean hit = false;

		if(AnimationListener.players.contains(npc.getRecordingPlayer())) {
			hit = true;
		}
		AnimationListener.players.remove(npc.getRecordingPlayer());

		ItemStack mainHand = new ItemStack(Material.AIR);
		ItemStack offHand = new ItemStack(Material.AIR);
		ItemStack helmet = new ItemStack(Material.AIR);
		ItemStack chestplate = new ItemStack(Material.AIR);
		ItemStack leggings = new ItemStack(Material.AIR);
		ItemStack boots = new ItemStack(Material.AIR);

		if(npc.getRecordingPlayer().getEquipment().getItemInMainHand().getType() != Material.AIR) mainHand = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getItemInMainHand(), null).build();
		if(npc.getRecordingPlayer().getEquipment().getItemInOffHand().getType() != Material.AIR) offHand = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getItemInOffHand(), null).build();
		if(npc.getRecordingPlayer().getEquipment().getHelmet() != null) helmet = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getHelmet(), null).build();
		if(npc.getRecordingPlayer().getEquipment().getChestplate() != null) chestplate = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getChestplate(), null).build();
		if(npc.getRecordingPlayer().getEquipment().getLeggings() != null) leggings = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getLeggings(), null).build();
		if(npc.getRecordingPlayer().getEquipment().getBoots() != null) boots = new ItemBuilder(npc.getRecordingPlayer().getEquipment().getBoots(), null).build();

		npc.getFrames().add(new Frame(npc.getRecordingPlayer().getLocation(), npc.getRecordingPlayer().isSneaking(), hit,
				helmet, chestplate, leggings, boots, mainHand, offHand));
	}
}