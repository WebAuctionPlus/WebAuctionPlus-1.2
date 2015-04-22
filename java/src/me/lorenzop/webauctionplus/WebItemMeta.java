package me.lorenzop.webauctionplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;


public class WebItemMeta {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private WebItemMeta() {}


	// encode enchantments for database storage
	public static String encodeEnchants(final ItemStack stack, final Player player) {
		if(stack  == null) throw new NullPointerException();
		final Map<Enchantment, Integer> enchants = getItemEnchants(stack, player);
		final boolean hasEnchants = !enchants.isEmpty();
		boolean lore_set = false;
		final StringBuilder str = new StringBuilder();
		if(hasEnchants) {
			// convert enchantments to int id for sorting
			final Map<Integer, Integer> intMap = new HashMap<Integer, Integer>();
			for(final Entry<Enchantment, Integer> entry : enchants.entrySet()) {
				intMap.put(
					getEnchId(entry.getKey()),
					entry.getValue()
				);
			}
			// sort by enchantment id
			final SortedSet<Integer> sorted = new TreeSet<Integer>(intMap.keySet());
			// append enchantments to string
			for(final Integer id : sorted) {
				final int level = intMap.get(id);
				if(str.length() > 0)
					str.append(",");
				str.append(Integer.toString(id));
				str.append(":");
				str.append(Integer.toString(level));
			}
		}
		if(stack.hasItemMeta()) {
			final ItemMeta meta = stack.getItemMeta();
			// append display name to string
			if(meta.hasDisplayName()) {
				if(str.length() > 0)
					str.append(",");
				str.append("<NAME>:");
				str.append( meta.getDisplayName().replace(":", "").replace(",", "").trim() );
			}
			// append lore to string
			if(meta.hasLore()) {
				if(str.length() > 0)
					str.append(",");
				str.append("<LORE>:");
				final String[] lore = meta.getLore().toArray(new String[0]);
				for(final String line : lore) {
					if(line == null) continue;
					if(lore_set)
						str.append("\n");
					str.append( line.replace(":", "").replace(",", "").replace("\n", "").trim() );
					lore_set = true;
				}
			}
		}
		if(str.length() == 0)
			return null;
		return str.toString();
	}


