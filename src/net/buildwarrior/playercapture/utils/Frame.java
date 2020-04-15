package net.buildwarrior.playercapture.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Frame {

	@Getter private final Location location;

	@Getter private final boolean sneaking;
	@Getter private final boolean flying;
	@Getter private final boolean swimming;
	@Getter private final boolean sleeping;

	@Getter private final boolean mainHandHit;

	@Getter private final ItemStack helmet;
	@Getter private final ItemStack chestplate;
	@Getter private final ItemStack leggings;
	@Getter private final ItemStack boots;
	@Getter private final ItemStack mainHand;
	@Getter private final ItemStack offHand;

	public Frame(Location location, boolean sneaking, boolean sleeping, boolean flying, boolean swimming, boolean mainHandHit,
				 ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainHand, ItemStack offHand) {
		this.location = location;
		this.sneaking = sneaking;
		this.sleeping = sleeping;
		this.flying = flying;
		this.swimming = swimming;
		this.mainHandHit = mainHandHit;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
		this.mainHand = mainHand;
		this.offHand = offHand;
	}
}