package me.lorenzop.webauctionplus.listeners;

import me.lorenzop.webauctionplus.WebAuctionPlus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class failPlayerListener implements Listener {

	private static volatile failPlayerListener instance = null;
	private static final Object lock = new Object();


	public static void start(JavaPlugin plugin) {
		if(instance != null) return;
		synchronized(lock) {
			if(instance != null) return;
			instance = new failPlayerListener();
			// send msg to players when joining
			Bukkit.getPluginManager().registerEvents(instance, plugin);
			// send msg to players already online
			for(Player p : WebAuctionPlus.getOnlinePlayers())
				sendMsg(p);
		}
	}
	public static void stop() {
		if(instance == null) return;
		synchronized(lock) {
			if(instance == null) return;
			HandlerList.unregisterAll(instance);
			instance = null;
		}
	}
	private failPlayerListener() {}


	// player join
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		sendMsg(p);
	}
	private static void sendMsg(Player p) {
		if(p == null) return;
		if(p.hasPermission("wa.webadmin") || p.isOp()) {
			final String failMsg = WebAuctionPlus.getFailMsg();
			if(failMsg == null || failMsg.isEmpty()) {
				p.sendMessage(WebAuctionPlus.chatPrefix + "Failed to load plugin. Please check the console log.");
			} else{
				if(failMsg.contains("|")) {
					for(String m : failMsg.split("|")) {
						if(m == null || m.isEmpty() || m.equals("|"))
							continue;
						p.sendMessage(WebAuctionPlus.chatPrefix + m);
					}
				} else {
					p.sendMessage(WebAuctionPlus.chatPrefix + failMsg);
				}
			}
		}
	}


}