	// decode enchantments from string
	public static void decodeEnchants(final ItemStack stack, final Player player, final String str) {
		if(str == null || str.isEmpty()) return;
		final Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		// parse string
		for(String part : str.split(",")) {
			if(part==null || part.isEmpty()) continue;
			String[] split = part.split(":");
			if(split.length != 2) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Invalid enchantment data: "+part);
				continue;
			}
			// custom name
			if(split[0].equals("<NAME>")) {
				final ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(split[1].trim());
				stack.setItemMeta(meta);
				continue;
			}
			// lore
			if(split[0].equals("<LORE>")) {
				final ItemMeta meta = stack.getItemMeta();
				List<String> lore = meta.getLore();
				if(lore == null) lore = new ArrayList<String>();
				for(final String s : split[1].split("\n"))
					lore.add(s.trim());
				meta.setLore(lore);
				stack.setItemMeta(meta);
				continue;
			}
			// enchantment
			int enchId = -1;
			int level  = -1;
			try {
				enchId = Integer.valueOf(split[0]);
				level  = Integer.valueOf(split[1]);
			} catch(Exception ignore) {}
			if(enchId < 0 || level < 1) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Invalid enchantment data: "+part);
				continue;
			}
			final Enchantment enchantment = getEnchById(enchId);
			if(enchantment == null) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Invalid enchantment id: "+part);
				continue;
			}
			// add enchantment to map
			enchants.put(enchantment, level);
		}
		// apply enchantments to item
		if(enchants != null && !enchants.isEmpty())
			applyItemEnchants(stack, player, enchants);
	}


	// safely get enchantments from an item
	private static Map<Enchantment, Integer> getItemEnchants(final ItemStack stack, final Player player) {
		if(stack == null) return null;
		final Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		Map<Enchantment, Integer> tmpEnchants = null;

		// item meta
		if(stack.hasItemMeta()) {
			final ItemMeta meta = stack.getItemMeta();
			// enchanted book
			if(meta instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
				tmpEnchants = bookMeta.getStoredEnchants();
			}
		}
		// normal item
		if(tmpEnchants == null)
			tmpEnchants = stack.getEnchantments();

		// check safe enchantments
		boolean removedUnsafe = false;
		for(Entry<Enchantment, Integer> entry : tmpEnchants.entrySet()) {
			final Enchantment ench = entry.getKey();
			final int level = checkSafeEnchantment(stack, ench, entry.getValue());
			if(level < 1) {
				removedUnsafe = true;
				continue;
			}
			enchants.put(ench, level);
		}
		if(removedUnsafe && player != null)
			player.sendMessage(WebAuctionPlus.logPrefix+WebAuctionPlus.Lang.getString("removed_enchantments"));
		return enchants;
	}
	// safely apply enchantments to a stack
	private static void applyItemEnchants(final ItemStack stack, final Player player,
			Map<Enchantment, Integer> enchants) {
		if(stack == null || enchants == null || enchants.isEmpty()) return;
		boolean removedUnsafe = false;
		for(Entry<Enchantment, Integer> entry : enchants.entrySet()) {
			final Enchantment ench = entry.getKey();
			final int level = checkSafeEnchantment(stack, ench, entry.getValue());
			if(level < 1) {
				removedUnsafe = true;
				continue;
			}

			// item meta
			final ItemMeta meta = stack.getItemMeta();
			// enchanted book
			if(meta instanceof EnchantmentStorageMeta) {
				EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
				bookMeta.addStoredEnchant(ench, level, WebAuctionPlus.timEnabled());
				stack.setItemMeta(bookMeta);
				continue;
			}
			// normal item
			if(WebAuctionPlus.timEnabled())
				stack.addUnsafeEnchantment(ench, level);
			else
				stack.addEnchantment(ench, level);
		}
		if(removedUnsafe && player != null)
			player.sendMessage(WebAuctionPlus.logPrefix+WebAuctionPlus.Lang.getString("removed_enchantments"));
	}


	// check natural enchantment
	private static int checkSafeEnchantment(final ItemStack stack, final Enchantment ench, int level) {
		final int FAIL_VALUE = -1;
		// enchanted book
		final boolean isContainer = (stack.getItemMeta() instanceof EnchantmentStorageMeta);
		if(stack == null || ench == null) return FAIL_VALUE;
		if(level < 1) return FAIL_VALUE;
		// Are unsave enchantments enabled?
		if(WebAuctionPlus.timEnabled()) {
			if(level > 127) level = 127;
		} else {
			// is the item enchantable?
			if(!isContainer) {
				if(!ench.canEnchantItem(stack)) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Removed unsafe enchantment: "+stack.toString()+"  "+ench.toString());
				return FAIL_VALUE;
				}
			}
			// level too low
			if(level < ench.getStartLevel()) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Raised unsafe enchantment: "+
					Integer.toString(level)+"  "+stack.toString()+"  "+ench.toString()+"  to level: "+ench.getStartLevel() );
				level = ench.getStartLevel();
			}
			// level too high
			if(level > ench.getMaxLevel()) {
				WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Lowered unsafe enchantment: "+
					Integer.toString(level)+"  "+stack.toString()+"  "+ench.toString()+"  to level: "+ench.getMaxLevel() );
				level = ench.getMaxLevel();
			}
		}
		return level;
	}


	@SuppressWarnings("deprecation")
	private static Integer getEnchId(Enchantment ench) {
		if(ench == null)
			return null;
		return ench.getId();
	}
	@SuppressWarnings("deprecation")
	private static Enchantment getEnchById(Integer value) {
		if(value == null)
			return null;
		return Enchantment.getById(value);
	}


}
