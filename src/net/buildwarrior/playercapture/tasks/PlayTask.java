package net.buildwarrior.playercapture.tasks;

import lombok.Getter;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.npc.NPC;
import net.buildwarrior.playercapture.utils.Actions;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityPose;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
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

		if(!clone.getDisplayName().equals(npc.getDisplayName()) || !clone.getSkinID().equals(npc.getSkinID())) {
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
			clone.setEntityPos(EntityPose.SNEAKING);

		} else if(npc.getFrames().get(frame).isSwimming()) {
			clone.setEntityPos(EntityPose.SWIMMING);

		} else if(npc.getFrames().get(frame).isSleeping()) {
			clone.setEntityPos(EntityPose.SLEEPING);

			clone.getEntityPlayer().e(new BlockPosition(
					npc.getFrames().get(frame).getLocation().getBlockX(),
					npc.getFrames().get(frame).getLocation().getBlockY(),
					npc.getFrames().get(frame).getLocation().getBlockZ()
			));

			tp = new Location(tp.getWorld(), tp.getX() + 0.5f, tp.getY() + 0.6f, tp.getZ() + 0.5f, tp.getYaw(), tp.getPitch());

		} else {
			clone.setEntityPos(EntityPose.STANDING);
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