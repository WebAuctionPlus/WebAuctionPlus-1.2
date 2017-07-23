package me.lorenzop.webauctionplus.listeners;

import me.lorenzop.webauctionplus.WebAuctionPlus;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class WebAuctionBlockListener implements Listener {

	private final WebAuctionPlus plugin;

	public WebAuctionBlockListener(WebAuctionPlus plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player p = event.getPlayer();
		if(getTypeId(block) == 63 || getTypeId(block) == 68) {
			Sign thisSign = (Sign) block.getState();
			if(ChatColor.stripColor(thisSign.getLine(0)).equals("[MineMarket]")) {
				if(!p.hasPermission("wa.remove")) {
					event.setCancelled(true);
					p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
				} else {
					p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("sign_removed"));
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + WebAuctionPlus.Lang.getString("sign_removed"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event) {

		//Temporary fix for 1.8
		String[] tmp_lines = event.getLines();
		String[] lines = new String[4];
		lines[0] = ChatColor.stripColor(tmp_lines[0]);
		lines[1] = ChatColor.stripColor(tmp_lines[1]);
		lines[2] = ChatColor.stripColor(tmp_lines[2]);
		lines[3] = ChatColor.stripColor(tmp_lines[3]);

		Player p = event.getPlayer();
		Block sign = event.getBlock();
		World world = sign.getWorld();
		if(p == null) return;
		if (!lines[0].equalsIgnoreCase("[WebAuction]") &&
			!lines[0].equalsIgnoreCase("[MineMarket]") &&
			!lines[0].equalsIgnoreCase("[mm]") ) return;
		event.setLine(0, "[MineMarket]");

		// Shout sign
		if(lines[1].equalsIgnoreCase("Shout")) {
			if(!p.hasPermission("wa.create.sign.shout")) {
				NoPermission(event);
				return;
			}
			event.setLine(1, "Shout");
			// line 2: radius
			int radius = 20;
			try {
				radius = Integer.parseInt(lines[2]);
			} catch (NumberFormatException ignore) {}
			event.setLine(2, Integer.toString(radius));
			event.setLine(3, "");
			this.plugin.shoutSigns.put(sign.getLocation(), radius);
			WebAuctionPlus.dataQueries.createShoutSign(world, radius, sign.getX(), sign.getY(), sign.getZ());
			p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("created_shout_sign"));
			return;
		}

		// Recent sign
		if(lines[1].equalsIgnoreCase("Recent")) {
			if(!p.hasPermission("wa.create.sign.recent")) {
				NoPermission(event);
				return;
			}
			// line 2: recent offset
			int offset = 1;
			try {
				offset = Integer.parseInt(lines[2]);
			} catch (NumberFormatException ignore) {}
			if(offset < 1)  offset = 1;
			if(offset > 99) offset = 99;
			// display auction
//			if(offset <= WebAuctionPlus.Stats.getTotalAuctions()) {
//				Auction offsetAuction = WebAuctionPlus.dataQueries.getAuctionForOffset(offset - 1);
//				ItemStack stack = offsetAuction.getItemStack();
//				int qty = stack.getAmount();
//				String formattedPrice = plugin.economy.format(offsetAuction.getPrice());
//				event.setLine(1, stack.getType().toString());
//				event.setLine(2, "qty: "+Integer.toString(qty));
//				event.setLine(3, formattedPrice);
//			} else {
				event.setLine(1, "Recent");
				event.setLine(2, Integer.toString(offset));
				event.setLine(3, "<New Sign>");
//			}
			this.plugin.recentSigns.put(sign.getLocation(), offset);
			WebAuctionPlus.dataQueries.createRecentSign(world, offset, sign.getX(), sign.getY(), sign.getZ());
			p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("created_recent_sign"));
			return;
		}

		// Deposit sign (money)
		if(lines[1].equalsIgnoreCase("Deposit")) {
			if(!p.hasPermission("wa.create.sign.deposit")) {
				NoPermission(event);
				return;
			}
			event.setLine(1, "Deposit");
			// line 2: amount
			double amount = 100.0;
			try {
				amount = WebAuctionPlus.ParseDouble(lines[2]);
				if(amount <= 0.0) amount = 100.0;
			} catch(NumberFormatException ignore) {}
			event.setLine(2, WebAuctionPlus.FormatDouble(amount));
			event.setLine(3, "");
			p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("created_deposit_sign"));
			return;
		}

		// Withdraw sign (money)
		if(lines[1].equalsIgnoreCase("Withdraw")) {
			if(!p.hasPermission("wa.create.sign.withdraw")) {
				NoPermission(event);
				return;
			}
			if(!lines[1].equals("Withdraw")) event.setLine(1,"Withdraw");
			// line 2: amount
			double amount = 0.0;
			if(!lines[2].equalsIgnoreCase("all")) {
				try {
					amount = WebAuctionPlus.ParseDouble(lines[2]);
					if(amount < 0.0) amount = 0.0;
				} catch(NumberFormatException ignore) {}
			}
			event.setLine(2, amount==0.0 ? "All" : WebAuctionPlus.FormatDouble(amount) );
			event.setLine(3, "");
			p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("created_withdraw_sign"));
			return;
		}

		// MailBox sign
		if(lines[1].equalsIgnoreCase("MailBox") ||
			lines[1].equalsIgnoreCase("Mail Box") ||
			lines[1].equalsIgnoreCase("Mail")) {
			if(!p.hasPermission("wa.create.sign.mailbox")) {
				NoPermission(event);
				return;
			}
			int page = 1;
			try {
				page = Integer.parseInt(lines[2]);
			} catch (NumberFormatException ignore) {}
			if(page < 1)  page = 1;
			if(page > 16) page = 16;
			event.setLine(1, "MailBox");
			event.setLine(2, Integer.toString(page));
			p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("created_deposit_mail_sign"));
			return;
		}

		// invalid web auction sign
		p.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("invalid_sign"));
		CancelEvent(event);
	}


	// no permission
	private static void NoPermission(SignChangeEvent event) {
		CancelEvent(event);
		Player p = event.getPlayer();
		if(p != null) p.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("no_permission"));
	}
	// invalid parameters
//	private static void InvalidParams(SignChangeEvent event) {
//		CancelEvent(event);
//		Player p = event.getPlayer();
//TODO: add to language files
//		if(p != null) p.sendMessage(WebAuctionPlus.chatPrefix+"Invalid sign parameters! Please check dev bukkit for the right sign usage.");
//	}
	private static void CancelEvent(SignChangeEvent event) {
		event.setCancelled(true);
//		event.getBlock().setTypeId(0);
//		Player p = event.getPlayer();
//		if(p != null) {
//			p.getInventory().addItem(new ItemStack(323, 1));
//			WebAuctionPlus.doUpdateInventory(p);
//		}
	}


	@SuppressWarnings("deprecation")
	private static Integer getTypeId(Block block) {
		if(block == null)
			return null;
		return block.getTypeId();
	}


}
