package net.buildwarrior.playercapture.tasks;

import lombok.Getter;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.utils.Actions;
import net.minecraft.server.v1_14_R1.EntityPose;
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

	@Override
	public void run() {

		clone.teleport(npc.getFrames().get(frame).getLocation());

		if(npc.getFrames().get(frame).isSneaking()) {
			clone.setEntityPos(EntityPose.SNEAKING);
		} else {
			clone.setEntityPos(EntityPose.STANDING);
		}

		if(npc.getFrames().get(frame).isMainHandHit()) {
			clone.playAnimation(Actions.MAIN_HAND);
		}

		clone.setEquipment(npc.getFrames().get(frame).getMainHand(),
				npc.getFrames().get(frame).getOffHand(),
				npc.getFrames().get(frame).getHelmet(),
				npc.getFrames().get(frame).getChestplate(),
				npc.getFrames().get(frame).getLeggings(),
				npc.getFrames().get(frame).getBoots());

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