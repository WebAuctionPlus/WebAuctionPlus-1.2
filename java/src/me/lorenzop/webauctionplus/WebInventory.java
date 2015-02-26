package me.lorenzop.webauctionplus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.lorenzop.webauctionplus.dao.AuctionPlayer;
import me.lorenzop.webauctionplus.mysql.DataQueries;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WebInventory {

	// inventory instances
	protected static final Map<UUID, WebInventory> openInvs = new HashMap<UUID, WebInventory>();

	protected final Player player;
	protected final Inventory chest;
        protected final AuctionPlayer Aplayer;
	protected final Map<Integer, Integer> tableRowIds = new HashMap<Integer, Integer>();
//	protected List<Integer> slotChanged = new ArrayList<Integer>();


	public WebInventory(final Player player, final AuctionPlayer Aplayer) {
		if(player == null) throw new NullPointerException();
                if(Aplayer == null) throw new NullPointerException();
                this.player = player;
                this.Aplayer = Aplayer;
                int numSlots = WebAuctionPlus.MinMax( WebAuctionPlus.settings.getInteger("Inventory Rows"), 1, 6) * 9;
                String invTitle = WebAuctionPlus.Lang.getString("mailbox_title");
                if(invTitle == null || invTitle.isEmpty()) {
                    invTitle = "WebAuction+ MailBox";
                }
                this.chest = Bukkit.createInventory(null, numSlots, invTitle);
                loadInventory();
                player.openInventory(chest);
	}


	// open mailbox
	public static void onInventoryOpen(final Player player){
		if(player == null) throw new NullPointerException();
		final String playerName = player.getName();
                final UUID playerUUID = player.getUniqueId();
                final AuctionPlayer Aplayer_tmp = WebAuctionPlus.dataQueries.getPlayer(playerUUID);
		synchronized(openInvs){
			// lock inventory                   
                        if(Aplayer_tmp != null) {
                            setLocked(playerUUID, true);
                            if(openInvs.containsKey(playerUUID)) {
                                    // chest already open
                                    player.sendMessage(WebAuctionPlus.chatPrefix+"MailBox already opened!");
                                    WebAuctionPlus.log.warning("Inventory already open for "+playerName+"!");
                                    return;
//                                  inventory = openInvs.get(player);
//                                  p.openInventory(inventory.chest);
                            } else {
                                    // create new virtual chest
                                    player.sendMessage(WebAuctionPlus.chatPrefix+"Opening Inventory.....");
                                    WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Inventory opened for: "+playerName);
                                    final WebInventory inventory = new WebInventory(player, Aplayer_tmp);
                                    openInvs.put(playerUUID, inventory);
                            }
                         } else {
                            player.sendMessage(WebAuctionPlus.chatPrefix+"You have to create an WebAuction account before you can use this sing.");
                        }
		}
//		player.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("mailbox_opened"));
	}
	// close mailbox
	public static void onInventoryClose(final Player player){
		if(player == null) throw new NullPointerException();
		final String playerName = player.getName();
                final UUID playerUUID = player.getUniqueId();
		if(playerName == null || playerName.isEmpty()) throw new NullPointerException();
		synchronized(openInvs){
			if(!openInvs.containsKey(playerUUID)) return;
			final WebInventory inventory = openInvs.get(playerUUID);
			// save inventory
			inventory.saveInventory();
			// remove inventory chest
			openInvs.remove(playerUUID);
			// unlock inventory
			setLocked(playerUUID, false);
		}
		WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"MailBox inventory closed and saved");
		player.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("mailbox_closed"));
	}
	public static void ForceCloseAll() {
		if(openInvs==null || openInvs.size()==0) return;
		for(final UUID playerUUID : openInvs.keySet()) {
			final Player player = Bukkit.getPlayer(playerUUID);
			player.closeInventory();
			WebInventory.onInventoryClose(player);
		}
	}


//	// inventory click
//	public static void onInventoryClick(Player p, int slot) {
//		if(p == null) return;
//		String player = p.getName();
//		if(!openInvs.containsKey(player)) return;
//		openInvs.get(player).onClick(slot);
//	}
//	protected void onClick(int slot) {
//		if(slot > chest.getSize()) return;
//		if(slotChanged.contains(slot)) return;
//WebAuctionPlus.log.warning("SLOT "+Integer.toString(slot));
//		slotChanged.add(slot);
//	}


