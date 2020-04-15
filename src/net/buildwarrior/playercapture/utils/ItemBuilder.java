package net.buildwarrior.playercapture.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @ItemBuilder allows you to build itemstacks in 1 line
 */
public class ItemBuilder {

	@Getter private Material material = null;
	@Getter private int amount = 1;
	@Getter private String displayName = null;
	@Getter private List<String> lore = null;
	@Getter private Map<Enchantment, Integer> enchantments = new HashMap<>();
	@Getter private String skullName = null;
	@Getter private Color leatherArmorColor = null;
	@Getter private List<Pattern> patterns = new ArrayList<>();
	@Getter private String base64;
	@Getter private float damageValue;

	private ItemMeta itemMeta = null;

	public ItemBuilder() {}

	public ItemBuilder(ItemStack itemStack, String base64) {
		this.material = itemStack.getType();
		this.amount = itemStack.getAmount();
		this.displayName = itemStack.getItemMeta().getDisplayName();
		this.lore = itemStack.getItemMeta().getLore();
		this.damageValue = itemStack.getDurability();

		for(Map.Entry<Enchantment, Integer> ench : itemStack.getItemMeta().getEnchants().entrySet()) {
			this.enchantments.put(ench.getKey(), ench.getValue());
		}

		if(this.material == Material.PLAYER_HEAD) {
			this.itemMeta = itemStack.getItemMeta();

			SkullMeta skullMeta = ((SkullMeta) this.itemMeta);

			this.skullName = skullMeta.getOwner();
			this.base64 = base64;
		}

		if(this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_CHESTPLATE || this.material == Material.LEATHER_LEGGINGS ||
				this.material == Material.LEATHER_BOOTS) {

			LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
			this.leatherArmorColor = leatherArmorMeta.getColor();
		}

		if(this.material == Material.BLACK_BANNER || this.material == Material.BLUE_BANNER || this.material == Material.BROWN_BANNER || this.material == Material.CYAN_BANNER ||
				this.material == Material.GRAY_BANNER || this.material == Material.GREEN_BANNER || this.material == Material.LIGHT_BLUE_BANNER || this.material == Material.LIGHT_GRAY_BANNER ||
				this.material == Material.LIME_BANNER || this.material == Material.MAGENTA_BANNER || this.material == Material.ORANGE_BANNER || this.material == Material.PINK_BANNER ||
				this.material == Material.PURPLE_BANNER || this.material == Material.RED_BANNER || this.material == Material.WHITE_BANNER || this.material == Material.YELLOW_BANNER) {

			BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
			this.patterns = bannerMeta.getPatterns();
		}
	}

	/**
	 * set the damage value of the item
	 *
	 * @param damageValue damage value of the item
	 * @return this
	 */
	public ItemBuilder damage(float damageValue) {
		this.damageValue = damageValue;

		return this;
	}

	/**
	 * set the material of the item
	 *
	 * @param material what block/item
	 * @return this
	 */
	public ItemBuilder type(Material material) {
		this.material = material;

		return this;
	}

	/**
	 * set the amount of the item
	 *
	 * @param amount number of items in the stack
	 * @return this
	 */
	public ItemBuilder amount(Integer amount) {
		this.amount = amount;

		return this;
	}

	/**
	 * set the display name of the item
	 *
	 * @param displayName name of the item
	 * @return this
	 */
	public ItemBuilder name(String displayName) {
		this.displayName = displayName;

		return this;
	}

	/**
	 * set the lore of the item
	 *
	 * @param strings target lore
	 * @return this
	 */
	public ItemBuilder lore(String... strings) {
		this.lore = Arrays.asList(strings);

		return this;
	}

	/**
	 * set the lore of the item with a list
	 *
	 * @param strings target lore
	 * @return this
	 */
	public ItemBuilder lore(List<String> strings) {
		this.lore = strings;

		return this;
	}

	/**
	 * add an enchantment to the item
	 *
	 * @param enchantment target type
	 * @param level target level
	 * @return this
	 */
	public ItemBuilder enchant(Enchantment enchantment, Integer level) {
		this.enchantments.put(enchantment, level);

		return this;
	}

	/**
	 * set the enchantments list
	 *
	 * @param enchantments list of enchantments
	 * @return this
	 */
	public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;

		return this;
	}

	/**
	 * set the color of the leather armor
	 *
	 * @param color target color
	 * @return this
	 */
	public ItemBuilder leatherArmor(Color color) {
		this.leatherArmorColor = color;

		return this;
	}

	/**
	 * set the pattens of the banner
	 *
	 * @param patterns target patterns
	 * @return this
	 */
	public ItemBuilder pattens(List<Pattern> patterns) {
		this.patterns = patterns;

		return this;
	}

	/**
	 * set skull with player name
	 *
	 * @param name target name
	 * @return this
	 */
	public ItemBuilder skull(String name) {
		this.material = Material.PLAYER_HEAD;
		this.skullName = name;

		return this;
	}

	/***
	 * Create 64base skull head
	 *
	 * @param base64 skull 64base string
	 * @return this
	 */
	public ItemBuilder skull64Base(String base64) {
		this.base64 = base64;

		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		PropertyMap propertyMap = profile.getProperties();
		if (propertyMap == null) {
			throw new IllegalStateException("Profile doesn't contain a property map");
		}
		propertyMap.put("textures", new Property("textures", base64));

		type(Material.PLAYER_HEAD);

		ItemStack head = new ItemStack(this.material, this.amount);
		ItemMeta headMeta = head.getItemMeta();

		Class<?> headMetaClass = headMeta.getClass();

		try {
			Field field = headMetaClass.getDeclaredField("profile");
			field.setAccessible(true);
			field.set(headMeta, profile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.itemMeta = headMeta;

		return this;
	}

	/**
	 * Build the item from this class
	 *
	 * @return ItemStack
	 */
	public ItemStack build() {
		if(material.equals(Material.AIR)) {
			return new ItemStack(Material.AIR);
		}

		ItemStack itemStack = new ItemStack(this.material, this.amount);

		itemStack.setDurability((short) this.damageValue);

		if(this.itemMeta == null) {
			this.itemMeta = itemStack.getItemMeta();
		}

		if (this.displayName != null) {
			this.itemMeta.setDisplayName(this.displayName);
		}

		if (this.lore != null) {
			this.itemMeta.setLore(this.lore);
		}

		if (this.skullName != null) {
			((SkullMeta) this.itemMeta).setOwner(this.skullName);
		}

		assert this.itemMeta != null;
		this.itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		this.itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		this.itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		this.itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		this.itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		this.itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		this.itemMeta.setUnbreakable(true);

		itemStack.setItemMeta(this.itemMeta);

		if (this.enchantments != null) {
			for (Enchantment enchantment : this.enchantments.keySet()) {
				int level = this.enchantments.get(enchantment);
				itemStack.addUnsafeEnchantment(enchantment, level);
			}
		}

		if (leatherArmorColor != null) {
			LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
			leatherArmorMeta.setColor(this.leatherArmorColor);

			itemStack.setItemMeta(leatherArmorMeta);
		}

		if (!patterns.isEmpty()) {
			BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();

			for (Pattern pattern : patterns) {
				bannerMeta.addPattern(pattern);
			}

			itemStack.setItemMeta(bannerMeta);
		}

		return itemStack;
	}
}