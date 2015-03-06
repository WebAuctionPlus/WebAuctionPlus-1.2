package me.lorenzop.webauctionplus.listeners;

import java.math.BigDecimal;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.WebInventory;
import me.lorenzop.webauctionplus.dao.AuctionPlayer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WebAuctionCommands implements CommandExecutor {

	private final WebAuctionPlus plugin;

	public WebAuctionCommands(WebAuctionPlus plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onCommand(CommandSender c_sender, Command command, String label, String[] args) {
		int params = args.length;
		OfflinePlayer player = null;
                final CommandSender sender = c_sender;
		if(sender instanceof Player) player = ((Player) sender);
		// 0 args
		if(params == 0) {
			return false;
		}
		// 1 arg
		if(params == 1) {
//			if(args[0].equalsIgnoreCase("close")){
//				WebInventory.onInventoryClose((Player) sender);
//				return true;
//			}
			// wa reload
			if(args[0].equalsIgnoreCase("reload")){
				if(sender instanceof Player) {
					if(!sender.hasPermission("wa.reload")){
						((Player)sender).sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("no_permission"));
						return true;
					}
				}
				if(sender instanceof Player)
					sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("reloading"));
				WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+WebAuctionPlus.Lang.getString("reloading"));
				plugin.onReload();
				if(WebAuctionPlus.isOk()) {
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("finished_reloading"));
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+WebAuctionPlus.Lang.getString("finished_reloading"));
				} else {
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix+"Failed to reload!");
					WebAuctionPlus.log.severe(WebAuctionPlus.logPrefix+"Failed to reload!");
				}
				return true;
			}
			// wa version
			if (args[0].equalsIgnoreCase("version")) {
				if(sender instanceof Player) {
					sender.sendMessage(WebAuctionPlus.chatPrefix+"v"+plugin.getDescription().getVersion());
					if(WebAuctionPlus.newVersionAvailable && sender.hasPermission("wa.webadmin"))
						sender.sendMessage(WebAuctionPlus.chatPrefix+"A new version is available! " + WebAuctionPlus.newVersion);
				} else {
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"v"+plugin.getDescription().getVersion());
					if(WebAuctionPlus.newVersionAvailable) {
						WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"A new version is available! " + WebAuctionPlus.newVersion);
						WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"http://dev.bukkit.org/server-mods/webauctionplus");
					}
				}
				return true;
			}
                        // wa mailbox
			if (args[0].equalsIgnoreCase("mailbox")) {
                            if(sender instanceof Player) {
                                if(!sender.hasPermission("wa.use.command.mailbox")) {
                                    sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
                                    return true;
                                }
                                // disallow creative
                                if(((Player)sender).getGameMode() != GameMode.SURVIVAL && !((Player)sender).isOp()) {
                                    sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_cheating"));
                                    return true;
                                }
                                // load virtual chest
                                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        WebInventory.onInventoryOpen(((Player)sender));  
                                    }
                                });
                            } else {
				WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"You can't use this command from the console!");
                            }
                            return true;
			}
			// wa update
			if(args[0].equalsIgnoreCase("update")){
				if(!sender.hasPermission("wa.reload")){
					sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("no_permission"));
					return true;
				}
				WebAuctionPlus.recentSignTask.run();
				if(sender instanceof Player)
					sender.sendMessage(WebAuctionPlus.chatPrefix+"Updated recent signs.");
				WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Updated recent signs.");
				return true;
			}
			return false;
		}
		if(!WebAuctionPlus.isOk()) {sender.sendMessage(WebAuctionPlus.chatPrefix+"Plugin isn't loaded"); return true;}
		// 2 args
		if(params == 2 || params == 3) {
			// wa password
			if (args[0].equalsIgnoreCase("password") ||
				args[0].equalsIgnoreCase("pass")     ||
				args[0].equalsIgnoreCase("pw")       ) {
				String pass = "";
				// is player
				boolean isPlayer = (sender instanceof Player);
				if (isPlayer) {
					if (params != 2 || args[1].isEmpty()) return false;
					pass = WebAuctionPlus.MD5(args[1]);
					args[1] = "";
				// is console
				} else {
					if (params != 3) return false;
					if (args[1].isEmpty() || args[2].isEmpty()) return false;
					player = getOfflinePlayer(args[1]);
					if(!player.hasPlayedBefore()) {
						sender.sendMessage(WebAuctionPlus.logPrefix+"Player not found!");
						sender.sendMessage(WebAuctionPlus.logPrefix+"Note: if you really need to, you can add a player to the database, just md5 the password.");
						return true;
					}
					pass = WebAuctionPlus.MD5(args[2]);
					args[2] = "";
				}
				//if(player.isEmpty()) return false;
				AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
				// create that person in database
				if(waPlayer == null) {
					// permission to create an account
					if (isPlayer) {
						if (!sender.hasPermission("wa.password.create")){
							((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
							return true;
						}
					}
					waPlayer = new AuctionPlayer(player.getUniqueId());
					waPlayer.setPerms(
						sender.hasPermission("wa.canbuy")   && isPlayer,
						sender.hasPermission("wa.cansell")  && isPlayer,
						sender.hasPermission("wa.webadmin") && isPlayer
					);
					WebAuctionPlus.dataQueries.createPlayer(waPlayer, pass);
					if (sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_created"));
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + WebAuctionPlus.Lang.getString("account_created") + " " + player +
							" with perms: " + waPlayer.getPermsString());
				// change password for an existing account
				} else {
					// permission to change password
					if(sender instanceof Player) {
						if (!sender.hasPermission("wa.password.change")){
							((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
							return true;
						}
					}
					WebAuctionPlus.dataQueries.updatePlayerPassword(player.getUniqueId(), pass);
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("password_changed"));
					WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + WebAuctionPlus.Lang.getString("password_changed") + " " + player);
				}
				return true;
			}
                        // wa deposit
			if (args[0].equalsIgnoreCase("deposit")) {
				double amount = 0.0D;
				// is player
				boolean isPlayer = (sender instanceof Player);
				if (isPlayer) {
                                    if (params != 2 || args[1].isEmpty()) return false;
                                    amount = WebAuctionPlus.ParseDouble(args[1]);
                                    args[1] = "";
                                    if (!sender.hasPermission("wa.use.command.deposit")){
                                        ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
                                        return true;
                                    }
                                    // player has enough money
                                    if(!WebAuctionPlus.vaultEconomy.has(((Player)sender), amount)) {
                                        ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_pocket"));
                                        return true;
                                    }
                                    // select waPlayer from database
                                    AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
                                    // check if person has an wa account
                                    if(waPlayer == null) {
                                        ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
                                        return true;
                                    }
                                    double currentMoney = waPlayer.getMoney();
                                    currentMoney += amount;
                                    currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
                                    ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + "Added " + amount +
                                                " to auction account, new auction balance: " + currentMoney);
                                    WebAuctionPlus.dataQueries.updatePlayerMoney(((Player)sender).getUniqueId(), currentMoney);
                                    WebAuctionPlus.vaultEconomy.withdrawPlayer(((Player)sender), amount);
                                    return true;                                                                          
				// is console
				} else {
					if (params != 3) return false;
					if (args[1].isEmpty() || args[2].isEmpty()) return false;
					player = getOfflinePlayer(args[1]);
					if(!player.hasPlayedBefore()) {
						WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Player not found!");
						return true;
					}
					amount = WebAuctionPlus.ParseDouble(args[2]);
					args[2] = "";
                                        // player has enough money
                                        if(!WebAuctionPlus.vaultEconomy.has(player, amount)) {
                                            WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_pocket"));
                                            return true;
                                        }
                                        // select waPlayer from database
                                        AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
                                        // check if person has an wa account
                                        if(waPlayer == null) {
                                            WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
                                            return true;
                                        }
                                        double currentMoney = waPlayer.getMoney();
                                        currentMoney += amount;
                                        currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
                                        WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + "Added " + amount +
                                                    " to auction account, new auction balance: " + currentMoney);
                                        WebAuctionPlus.dataQueries.updatePlayerMoney(player.getUniqueId(), currentMoney);
                                        WebAuctionPlus.vaultEconomy.withdrawPlayer(player, amount);
                                        return true; 
				}                        
			}
                         // wa withdraw
			if (args[0].equalsIgnoreCase("withdraw")) {
				double amount = 0.0D;
				// is player
				boolean isPlayer = (sender instanceof Player);
				if (isPlayer) {
                                    if (params != 2 || args[1].isEmpty()) return false;
                                    amount = WebAuctionPlus.ParseDouble(args[1]);
                                    args[1] = "";
                                    if (!sender.hasPermission("wa.use.command.withdraw")){
                                        ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
                                        return true;
                                    }                                   
                                    // select waPlayer from database
                                    AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
                                    // check if person has an wa account
                                    if(waPlayer == null) {
                                        ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
                                        return true;
                                    }
                                    double currentMoney = waPlayer.getMoney();
                                    if(currentMoney < amount) {
					((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_account"));
					return true;
                                    }
                                    currentMoney -= amount;
                                    currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
                                    ((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + "Removed " +
					amount + " from auction account, new auction balance: " + currentMoney);
                                    WebAuctionPlus.dataQueries.updatePlayerMoney(((Player)sender).getUniqueId(), currentMoney);
                                    WebAuctionPlus.vaultEconomy.depositPlayer(((Player)sender), amount);
                                    return true;
                                // is console
                                } else {
					if (params != 3) return false;
					if (args[1].isEmpty() || args[2].isEmpty()) return false;
					player = getOfflinePlayer(args[1]);
					if(!player.hasPlayedBefore()) {
						WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Player not found!");
						return true;
					}
					amount = WebAuctionPlus.ParseDouble(args[2]);
					args[2] = "";
                                        
                                        // select waPlayer from database
                                        AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player.getUniqueId());
                                        // check if person has an wa account
                                        if(waPlayer == null) {
                                            WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
                                            return true;
                                        }
                                        double currentMoney = waPlayer.getMoney();
                                        if(currentMoney < amount) {
					WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_account"));
					return true;
                                        }
                                        currentMoney -= amount;
                                        currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
                                        WebAuctionPlus.log.info(WebAuctionPlus.chatPrefix + "Removed " +
                                                    amount + " from auction account, new auction balance: " + currentMoney);
                                        WebAuctionPlus.dataQueries.updatePlayerMoney(player.getUniqueId(), currentMoney);
                                        WebAuctionPlus.vaultEconomy.depositPlayer(player, amount);
                                        return true;
				}                        
			}
			return false;
		}
		// 4 args
		if(params == 4) {
//			// wa give <player> <item> <count>
//			if (args[0].equals("give")) {
// /wa give lorenzop diamond 3
//			}
			return false;
		}
		return false;
	}


	@SuppressWarnings("deprecation")
	private static OfflinePlayer getOfflinePlayer(final String playerName) {
		return Bukkit.getOfflinePlayer(playerName);
	}


}
