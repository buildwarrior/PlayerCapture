package net.buildwarrior.playercapture.utils;

import lombok.Getter;

public enum Actions {
	MAIN_HAND(0),
	DAMAGE(1),
	LEAVE_BED(2),
	OFF_HAND(3),
	CRITCAL(4),
	MAGIC(5);

	@Getter private  int id;

	Actions(int id) {
		this.id = id;
	}
}