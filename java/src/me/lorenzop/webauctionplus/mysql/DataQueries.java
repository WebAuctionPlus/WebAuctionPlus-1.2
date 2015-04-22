package me.lorenzop.webauctionplus.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.WebItemMeta;
import me.lorenzop.webauctionplus.dao.Auction;
import me.lorenzop.webauctionplus.dao.AuctionPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class DataQueries extends MySQLConnPool {


	public DataQueries(String dbHost, int dbPort, String dbUser,
			String dbPass, String dbName, String dbPrefix) {
		DataQueries.logPrefix = WebAuctionPlus.logPrefix;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
		this.dbName = dbName;
		this.dbPrefix = dbPrefix;
	}


	// auctions
	public Auction getAuction(int auctionId) {
		Auction auction = null;
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: getAuction "+Integer.toString(auctionId));
			st = conn.prepareStatement("SELECT `playerName`, `playerId`, `itemId`, `itemDamage`, `qty`, `enchantments`, `itemTitle`, "+
				"`price`, `allowBids`, `currentBid`, `currentWinner` FROM `"+dbPrefix+"Auctions` JOIN `"+dbPrefix+"Players` ON "+dbPrefix+"Players.id = "+dbPrefix+"Auctions.playerId WHERE "+dbPrefix+"Auctions.id = ? LIMIT 1");
//UNIX_TIMESTANP(`created`) AS `created`,
			st.setInt(1, auctionId);
			rs = st.executeQuery();
			if(rs.next()) {
				auction = new Auction();
				auction.setOffset(auctionId);
				Material mat = Material.matchMaterial(Integer.toString(rs.getInt("itemId")));
				if(mat == null) {
					(new NullPointerException("Unknown item id: "+Integer.toString(rs.getInt("itemId"))))
						.printStackTrace();
					return null;
				}
				ItemStack stack = new ItemStack(mat, rs.getInt("qty"), rs.getShort("itemDamage"));
				WebItemMeta.encodeEnchants(stack, null);
				auction.setItemStack(stack);
				auction.setItemTitle(rs.getString("itemTitle"));
				auction.setPlayerName(rs.getString("playerName"));
				auction.setPlayerName(rs.getString("playerId"));
				auction.setPrice(rs.getDouble("price"));
//				auction.setCreated(rs.getInt("created"));
				auction.setAllowBids(rs.getBoolean("allowBids"));
				auction.setCurrentBid(rs.getDouble("currentBid"));
				auction.setCurrentWinner(rs.getString("currentWinner"));
			}
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to get auction " + Integer.toString(auctionId));
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return auction;
	}


	// shout sign
	public void createShoutSign(World world, int radius, int x, int y, int z) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: createShoutSign " +
				Integer.toString(radius) + " " + Integer.toString(x) + "," +
				Integer.toString(y) + "," + Integer.toString(z) );
			st = conn.prepareStatement("INSERT INTO `"+dbPrefix+"ShoutSigns` " +
				"(`world`, `radius`, `x`, `y`, `z`) VALUES (?, ?, ?, ?, ?)");
			st.setString(1, world.getName());
			st.setInt   (2, radius);
			st.setInt   (3, x);
			st.setInt   (4, y);
			st.setInt   (5, z);
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to create shout sign");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}
	public void removeShoutSign(Location location) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: removeShoutSign " + location.toString());
			st = conn.prepareStatement("DELETE FROM `"+dbPrefix+"ShoutSigns` WHERE " +
				"`world` = ? AND `x` = ? AND `y` = ? AND `z` = ?");
			st.setString(1, location.getWorld().getName());
			st.setInt   (2, (int) location.getX());
			st.setInt   (3, (int) location.getY());
			st.setInt   (4, (int) location.getZ());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to remove shout sign at location " + location);
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}
	public Map<Location, Integer> getShoutSignLocations() {
		Map<Location, Integer> signLocations = new HashMap<Location, Integer>();
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: getShoutSignLocations");
			st = conn.prepareStatement("SELECT `world`,`radius`,`x`,`y`,`z` FROM `"+dbPrefix+"ShoutSigns`");
			Location location;
			rs = st.executeQuery();
			while(rs.next()) {
				World world = Bukkit.getServer().getWorld(rs.getString("world"));
				location = new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				signLocations.put(location,    rs.getInt("radius"));
			}
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to get shout sign locations");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return signLocations;
	}


	// recent sign
	public void createRecentSign(World world, int offset, int x, int y, int z) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: createRecentSign " +
				world.getName() + " " + Integer.toString(offset) + " " +
				Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(z) );
			st = conn.prepareStatement("INSERT INTO `"+dbPrefix+"RecentSigns` " +
				"(`world`, `offset`, `x`, `y`, `z`) VALUES (?, ?, ?, ?, ?)");
			st.setString(1, world.getName());
			st.setInt   (2, offset);
			st.setInt   (3, x);
			st.setInt   (4, y);
			st.setInt   (5, z);
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to create recent sign");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}
	public void removeRecentSign(Location location) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: removeRecentSign " + location.toString());
			st = conn.prepareStatement("DELETE FROM `"+dbPrefix+"RecentSigns` WHERE "+
				"`world` = ? AND `x` = ? AND `y` = ? AND `z` = ?");
			st.setString(1, location.getWorld().getName());
			st.setInt   (2, (int) location.getX());
			st.setInt   (3, (int) location.getY());
			st.setInt   (4, (int) location.getZ());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to remove recent sign at location " + location.toString());
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}
	public Map<Location, Integer> getRecentSignLocations() {
		Map<Location, Integer> signLocations = new HashMap<Location, Integer>();
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: getRecentSignLocations");
			st = conn.prepareStatement("SELECT `world`,`offset`,`x`,`y`,`z` FROM `"+dbPrefix+"RecentSigns`");
			Location location;
			rs = st.executeQuery();
			while(rs.next()) {
				World world = Bukkit.getServer().getWorld(rs.getString("world"));
				location = new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				signLocations.put(location,    rs.getInt("offset"));
			}
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to get shout sign locations");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return signLocations;
	}


	public AuctionPlayer getPlayer(UUID uuid) {
		AuctionPlayer waPlayer = null;
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: getPlayer " + uuid.toString());
			st = conn.prepareStatement("SELECT `id`,`playerName`,`uuid`,`money`,`Permissions` " +
				"FROM `"+dbPrefix+"Players` WHERE `uuid` = ? LIMIT 1");
			st.setString(1, uuid.toString());
			rs = st.executeQuery();
			if(rs.next()) {
				waPlayer = new AuctionPlayer();
				waPlayer.setPlayerId(  rs.getInt   ("id"));
				waPlayer.setPlayerName(rs.getString("playerName"));
				waPlayer.setPlayerUUID(UUID.fromString(rs.getString("uuid")));
				waPlayer.setMoney(     rs.getDouble("money"));
				waPlayer.setPerms(     rs.getString("Permissions"));
			}
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to get player with UUID: " + uuid.toString());
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return waPlayer;
	}

	public AuctionPlayer getPlayer(int id) {
		AuctionPlayer waPlayer = null;
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: getPlayer " + id);
			st = conn.prepareStatement("SELECT `id`,`playerName`,`uuid`,`money`,`Permissions` " +
				"FROM `"+dbPrefix+"Players` WHERE `id` = ? LIMIT 1");
			st.setInt(1, id);
			rs = st.executeQuery();
			if(rs.next()) {
				waPlayer = new AuctionPlayer();
				waPlayer.setPlayerId(  rs.getInt   ("id"));
				waPlayer.setPlayerName(rs.getString("playerName"));
				waPlayer.setPlayerUUID(UUID.fromString(rs.getString("uuid")));
				waPlayer.setMoney(     rs.getDouble("money"));
				waPlayer.setPerms(     rs.getString("Permissions"));
			}
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to get player with interlan ID: " + id);
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return waPlayer;
	}


	public void createPlayer(AuctionPlayer waPlayer, String pass) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: createPlayer " + waPlayer.getPlayerName() +
				" with perms: " + waPlayer.getPermsString());
			st = conn.prepareStatement("INSERT INTO `"+dbPrefix+"Players` " +
				"(`playerName`,`uuid`, `password`, `Permissions`) VALUES (?, ?, ?, ?)");
			st.setString(1, waPlayer.getPlayerName());
			st.setString(2, waPlayer.getPlayerUUID().toString());
			st.setString(3, pass);
			st.setString(4, waPlayer.getPermsString());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to update player permissions in DB");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}

	public void updatePlayerName(AuctionPlayer waPlayer, String name) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: updatePlayerName " + waPlayer.getPlayerUUID().toString());
			st = conn.prepareStatement("UPDATE `"+dbPrefix+"Players` SET `playerName` = ? WHERE `uuid` = ? LIMIT 1");
			st.setString(1, name);
			st.setString(2, waPlayer.getPlayerUUID().toString());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to update name for player: " + waPlayer.getPlayerUUID().toString());
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}


	public void updatePlayerPassword(UUID uuid, String newPass) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: updatePlayerPassword " + uuid.toString());
			st = conn.prepareStatement("UPDATE `"+dbPrefix+"Players` SET `password` = ? WHERE `uuid` = ? LIMIT 1");
			st.setString(1, newPass);
			st.setString(2, uuid.toString());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to update password for player: " + uuid.toString());
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}


	public void updatePlayerPermissions(AuctionPlayer waPlayer, boolean canBuy, boolean canSell, boolean isAdmin) {
		// return if update not needed
		if(waPlayer.comparePerms(canBuy, canSell, isAdmin)) return;
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			// update player permissions for website
			waPlayer.setPerms(canBuy, canSell, isAdmin);
			if(isDebug()) log.info("WA Query: updatePlayerPermissions " + waPlayer.getPlayerName() +
				" with perms: " + waPlayer.getPermsString());
			st = conn.prepareStatement("UPDATE `"+dbPrefix+"Players` SET " +
				"`Permissions` = ? WHERE `uuid` = ? LIMIT 1");
			st.setString(1, waPlayer.getPermsString());
			st.setString(2, waPlayer.getPlayerUUID().toString());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to update player permissions in DB");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}


	public void updatePlayerMoney(UUID uuid, double money) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: updatePlayerMoney " + uuid.toString());
			st = conn.prepareStatement("UPDATE `"+dbPrefix+"Players` SET `money` = ? WHERE `uuid` = ?");
			st.setDouble(1, money);
			st.setString(2, uuid.toString());
			st.executeUpdate();
		} catch(SQLException e) {
			log.warning(logPrefix + "Unable to update player money in DB");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
	}


	// checks if account has temp password
	public Boolean queryTempPassword(final UUID uuid) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(isDebug()) log.info("WA Query: queryTempPassword "+uuid.toString());
			st = conn.prepareStatement("SELECT LENGTH(`password`) AS `size` FROM `"+this.dbPrefix+"Players` WHERE `uuid` = ? LIMIT 1");
			st.setString(1, uuid.toString());
			rs = st.executeQuery();
			if(rs.next()) {
				final int size = rs.getInt("size");
				return Boolean.valueOf(size < 32);
			}
		} catch (SQLException e) {
			log.warning(logPrefix+"Unable to query temp password");
			e.printStackTrace();
		} finally {
			closeResources(conn, st, rs);
		}
		return null;
	}
	// disables a temp password
	public void clearTempPassword(final UUID uuid) {
		Connection conn = getConnection();
		PreparedStatement st = null;
		try {
			if(isDebug()) log.info("WA Query: clearTempPassword "+uuid.toString());
			st = conn.prepareStatement("UPDATE `"+this.dbPrefix+"Players` SET `password` = NULL WHERE `uuid` = ? LIMIT 1");
			st.setString(1, uuid.toString());
			st.executeUpdate();
		} catch (SQLException e) {
			log.warning(logPrefix+"Unable to query temp password");
			e.printStackTrace();
		} finally {
			closeResources(conn, st);
		}
	}


}
