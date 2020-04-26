package net.buildwarrior.playercapture.npc;

import lombok.Getter;
import net.buildwarrior.playercapture.utils.URLContents;
import org.bukkit.OfflinePlayer;

public class SkinCatch {

	@Getter private String value;
	@Getter private String signature;

	@Getter private final OfflinePlayer player;

	public SkinCatch(String value, String signature, OfflinePlayer skinID) {
		this.value = value;
		this.signature = signature;
		this.player = skinID;
	}

	public SkinCatch(OfflinePlayer skinID) {
		this.player = skinID;

		String urlContents = URLContents.getUrlContents("https://sessionserver.mojang.com/session/minecraft/profile/" +
				skinID.getUniqueId().toString().replaceAll("-", "") + "?unsigned=false");

		try {
			this.value = urlContents.split("\"value\" : \"")[1].split("\",")[0];
			this.signature = urlContents.split("\"signature\" : \"")[1].split("\"")[0];

		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("COUNT NOT FIND PLAYER FOR SKIN");
		}
	}
}