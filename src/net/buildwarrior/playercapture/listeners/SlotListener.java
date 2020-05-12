package net.buildwarrior.playercapture.listeners;

import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.versions.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class SlotListener implements Listener {

	@EventHandler
	public void slot(PlayerItemHeldEvent e) {
		Player player = e.getPlayer();

		if(PlayerCapture.getInstance().getCurrentlyRecording().containsKey(player)) {
			if(e.getNewSlot() == 0) {
				NPC npc = PlayerCapture.getInstance().getCurrentlyRecording().get(player).getNpc();

				PlayerCapture.getInstance().getCurrentlyRecording().get(player).cancel();
				PlayerCapture.getInstance().getCurrentlyRecording().remove(player);

				npc.setUp();
				npc.save();
				player.sendMessage(ChatColor.GREEN + "Recording stopped!");
			}
		}
	}
}