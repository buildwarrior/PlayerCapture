package net.buildwarrior.playercapture.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.HashMap;

public class SleepListener implements Listener {

	public static HashMap<Player, Location> players = new HashMap<>();

	@EventHandler
	public void sleep(PlayerBedEnterEvent e) {
		players.put(e.getPlayer(), e.getBed().getLocation());
	}

	@EventHandler
	public void sleep(PlayerBedLeaveEvent e) {
		players.remove(e.getPlayer());
	}
}