package net.buildwarrior.playercapture;

import org.bukkit.ChatColor;

public class Lang {

	public static String NO_PERMISSION(String perm) {
		return ChatColor.RED + "You need the permissions " + perm + " to use this!";
	}

	public static String UNKNOWN_PLAYER = ChatColor.RED + "Unknown player!";
	public static String PLAYER_NOT_FOUND = ChatColor.RED + "Player not found!";
	public static String PLAYERS_ONLY = ChatColor.RED + "Only players can do this command!";

	public static final String RELOAD = ChatColor.GREEN + "Plugin reloaded!";
	public static final String NPC_EXISTS = ChatColor.RED + "NPC already exists with that name!";
	public static final String ALREADY_RECORDING = ChatColor.RED + "You cannot do this command while recording!";
	public static final String NPC_NOT_FOUND = ChatColor.RED + "NPC not found!";
	public static final String START = ChatColor.GREEN + "NPC started!";
	public static final String END = ChatColor.GREEN + "NPC stopped!";
	public static final String NOT_PLAYING = ChatColor.RED + "NPC not playing!";
	public static String CONFIG(String value) {
		return ChatColor.GREEN + "Animation removed! (" + value + ")";
	}
	public static final String NAME_SET = ChatColor.GREEN + "Name set!";
	public static final String NPC_ALREADY_NAMED = ChatColor.RED + "NPC already exists with that name!";
	public static final String FILE_NOT_FOUND = ChatColor.RED + "File was not found!";
	public static final String IMPORTED = ChatColor.GREEN + "NPC config imported!";
	public static final String ALREADY_RUNNING = ChatColor.RED + "NPC is already running!";
}