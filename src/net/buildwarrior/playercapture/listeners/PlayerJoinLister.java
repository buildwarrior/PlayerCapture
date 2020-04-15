package net.buildwarrior.playercapture.listeners;

import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.tasks.PlayTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinLister implements Listener {

	@EventHandler
	public void join(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		for(PlayTask npc : PlayerCapture.getInstance().getRunning().values()) {

			npc.getClone().spawnPer(player);
		}
	}
}