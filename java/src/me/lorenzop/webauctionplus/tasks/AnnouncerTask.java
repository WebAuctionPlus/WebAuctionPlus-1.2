package me.lorenzop.webauctionplus.tasks;

import java.util.ArrayList;
import java.util.List;

import me.lorenzop.webauctionplus.WebAuctionPlus;

import org.bukkit.Server;

public class AnnouncerTask implements Runnable {

	private int currentAnnouncement   = 0;
//	private int numberOfAnnouncements = 0;

	public boolean announceRandom = false;
	private List<String> announcementMessages = new ArrayList<String>();
	public String chatPrefix = "";

	private final WebAuctionPlus plugin;

	public AnnouncerTask(WebAuctionPlus plugin) {
		this.plugin = plugin;
	}

	public void run() {
		if (WebAuctionPlus.getOnlinePlayers().length == 0) return;
		if (this.announcementMessages.isEmpty()) return;
		// random
		if (this.announceRandom) {
			this.currentAnnouncement = WebAuctionPlus.getNewRandom(this.currentAnnouncement, this.announcementMessages.size() - 1);
			announce(this.currentAnnouncement);
		// sequential
		} else {
			while (this.currentAnnouncement > this.announcementMessages.size()-1) {
				this.currentAnnouncement -= this.announcementMessages.size();
			}
			announce(this.currentAnnouncement);
			this.currentAnnouncement++;
		}
//		numberOfAnnouncements++;
	}

	public void addMessages(List<String> addMsg) {
		for (String msg : addMsg)
			addMessages(msg);
	}
	public void addMessages(String addMsg) {
		this.announcementMessages.add(addMsg);
	}
	public void clearMessages() {
		this.announcementMessages.clear();
	}

	public void announce(int lineNumber){
		if (this.announcementMessages.isEmpty() || lineNumber < 0) return;
		WebAuctionPlus.log.info(WebAuctionPlus.logPrefix + "Announcement # " + Integer.toString(lineNumber));
		announce(this.announcementMessages.get(lineNumber));
	}

	public void announce(String line){
		if (line.isEmpty()) return;
		Server server = this.plugin.getServer();
		String[] messages = line.split("&n");
		for (String message : messages) {
			// is command
			if (message.startsWith("/")) {
				server.dispatchCommand(server.getConsoleSender(), message.substring(1));
			} else if (WebAuctionPlus.getOnlinePlayers().length > 0) {
				message = WebAuctionPlus.ReplaceColors(this.chatPrefix + message);
				server.broadcast(message, "wa.announcer.receive");
			}
		}
	}

}
