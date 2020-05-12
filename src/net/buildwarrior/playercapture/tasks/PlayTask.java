package net.buildwarrior.playercapture.tasks;

import lombok.Getter;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.utils.Actions;
import net.buildwarrior.playercapture.utils.NPCPose;
import net.buildwarrior.playercapture.versions.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayTask extends BukkitRunnable {

	@Getter private NPC npc;
	@Getter private NPC clone;
	private int frame;

	public PlayTask(NPC npc, NPC clone) {
		this.npc = npc;
		this.clone = clone;

		clone.spawn();
	}

	public PlayTask(NPC npc, NPC clone, int frame) {
		this.npc = npc;
		this.clone = clone;
		this.frame = frame;

		clone.spawn();
	}

	@Override
	public void run() {

		if(!clone.getDisplayName().equals(npc.getDisplayName()) || !clone.getSkinCatch().equals(npc.getSkinCatch())) {

			cancel();
			clone.remove();
			PlayerCapture.getInstance().getRunning().remove(npc.getName());

			PlayTask playTask = new PlayTask(npc, npc.clone(frame), frame);
			playTask.runTaskTimer(PlayerCapture.getInstance(), 0, 2);

			PlayerCapture.getInstance().getRunning().put(npc.getName(), playTask);
			return;
		}

		Location tp = npc.getFrames().get(frame).getLocation();

		if(npc.getFrames().get(frame).isSneaking()) {
			clone.setEntityPos(NPCPose.SNEAKING);

		} else if(npc.getFrames().get(frame).isSwimming()) {
			clone.setEntityPos(NPCPose.SWIMMING);

		} else if(npc.getFrames().get(frame).isGliding()) {
			clone.setEntityPos(NPCPose.FALL_FLYING);

		} else if(npc.getFrames().get(frame).isSleeping()) {
			clone.setEntityPos(NPCPose.SLEEPING);

			clone.setBed(npc.getFrames().get(frame).getLocation().getBlockX(),
					npc.getFrames().get(frame).getLocation().getBlockY(),
					npc.getFrames().get(frame).getLocation().getBlockZ());

			tp = new Location(tp.getWorld(), tp.getX() + 0.5f, tp.getY() + 0.6f, tp.getZ() + 0.5f, tp.getYaw(), tp.getPitch());

		} else {
			clone.setEntityPos(NPCPose.STANDING);
		}

		clone.teleport(tp);

		if(npc.getFrames().get(frame).isMainHandHit()) {
			clone.playAnimation(Actions.MAIN_HAND);
		}

		clone.setEquipment(npc.getFrames().get(frame).getHelmet(),
				npc.getFrames().get(frame).getChestplate(),
				npc.getFrames().get(frame).getLeggings(),
				npc.getFrames().get(frame).getBoots(),
				npc.getFrames().get(frame).getMainHand(),
				npc.getFrames().get(frame).getOffHand());

		if(npc.getFrames().get(frame).getChat() != null) {
			//TODO over head chat
			String message = npc.getFrames().get(frame).getChat().split("\\(")[1];

			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.substring(0, message.length() -1)));
		}

		frame++;

		if(npc.getFrames().size() == frame) {

			if(npc.isLoop()) {
				frame = 0;
				return;
			}

			cancel();
			clone.remove();

			PlayerCapture.getInstance().getRunning().remove(npc.getName());
		}
	}
}