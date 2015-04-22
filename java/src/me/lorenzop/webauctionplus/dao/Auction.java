package me.lorenzop.webauctionplus.dao;

import org.bukkit.inventory.ItemStack;

public class Auction {

	private int Offset			= -1;
	private ItemStack stack		= null;
	private String itemTitle	= null;
	private String player		= null;
	private int playerId		= 0;
	private double price		= 0D;
//	private long created		= 0;
	private Boolean allowBids	= false;
	private Double currentBid	= 0D;
	private String currentWinner= null;

	public Auction() {
	}

	// auction id
	public int getOffset() {
		return this.Offset;
	}
	public void setOffset(int Offset) {
		this.Offset = Offset;
	}

	// item stack
	public ItemStack getItemStack() {
		return this.stack;
	}
	public void setItemStack(ItemStack stack) {
		this.stack = stack;
	}

	// item title
	public String getItemTitle() {
		if(this.itemTitle == null || this.itemTitle.isEmpty())
			if(this.stack != null) return this.stack.getType().name();
		return this.itemTitle;
	}
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}

	// player name
	public String getPlayerName() {
		return this.player;
	}
	public void setPlayerName(String player) {
		this.player = player;
	}

	// player Id
	public int getPlayerId() {
		return this.playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	// price
	public double getPrice() {
		return this.price;
	}
	public void setPrice(double price) {
		this.price = price;
	}

//	// created timestamp
//	public long getCreated() {
//		return created;
//	}
//	public void setCreated(long created) {
//		this.created = created;
//	}

	// allow bids ?
	public Boolean getAllowBids() {
		return this.allowBids;
	}
	public void setAllowBids(Boolean bid) {
		this.allowBids = bid;
	}

	// current bid ?
	public Double getCurrentBid() {
		return this.currentBid;
	}
	public void setCurrentBid(Double bid) {
		this.currentBid = bid;
	}

	// current winner ?
	public String getCurrentWinner() {
		return this.currentWinner;
	}
	public void setCurrentWinner(String player) {
		this.currentWinner = player;
	}

}
