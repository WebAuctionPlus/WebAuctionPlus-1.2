package me.lorenzop.webauctionplus.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.dao.Auction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShoutSignTask implements Runnable {

	private int lastAuction;

	private final WebAuctionPlus plugin;

	public ShoutSignTask(WebAuctionPlus plugin) {
		this.plugin = plugin;
		// Get current auction ID
		this.lastAuction = WebAuctionPlus.stats.getMaxAuctionID();
		if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Current Auction id = "+this.lastAuction);
	}

	public void run() {
		// check for new auctions
		int latestAuctionID = WebAuctionPlus.stats.getMaxAuctionID();
		if(this.lastAuction >= latestAuctionID) return;
		this.lastAuction = latestAuctionID;
		if(WebAuctionPlus.isDebug()) WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Current Auction id = "+this.lastAuction);
		if(WebAuctionPlus.getOnlinePlayers().length == 0) return;

		Auction auction = WebAuctionPlus.dataQueries.getAuction(latestAuctionID);
		ItemStack stack = auction.getItemStack();

// TODO: language here
		String msg;
		if(auction.getAllowBids()) msg = "新拍卖: ";
		else                       msg = "新上架: ";
		msg += Integer.toString(stack.getAmount())+"x "+auction.getItemTitle()+" ";
		if(stack.getEnchantments().size() == 1)
			msg += "(拥有  1 项附魔) ";
		else if(stack.getEnchantments().size() > 1)
			msg += "(拥有 "+Integer.toString(stack.getEnchantments().size())+" 项附魔) ";
		if(auction.getAllowBids())
			msg += "已经开始!";
		else
			msg += "单价 "+WebAuctionPlus.FormatPrice(auction.getPrice())+".";
		WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+msg);

		// announce globally
		if(WebAuctionPlus.announceGlobal()) {
			Bukkit.broadcastMessage(WebAuctionPlus.chatPrefix+msg);
		} else {

			// Loop each shout sign, sending the New Auction message to each
			List<Location> SignsToRemove = new ArrayList<Location>();
			for(Map.Entry<Location, Integer> entry : this.plugin.shoutSigns.entrySet()) {
				Location loc = entry.getKey();
				int radius = entry.getValue();
				if(loc.getBlock().getType() != Material.SIGN && loc.getBlock().getType() != Material.WALL_SIGN) {
					SignsToRemove.add(loc);
					continue;
				}
				WebAuctionPlus.BroadcastRadius(msg, loc, radius);
			}
			try {
				for(Location signLoc : SignsToRemove) {
					this.plugin.shoutSigns.remove(signLoc);
					WebAuctionPlus.dataQueries.removeShoutSign(signLoc);
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Removed invalid sign at location: "+signLoc);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

		}
	}

}