//	// inventory lock
//	public static boolean isLocked(String player) {
//		boolean locked = false;
//		Connection conn = WebAuctionPlus.dataQueries.getConnection();
//		PreparedStatement st = null;
//		ResultSet rs = null;
//		try {
//			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: isLocked");
//			st = conn.prepareStatement("SELECT `Locked` FROM `"+WebAuctionPlus.dataQueries.dbPrefix()+"Players` "+
//				"WHERE `playerName` = ? LIMIT 1");
//			st.setString(1, player);
//			rs = st.executeQuery();
//			// got lock state
//			if(rs.next()) locked = (rs.getInt("Locked") != 0);
//		} catch(SQLException e) {
//			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to get inventory lock");
//			e.printStackTrace();
//			return true;
//		} finally {
//			WebAuctionPlus.dataQueries.closeResources(conn, st, rs);
//		}
//		return locked;
//	}
	// set inventory lock
	public static void setLocked(final UUID playerUUID, final boolean locked) {
		if(playerUUID == null) throw new NullPointerException();
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		try {
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: setLocked "+(locked?"engaged":"released"));
			st = conn.prepareStatement("UPDATE `"+WebAuctionPlus.dataQueries.dbPrefix()+"Players` "+
				"SET `Locked` = ? WHERE `uuid` = ? LIMIT 1");
			if(locked)
				st.setInt(1, 1);
			else
				st.setInt(1, 0);
			st.setString(2, playerUUID.toString());
			st.executeUpdate();
		} catch(SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to set inventory lock");
			e.printStackTrace();
		} finally {
			WebAuctionPlus.dataQueries.closeResources(conn, st);
		}
	}


	// load inventory from db
	protected void loadInventory() {
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
//		slotChanged.clear();
		chest.clear();
		tableRowIds.clear();
		try {
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: isLocked");
			st = conn.prepareStatement("SELECT `id`, `itemId`, `itemDamage`, `qty`, `enchantments`, `itemTitle` "+
				"FROM `"+WebAuctionPlus.dataQueries.dbPrefix()+"Items` WHERE `playerId` = ? ORDER BY `id` ASC LIMIT ?");
			st.setInt(1, Aplayer.getPlayerId());
			st.setInt   (2, chest.getSize());
			rs = st.executeQuery();
			ItemStack[] stacks = new ItemStack[chest.getSize()];
			int i = -1;
			while(rs.next()) {
				if(rs.getInt("qty") < 1) continue;
				i++; if(i >= chest.getSize()) break;
				tableRowIds.put(i, rs.getInt("id"));
				// create/split item stack
				stacks[i] = getSplitItemStack(
					rs.getInt("itemId"),
					rs.getShort("itemDamage"),
					rs.getInt("qty"),
					rs.getString("enchantments"),
					rs.getString("itemTitle")
				);
				if(stacks[i] == null) tableRowIds.remove(i);
			}
			chest.setContents(stacks);
		} catch(SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to set inventory lock");
			e.printStackTrace();
		} finally {
			WebAuctionPlus.dataQueries.closeResources(conn, st);
		}
	}
	// create/split item stack
	private ItemStack getSplitItemStack(final int itemId, final short itemDamage, final int qty, final String enchStr, final String itemTitle) {
		final Material mat = Material.matchMaterial(Integer.toString(itemId));
		if(mat == null) {
			(new NullPointerException("Unknown material id: "+Integer.toString(itemId)))
				.printStackTrace();
			return null;
		}
		int tmpQty = qty;
		final ItemStack stack = new ItemStack(mat, qty, itemDamage);
		final int maxSize = stack.getMaxStackSize();
		if(maxSize < 1) return null;
		// split stack
		if(qty > maxSize) {
			Connection conn = WebAuctionPlus.dataQueries.getConnection();
			PreparedStatement st = null;
			while(tmpQty > maxSize) {
				try {
					if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: getSplitItemStack  qty:"+Integer.toString(tmpQty)+"  max:"+Integer.toString(maxSize));
					st = conn.prepareStatement("INSERT INTO `"+WebAuctionPlus.dataQueries.dbPrefix()+"Items` ( "+
						"`playerId`, `itemId`, `itemDamage`, `qty`, `enchantments`, `itemTitle` )VALUES( ?, ?, ?, ?, ?, ? )");
					st.setInt(1, Aplayer.getPlayerId());
					st.setInt   (2, itemId);
					st.setShort (3, itemDamage);
					st.setInt   (4, maxSize);
					st.setString(5, enchStr);
					st.setString(6, itemTitle);
					st.executeUpdate();
				} catch(SQLException e) {
					WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to insert new item to inventory!");
					e.printStackTrace();
					return null;
				} finally {
					DataQueries.closeResources(st, null);
				}
				tmpQty -= maxSize;
			}
			stack.setAmount(tmpQty);
			WebAuctionPlus.dataQueries.closeResources(conn);
		}
		// add enchantments
		if(enchStr != null && !enchStr.isEmpty())
			WebItemMeta.decodeEnchants(stack, player, enchStr);
		return stack;
	}
	// save inventory to db
	protected void saveInventory() {
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		int countInserted = 0;
		int countUpdated  = 0;
		int countDeleted  = 0;
		for(int i = 0; i < chest.getSize(); i++) {
//			if(!slotChanged.contains(i)) continue;
			ItemStack stack = chest.getItem(i);

			// empty slot
			if(stack == null || getTypeId(stack) == 0) {

				// delete item
				if(tableRowIds.containsKey(i)) {
					try {
						if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: saveInventory::delete slot "+Integer.toString(i));
						st = conn.prepareStatement("DELETE FROM `"+WebAuctionPlus.dataQueries.dbPrefix()+"Items` WHERE `id` = ? LIMIT 1");
						st.setInt(1, tableRowIds.get(i));
						st.executeUpdate();
					} catch(SQLException e) {
						WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to delete item from inventory!");
						e.printStackTrace();
					} finally {
						DataQueries.closeResources(st, null);
					}
					countDeleted++;
					continue;

				// no item
				} else {
					continue;
				}

			// item in slot
			} else {

				final int itemId = getTypeId(stack);
				final short itemDamage = stack.getDurability();
				final int itemQty = stack.getAmount();
				String enchStr = WebItemMeta.encodeEnchants(stack, player);

				// update existing item
				if(tableRowIds.containsKey(i)) {
					try {
						if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: saveInventory::update slot "+Integer.toString(i));
						st = conn.prepareStatement("UPDATE `"+WebAuctionPlus.dataQueries.dbPrefix()+"Items` SET "+
							"`itemId` = ?, `itemDamage` = ?, `qty` = ?, `enchantments` = ? WHERE `id` = ? LIMIT 1");
						st.setInt   (1, itemId);
						st.setShort (2, itemDamage);
						st.setInt   (3, itemQty);
						st.setString(4, enchStr);
						st.setInt   (5, tableRowIds.get(i));
						st.executeUpdate();
					} catch(SQLException e) {
						WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to update item to inventory!");
						e.printStackTrace();
					} finally {
						DataQueries.closeResources(st, null);
					}
					countUpdated++;
					continue;

				// insert new item
				} else {
					try {
						if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: saveInventory::insert slot "+Integer.toString(i));
						st = conn.prepareStatement("INSERT INTO `"+WebAuctionPlus.dataQueries.dbPrefix()+"Items` ( "+
							"`playerId`, `itemId`, `itemDamage`, `qty`, `enchantments` )VALUES( ?, ?, ?, ?, ? )");
						st.setInt   (1, Aplayer.getPlayerId());
						st.setInt   (2, itemId);
						st.setShort (3, itemDamage);
						st.setInt   (4, itemQty);
						st.setString(5, enchStr);
						st.executeUpdate();
					} catch(SQLException e) {
						WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Unable to insert new item to inventory!");
						e.printStackTrace();
					} finally {
						DataQueries.closeResources(st, null);
					}
					countInserted++;
					continue;

				}
			}

		}
		WebAuctionPlus.dataQueries.closeResources(conn);
//		slotChanged.clear();
		chest.clear();
		tableRowIds.clear();
		WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Updated player inventory for: "+Aplayer.getPlayerName()+" ["+
			" Inserted:"+Integer.toString(countInserted)+
			" Updated:"+Integer.toString(countUpdated)+
			" Deleted:"+Integer.toString(countDeleted)+
			" ]");
	}


	@SuppressWarnings("deprecation")
	private static Integer getTypeId(final ItemStack item) {
		if(item == null)
			return null;
		return item.getTypeId();
	}


}
