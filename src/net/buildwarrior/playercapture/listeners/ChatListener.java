package net.buildwarrior.playercapture.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class ChatListener implements Listener {

	public static HashMap<Player, String> chat = new HashMap<>();

	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();

		if(chat.containsKey(player)) {
			chat.replace(player, e.getMessage());
		} else {
			chat.put(player, e.getMessage());
		}
	}
}