package me.lorenzop.webauctionplus.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.dao.AuctionPlayer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerAlertTask implements Runnable {

	private OfflinePlayer playerJoined = null;


	public PlayerAlertTask() {
		this.playerJoined = null;
	}
	public PlayerAlertTask(OfflinePlayer playerJoined) {
		this.playerJoined = playerJoined;
	}


	@SuppressWarnings("resource")
	public synchronized void run() {
		HashMap<Integer, AuctionPlayer> playersMap = new HashMap<Integer, AuctionPlayer>();
		AuctionPlayer waPlayer = null;
		Player p = null;
		String whereSql = "";
		int i = 0;
		// build players online hashmap
		if(this.playerJoined == null) {
			final Player[] playersList = WebAuctionPlus.getOnlinePlayers();
			// no players online
			if (playersList.length == 0) return;
			// build query
			for (Player player : playersList) {
				waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
				if(waPlayer != null) {
					i++; if(i != 1) whereSql += " OR ";
					whereSql += "`sellerid` = ?";
					playersMap.put(i, waPlayer);
				}
			}
		// only running for a single joined player
		} else {
			waPlayer = WebAuctionPlus.dataQueries.getPlayer(this.playerJoined.getUniqueId());
			p = Bukkit.getPlayer(this.playerJoined.getUniqueId());
			if (waPlayer == null || p==null) return;
			// update name
			if (!waPlayer.getPlayerName().equals(p.getName())){
				WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + "Name of player - " + this.playerJoined + " has changed. " +
						"The old name was: " + waPlayer.getPlayerName());
				WebAuctionPlus.dataQueries.updatePlayerName(waPlayer, p.getName());
				waPlayer.setPlayerName(p.getName());
			}
			// update permissions
			boolean canBuy  = p.hasPermission("wa.canbuy");
			boolean canSell = p.hasPermission("wa.cansell");
			boolean isAdmin = p.hasPermission("wa.webadmin");
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + "Player found - " + this.playerJoined + " with perms:" +
					(canBuy ?" canBuy" :"") +
					(canSell?" canSell":"") +
					(isAdmin?" isAdmin":"") );
			WebAuctionPlus.dataQueries.updatePlayerPermissions(waPlayer, canBuy, canSell, isAdmin);
			// build query
			whereSql += "sellerid = ?";
			playersMap.put(1, waPlayer);
		}
		if(playersMap.size() == 0) return;
		// run the querys
		Connection conn = WebAuctionPlus.dataQueries.getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: SaleAlertTask::SaleAlerts " + playersMap.toString());
			st = conn.prepareStatement("SELECT "+WebAuctionPlus.dataQueries.dbPrefix()+"LogSales.id, `playerName`, `saleType`, `itemType`, `itemTitle`, `sellerid`, `qty`,`price` FROM " +
				WebAuctionPlus.dataQueries.dbPrefix()+"LogSales JOIN "+WebAuctionPlus.dataQueries.dbPrefix()+"Players ON "+WebAuctionPlus.dataQueries.dbPrefix()+"LogSales.buyerid = "+WebAuctionPlus.dataQueries.dbPrefix()+"Players.id  WHERE ( " + whereSql + " ) AND `logType` = 'sale' AND `alert` != 0 LIMIT 4");
			for(Map.Entry<Integer, AuctionPlayer> entry : playersMap.entrySet()) {
				st.setInt(entry.getKey(), entry.getValue().getPlayerId());
			}
			rs = st.executeQuery();
			String markSeenSql = "";
			while (rs.next()) {
				if(this.playerJoined == null)
					p = Bukkit.getPlayer(WebAuctionPlus.dataQueries.getPlayer(rs.getInt("sellerid")).getPlayerUUID());
				if(p != null) {
// TODO: language here
					p.sendMessage(WebAuctionPlus.chatPrefix+"You sold " +
						rs.getInt   ("qty")+"x "+
						rs.getString("itemTitle")+" to "+
						rs.getString("playerName")+" for "+
						WebAuctionPlus.FormatPrice(rs.getDouble("price"))+
						(rs.getInt("qty")>1 ? " each, "+WebAuctionPlus.FormatPrice(rs.getDouble("price")*rs.getDouble("qty"))+" total." : "") );
					// mark seen sql
					if(!markSeenSql.isEmpty()) markSeenSql += " OR ";
					markSeenSql += "`id` = " + Integer.toString(rs.getInt("id"));
				}
			}
			// mark seen
			if(!markSeenSql.isEmpty()) {
				if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info("WA Query: SaleAlertTask::SaleAlerts " + playersMap.toString());
				st = conn.prepareStatement("UPDATE `"+WebAuctionPlus.dataQueries.dbPrefix()+"LogSales` SET `alert` = 0 WHERE " + markSeenSql);
				if(st.executeUpdate() == 0)
					WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix+"Failed to mark sale alerts seen!");
			}
			// alert joined player
			if(this.playerJoined!=null && p!=null) {
				// alert admin of new version
				if(WebAuctionPlus.newVersionAvailable && ( p.hasPermission("wa.webadmin") || p.isOp() ))
					p.sendMessage(WebAuctionPlus.chatPrefix + "A new version is available! " + WebAuctionPlus.newVersion);
			}
		} catch (SQLException e) {
			WebAuctionPlus.log.warning(WebAuctionPlus.logPrefix + "Unable to get sale alerts for players");
			e.printStackTrace();
		} finally {
			WebAuctionPlus.dataQueries.closeResources(conn, st, rs);
		}
	}


}
