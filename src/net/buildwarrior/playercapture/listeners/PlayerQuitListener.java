package net.buildwarrior.playercapture.listeners;

import net.buildwarrior.playercapture.PlayerCapture;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void quit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		PlayerCapture.getInstance().getCurrentlyRecording().remove(player);
	}
}