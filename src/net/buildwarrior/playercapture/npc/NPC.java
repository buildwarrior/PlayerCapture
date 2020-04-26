package net.buildwarrior.playercapture.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.buildwarrior.playercapture.PlayerCapture;
import net.buildwarrior.playercapture.Config;
import net.buildwarrior.playercapture.utils.Frame;
import net.buildwarrior.playercapture.utils.URLContents;
import net.buildwarrior.playercapture.utils.Actions;
import net.minecraft.server.v1_14_R1.DataWatcherRegistry;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityPose;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPC {

	@Getter private final String name;
	@Getter private final Player recordingPlayer;

	@Getter @Setter private String displayName;
	@Getter private World world;
	@Getter @Setter	private SkinCatch skinCatch;
	@Getter @Setter private boolean loop;

	@Setter @Getter private List<Frame> frames = new ArrayList<>();

	@Getter private EntityPlayer entityPlayer;

	@Setter private Location start;

	private byte b;

	NPC(String name, Player recordingPlayer, SkinCatch skinCatch) {
		this.name = name;
		this.recordingPlayer = recordingPlayer;
		this.world = recordingPlayer.getWorld();
		this.skinCatch = skinCatch;

		this.start = recordingPlayer.getLocation();
		this.b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
	}

	public NPC(String name, World world, SkinCatch skinCatch, byte b) {
		this.name = name;
		this.recordingPlayer = null;
		this.world = world;
		this.skinCatch = skinCatch;
		this.b = b;
	}

	public NPC clone(int frame) {
		NPC npc = new NPC(name, world, skinCatch, b);
		npc.setDisplayName(this.displayName);
		npc.setLoop(this.loop);
		npc.setFrames(this.frames);
		npc.setStart(frames.get(frame).getLocation());
		npc.setUp();
		return npc;
	}

	public void setUp() {
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer worldServer = ((CraftWorld) this.world).getHandle();

		GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', displayName));

		if(displayName.equalsIgnoreCase("-")) {
			try {
				Field f = profile.getClass().getDeclaredField("name");
				f.setAccessible(true);
				f.set(profile, "");

			} catch(NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		profile.getProperties().put("textures", new Property("textures", skinCatch.getValue(), skinCatch.getSignature()));

		this.entityPlayer = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));

		this.entityPlayer.setLocation(start.getX(), start.getY(), start.getZ(), 0, 0);

		//0x40 : Hat
		//0x20 right leg
		//0x10 left leg
		//0x08 left arm
		//0x04 right arm
		//0x02 body
		//0x01 cape
		this.entityPlayer.getDataWatcher().set(DataWatcherRegistry.a.a(15), b);
	}

	public void save() {
		Config config = new Config(PlayerCapture.getInstance().getDataFolder() + "\\Recordings/" + name + ".yml");
		List<String> list = new ArrayList<>();

		config.getData().set("DisplayName", this.displayName);
		config.getData().set("World", this.world.getName());
		config.getData().set("Skin", this.skinCatch.getPlayer().getName());
		config.getData().set("Loop", this.loop);
		config.getData().set("Value", this.skinCatch.getValue());
		config.getData().set("Signature", this.skinCatch.getSignature());

		config.getData().set("ShowHat", true);
		config.getData().set("ShowLeftArm", true);
		config.getData().set("ShowRightArm", true);
		config.getData().set("ShowLeftLeg", true);
		config.getData().set("ShowRightLeg", true);
		config.getData().set("ShowBody", true);
		config.getData().set("ShowCape", true);

		for(Frame frame : getFrames()) {
			list.add(
					"X:" + frame.getLocation().getX() +
					"/Y:" + frame.getLocation().getY() +
					"/Z:" + frame.getLocation().getZ() +
					"/Pitch:" + frame.getLocation().getPitch() +
					"/Yaw:" + frame.getLocation().getYaw() +
					"/Sneak:" + frame.isSneaking() +
					"/Sleep:" + frame.isSleeping() +
					"/Fly:" + frame.isFlying() +
					"/Swim:" + frame.isSwimming() +
					"/MainHand:" + frame.getMainHand().getType() + ":" + frame.getMainHand().getDurability() +
					"/OffHand:" + frame.getOffHand().getType() + ":" + frame.getOffHand().getDurability() +
					"/Helmet:" + frame.getHelmet().getType() + ":" + frame.getHelmet().getDurability() +
					"/Chestplate:" + frame.getChestplate().getType() + ":" + frame.getChestplate().getDurability() +
					"/Leggings:" + frame.getLeggings().getType() + ":" + frame.getLeggings().getDurability() +
					"/Boots:" + frame.getBoots().getType() + ":" + frame.getBoots().getDurability() +
					"/Hit:" + frame.isMainHandHit());
		}

		config.getData().set("Frame", list);
		config.saveConfig();
	}

	public void spawnPer(Player player) {
		PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(this.entityPlayer, (byte) ((0 * 256.0F) / 360.0F));

		PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;

		PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(this.entityPlayer);

		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.entityPlayer));
		connection.sendPacket(packetPlayOutNamedEntitySpawn);
		connection.sendPacket(rotation);

		PacketPlayOutPlayerInfo playOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);

		new BukkitRunnable() {
			@Override
			public void run() {
				connection.sendPacket(playOutPlayerInfo);
			}
		}.runTaskLater(PlayerCapture.getInstance(), 100);
	}

	public void spawn() {
		for(Player pp : Bukkit.getOnlinePlayers()) {
			spawnPer(pp);
		}
	}

	public void remove() {
		for(Player pp : Bukkit.getOnlinePlayers()) {

			PacketPlayOutEntityDestroy kill = new PacketPlayOutEntityDestroy(entityPlayer.getId());
			((CraftPlayer)pp).getHandle().playerConnection.sendPacket(kill);
		}
	}

	public void setEntityPos(EntityPose value) {
		this.entityPlayer.getDataWatcher().set(DataWatcherRegistry.s.a(6), value);

		PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), false);

		for(Player pp : Bukkit.getOnlinePlayers()) {

			((CraftPlayer)pp).getHandle().playerConnection.sendPacket(packet);
		}
	}

	public void teleport(Location location) {
		PacketPlayOutEntity.PacketPlayOutEntityLook packetPlayOutEntityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entityPlayer.getId(), (byte)(location.getYaw() * 256.0f / 360.0f), (byte)location.getPitch(), true);

		for(Player pp : Bukkit.getOnlinePlayers()) {

			PlayerConnection playerConnection = ((CraftPlayer)pp).getHandle().playerConnection;
			this.entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			playerConnection.sendPacket(new PacketPlayOutEntityTeleport(this.entityPlayer));
			playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(this.entityPlayer, (byte)(location.getYaw() * 256.0f / 360.0f)));
			playerConnection.sendPacket(packetPlayOutEntityLook);
		}
	}

	public void playAnimation(Actions action) {
		for(Player pp : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)pp).getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(this.entityPlayer, action.getId()));
		}
	}

	public void setEquipment(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainHand, ItemStack offHand) {
		for(Player pp : Bukkit.getOnlinePlayers()) {

			PlayerConnection playerConnection = ((CraftPlayer)pp).getHandle().playerConnection;
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(mainHand)));
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(offHand)));
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.FEET, CraftItemStack.asNMSCopy(boots)));
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(leggings)));
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(chestplate)));
			playerConnection.sendPacket(new PacketPlayOutEntityEquipment(this.entityPlayer.getId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet)));
		}
	}
}