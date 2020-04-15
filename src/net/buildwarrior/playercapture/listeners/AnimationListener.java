package net.buildwarrior.playercapture.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

import java.util.ArrayList;
import java.util.List;

public class AnimationListener implements Listener {

	public static List<Player> players = new ArrayList<>();

	@EventHandler
	public void anim(PlayerAnimationEvent e) {
		Player player = e.getPlayer();

		if(e.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) {
			players.add(player);
		}
	}
}