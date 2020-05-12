package net.buildwarrior.playercapture.versions;

import net.buildwarrior.playercapture.npc.SkinCatch;
import net.buildwarrior.playercapture.utils.Actions;
import net.buildwarrior.playercapture.utils.ChatType;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.NPCPose;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NPC {

	void setLoop(boolean value);

	void setDisplayName(String value);

	void setFrames(List<Frame> values);

	void setStart(Location location);

	void sendClickableMessage(Player player, String text, String clickableText, String runCommand);

	void setSkinCatch(SkinCatch value);

	void teleport(Location value);

	void playAnimation(Actions value);

	void setEquipment(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainHand, ItemStack offHand);

	NPC clone(int frame);

	void setChatType(ChatType value);

	void spawnPer(Player value);

	void setEntityPos(NPCPose value);

	void setBed(int x, int y, int z);

	void setUp();

	void save();

	void spawn();

	void remove();

	boolean isLoop();

	SkinCatch getSkinCatch();

	String getName();

	String getDisplayName();

	World getWorld();

	List<Frame> getFrames();

	Player getRecordingPlayer();
}