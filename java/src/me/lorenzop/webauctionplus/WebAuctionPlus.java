package me.lorenzop.webauctionplus;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import me.lorenzop.webauctionplus.dao.waStats;
import me.lorenzop.webauctionplus.listeners.WebAuctionBlockListener;
import me.lorenzop.webauctionplus.listeners.WebAuctionCommands;
import me.lorenzop.webauctionplus.listeners.WebAuctionPlayerListener;
import me.lorenzop.webauctionplus.listeners.WebAuctionServerListener;
import me.lorenzop.webauctionplus.listeners.failPlayerListener;
import me.lorenzop.webauctionplus.mysql.DataQueries;
import me.lorenzop.webauctionplus.mysql.MySQLTables;
import me.lorenzop.webauctionplus.mysql.MySQLUpdate;
import me.lorenzop.webauctionplus.tasks.AnnouncerTask;
import me.lorenzop.webauctionplus.tasks.PlayerAlertTask;
import me.lorenzop.webauctionplus.tasks.RecentSignTask;
import me.lorenzop.webauctionplus.tasks.ShoutSignTask;
import me.lorenzop.webauctionplus.tasks.TempPasswordTimeoutTask;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebAuctionPlus extends JavaPlugin {

	private static volatile WebAuctionPlus instance = null;
	private static volatile boolean isOk    = false;
	private static volatile boolean isDebug = false;
	private static volatile String failMsg = null;

	public static final String logPrefix  = "[MineMarket] ";
	public static final String chatPrefix = ChatColor.DARK_GREEN+"["+ChatColor.WHITE+"MineMarket"+ChatColor.DARK_GREEN+"] ";
	public static final Logger log = Logger.getLogger("Minecraft");

	public static Metrics metrics;
	public static waStats stats;

	// plugin version
	public static String currentVersion = null;
	public static String newVersion = null;
	public static boolean newVersionAvailable = false;

	// config
	public FileConfiguration config = null;
	public static waSettings settings = null;

	// language
	public static volatile Language Lang;

	public static volatile DataQueries dataQueries = null;
	public volatile WebAuctionCommands WebAuctionCommandsListener = new WebAuctionCommands(this);

	public Map<String,   Long>    lastSignUse = new HashMap<String , Long>();
	public Map<Location, Integer> recentSigns = new HashMap<Location, Integer>();
	public Map<Location, Integer> shoutSigns  = new HashMap<Location, Integer>();

	public int signDelay          = 0;
	public int numberOfRecentLink = 0;

	// use recent signs
	private static boolean useOriginalRecent = false;
	// sign link
	private static boolean useSignLink = false;
	// tim the enchanter
	private static boolean timEnabled = false;
	// globally announce new auctions (vs using shout signs)
	private static boolean announceGlobal = false;

	// JSON Server
//	public waJSONServer jsonServer;

	// recent sign task
	public static RecentSignTask recentSignTask = null;

	// temp password timeout task
	public static volatile TempPasswordTimeoutTask tempPassTimeoutTask = null;

	// announcer
	public AnnouncerTask waAnnouncerTask = null;
	public boolean announcerEnabled	= false;

	public static Economy vaultEconomy = null;


	public WebAuctionPlus() {
	}


	public void onEnable() {
//		if(!CheckJavaVersion("1.7"))
//			throw new RuntimeException("This plugin requires java 1.7 or newer!");
		if(isOk) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"********************************************");
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"*** WebAuctionPlus is already running!!! ***");
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"********************************************");
			return;
		}
		instance = this;
		isOk = false;
		failMsg = null;
		currentVersion = getDescription().getVersion();

		// Command listener
		getCommand("wa").setExecutor(this.WebAuctionCommandsListener);

		// load config.yml
		if(!onLoadConfig())
			return;

		// load more services
		onLoadMetrics();
		checkUpdateAvailable();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new WebAuctionPlayerListener(this), this);
		pm.registerEvents(new WebAuctionBlockListener (this), this);
		pm.registerEvents(new WebAuctionServerListener(),     this);
		isOk = true;
	}


	public void onDisable() {
		isOk = false;
		failPlayerListener.stop();
		// unregister listeners
		HandlerList.unregisterAll(this);
		// stop schedulers
		try {
			getServer().getScheduler().cancelTasks(this);
		} catch (Exception ignore) {}
		if(tempPassTimeoutTask != null) tempPassTimeoutTask.shutdown();
		if(this.waAnnouncerTask != null) this.waAnnouncerTask.clearMessages();
		if(this.shoutSigns      != null) this.shoutSigns.clear();
		if(this.recentSigns     != null) this.recentSigns.clear();
		// close inventories
		try {
			WebInventory.ForceCloseAll();
		} catch (Exception ignore) {}
		// close mysql connection
		try {
			if(dataQueries != null)
				dataQueries.forceCloseConnections();
		} catch (Exception ignore) {}
		// close config
		try {
			this.config = null;
		} catch (Exception ignore) {}
		settings = null;
		Lang = null;
		log.info(logPrefix + "Disabled, bye for now :-)");
	}


	public void onReload() {
		failMsg = null;
		onDisable();
		// load config.yml
		if(!onLoadConfig()) return;
		isOk = true;
	}


	public static WebAuctionPlus getPlugin() {
		return instance;
	}
	public static boolean isOk()    {return isOk;}
	public static boolean isDebug() {return isDebug;}


	public static void fail(String msg) {
		if(msg != null && !msg.isEmpty()) {
			log.severe(logPrefix + msg);
			if(failMsg == null) failMsg = "";
			if(!failMsg.isEmpty()) failMsg += "|";
			failMsg += msg;
		}
		getPlugin().onDisable();
		failPlayerListener.start(getPlugin());
	}
	public static String getFailMsg() {
		if(failMsg == null || failMsg.isEmpty())
			return null;
		return failMsg;
	}


	public boolean onLoadConfig() {
		// init configs
		if(this.config != null) this.config = null;
		this.config = getConfig();
		configDefaults();

		// connect MySQL
		if(dataQueries == null)
			if(!ConnectDB())
				return false;

		// load stats class
		if(stats == null) stats = new waStats();

		// load settings from db
		if(settings != null) settings = null;
		settings = new waSettings(this);
		settings.LoadSettings();
		if(!settings.isOk()) {
			fail("Failed to load settings from database.");
			return false;
		}

		// update the version in db
		if(! currentVersion.equals(settings.getString("Version")) ){
			String oldVersion = settings.getString("Version");
			// update database
			MySQLUpdate.doUpdate(oldVersion);
			// update version number
			settings.setString("Version", currentVersion);
			log.info(logPrefix+"Updated version from "+oldVersion+" to "+currentVersion);
		}

		// load language file
		if(Lang != null) Lang = null;
		Lang = new Language(this);
		Lang.loadLanguage(settings.getString("Language"));
		if(!Lang.isOk()) {
			fail("Failed to load language file.");
			return false;
		}

		try {
			isDebug = this.config.getBoolean("Development.Debug");
//			addComment("debug_mode", Arrays.asList("# This is where you enable debug mode"))
			this.signDelay          = this.config.getInt    ("Misc.SignClickDelay");
			timEnabled         = this.config.getBoolean("Misc.UnsafeEnchantments");
			announceGlobal     = this.config.getBoolean("Misc.AnnounceGlobally");
			this.numberOfRecentLink = this.config.getInt    ("SignLink.NumberOfLatestAuctionsToTrack");
			useSignLink        = this.config.getBoolean("SignLink.Enabled");
			if(useSignLink)
				if(!Bukkit.getPluginManager().getPlugin("SignLink").isEnabled()) {
					log.warning(logPrefix+"SignLink is enabled but plugin is not loaded!");
					useSignLink = false;
				}

			// scheduled tasks
			BukkitScheduler scheduler = Bukkit.getScheduler();
			boolean UseMultithreads = this.config.getBoolean("Development.UseMultithreads");

			// temp password timeout task
			if(tempPassTimeoutTask == null || tempPassTimeoutTask.isStopped())
				tempPassTimeoutTask = new TempPasswordTimeoutTask(this);

			// announcer
			this.announcerEnabled = this.config.getBoolean("Announcer.Enabled");
			long announcerMinutes = 20 * 60 * this.config.getLong("Tasks.AnnouncerMinutes");
			if(this.announcerEnabled) this.waAnnouncerTask = new AnnouncerTask(this);
			if (this.announcerEnabled && announcerMinutes>0) {
				if(announcerMinutes < 6000) announcerMinutes = 6000; // minimum 5 minutes
				this.waAnnouncerTask.chatPrefix     = this.config.getString ("Announcer.Prefix");
				this.waAnnouncerTask.announceRandom = this.config.getBoolean("Announcer.Random");
				this.waAnnouncerTask.addMessages(     this.config.getStringList("Announcements"));
				scheduler.runTaskTimerAsynchronously(this, this.waAnnouncerTask,
					(announcerMinutes/2), announcerMinutes);
				log.info(logPrefix + "Enabled Task: Announcer (always multi-threaded)");
			}

			long saleAlertSeconds        = 20 * this.config.getLong("Tasks.SaleAlertSeconds");
			long shoutSignUpdateSeconds  = 20 * this.config.getLong("Tasks.ShoutSignUpdateSeconds");
			long recentSignUpdateSeconds = 20 * this.config.getLong("Tasks.RecentSignUpdateSeconds");
			useOriginalRecent            =      this.config.getBoolean("Misc.UseOriginalRecentSigns");

			// Build shoutSigns map
			if (shoutSignUpdateSeconds > 0)
				this.shoutSigns.putAll(dataQueries.getShoutSignLocations());
			// Build recentSigns map
			if (recentSignUpdateSeconds > 0)
				this.recentSigns.putAll(dataQueries.getRecentSignLocations());

			// report sales to players (always multi-threaded)
			if (saleAlertSeconds > 0) {
				if(saleAlertSeconds < 3*20) saleAlertSeconds = 3*20;
				scheduler.runTaskTimerAsynchronously(this, new PlayerAlertTask(),
					saleAlertSeconds, saleAlertSeconds);
				log.info(logPrefix + "Enabled Task: Sale Alert (always multi-threaded)");
			}
			// shout sign task
			if (shoutSignUpdateSeconds > 0) {
				if (UseMultithreads)
					scheduler.runTaskTimerAsynchronously(this, new ShoutSignTask(this),
						shoutSignUpdateSeconds, shoutSignUpdateSeconds);
				else
					scheduler.scheduleSyncRepeatingTask (this, new ShoutSignTask(this),
						shoutSignUpdateSeconds, shoutSignUpdateSeconds);
				log.info(logPrefix + "Enabled Task: Shout Sign (using " + (UseMultithreads?"multiple threads":"single thread") + ")");
			}
			// update recent signs
			if(recentSignUpdateSeconds > 0 && useOriginalRecent) {
				recentSignTask = new RecentSignTask(this);
				if (UseMultithreads)
					scheduler.runTaskTimerAsynchronously(this, recentSignTask,
						5*20, recentSignUpdateSeconds);
				else
					scheduler.scheduleSyncRepeatingTask (this, recentSignTask,
						5*20, recentSignUpdateSeconds);
				log.info(logPrefix + "Enabled Task: Recent Sign (using " + (UseMultithreads?"multiple threads":"single thread") + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed loading the config.");
			return false;
		}
		return true;
	}

	public void onSaveConfig() {
	}


	// Init database
	public synchronized boolean ConnectDB() {
		if(this.config.getString("MySQL.Password").equals("password123")) {
			fail("Please set the database connection info in the config.");
			return false;
		}
		log.info(logPrefix + "MySQL Initializing.");
		if(dataQueries != null) {
			fail("Database connection already made?");
			return false;
		}
		try {
			int port = this.config.getInt("MySQL.Port");
			if(port < 1) port = Integer.valueOf(this.config.getString("MySQL.Port"));
			if(port < 1) port = 3306;
			dataQueries = new DataQueries(
				this.config.getString("MySQL.Host"),
				port,
				this.config.getString("MySQL.Username"),
				this.config.getString("MySQL.Password"),
				this.config.getString("MySQL.Database"),
				this.config.getString("MySQL.TablePrefix")
			);
			dataQueries.setConnPoolSizeWarn(this.config.getInt("MySQL.ConnectionPoolSizeWarn"));
			dataQueries.setConnPoolSizeHard(this.config.getInt("MySQL.ConnectionPoolSizeHard"));
			// create/update tables
			MySQLTables dbTables = new MySQLTables(this);
			if(!dbTables.isOk()) {
				fail(logPrefix+"Error loading db updater class.");
				return false;
			}
			dbTables = null;
		//} catch (SQLException e) {
		} catch (Exception e) {
			e.printStackTrace();
			fail(logPrefix+"Unable to connect to MySQL database.");
			return false;
		}
		return true;
	}

	private void configDefaults() {
		this.config.addDefault("MySQL.Host",                             "localhost");
		this.config.addDefault("MySQL.Username",                         "minecraft");
		this.config.addDefault("MySQL.Password",                         "password123");
		this.config.addDefault("MySQL.Port",                             3306);
		this.config.addDefault("MySQL.Database",                         "minecraft");
		this.config.addDefault("MySQL.TablePrefix",                      "WA_");
		this.config.addDefault("MySQL.ConnectionPoolSizeWarn",           5);
		this.config.addDefault("MySQL.ConnectionPoolSizeHard",           10);
		this.config.addDefault("Misc.ReportSales",                       true);
		this.config.addDefault("Misc.UseOriginalRecentSigns",            true);
		this.config.addDefault("Misc.SignClickDelay",                    500);
		this.config.addDefault("Misc.UnsafeEnchantments",                false);
		this.config.addDefault("Misc.AnnounceGlobally",                  true);
		this.config.addDefault("Tasks.SaleAlertSeconds",                 20L);
		this.config.addDefault("Tasks.ShoutSignUpdateSeconds",           20L);
		this.config.addDefault("Tasks.RecentSignUpdateSeconds",          60L);
		this.config.addDefault("Tasks.AnnouncerMinutes",                 60L);
		this.config.addDefault("SignLink.Enabled",                       false);
		this.config.addDefault("SignLink.NumberOfLatestAuctionsToTrack", 10);
		this.config.addDefault("Development.UseMultithreads",            false);
		this.config.addDefault("Development.Debug",                      false);
		this.config.addDefault("Announcer.Enabled",                      false);
		this.config.addDefault("Announcer.Prefix",                       "&c[Info] ");
		this.config.addDefault("Announcer.Random",                       false);
		this.config.addDefault("Announcements", new String[] {"This server is running WebAuctionPlus!"} );
		this.config.options().copyDefaults(true);
		saveConfig();
	}


	public static boolean useOriginalRecent() {
		return useOriginalRecent;
	}
	public static boolean useSignLink() {
		return useSignLink;
	}
	public static boolean timEnabled() {
		return timEnabled;
	}
	public static boolean announceGlobal() {
		return announceGlobal;
	}


	@SuppressWarnings("deprecation")
	public static synchronized void doUpdateInventory(Player p) {
		p.updateInventory();
	}

	public static long getCurrentMilli() {
		return System.currentTimeMillis();
	}

	// format chat colors
	public static String ReplaceColors(String text){
		return text.replaceAll("&([0-9a-fA-F])", "\247$1");
	}

	// add strings with delimiter
	public static String addStringSet(String baseString, String addThis, String Delim) {
		if (addThis.isEmpty())    return baseString;
		if (baseString.isEmpty()) return addThis;
		return baseString + Delim + addThis;
	}

//	public static String format(double amount) {
//		DecimalFormat formatter = new DecimalFormat("#,##0.00");
//		String formatted = formatter.format(amount);
//		if (formatted.endsWith("."))
//			formatted = formatted.substring(0, formatted.length() - 1);
//		return Common.formatted(formatted, Constants.Nodes.Major.getStringList(), Constants.Nodes.Minor.getStringList());
//	}

	// work with doubles
	public static String FormatPrice(double value) {
		return settings.getString("Currency Prefix") + FormatDouble(value) + settings.getString("Currency Postfix");
	}
	public static String FormatDouble(double value) {
		DecimalFormat decim = new DecimalFormat("##,###,##0.00");
		return decim.format(value);
	}
	public static double ParseDouble(String value) {
		return Double.parseDouble( value.replaceAll("[^0-9.]+","") );
	}
	public static double RoundDouble(double value, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	public static int getNewRandom(int oldNumber, int maxNumber) {
		if (maxNumber == 0) return maxNumber;
		if (maxNumber == 1) return 1 - oldNumber;
		Random randomGen = new Random();
		int newNumber = 0;
		while (true) {
			newNumber = randomGen.nextInt(maxNumber + 1);
			if (newNumber != oldNumber) return newNumber;
		}
	}

	// min/max value
	public static int MinMax(int value, int min, int max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	public static long MinMax(long value, long min, long max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	public static double MinMax(double value, double min, double max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	// min/max by object
	public static boolean MinMax(Integer value, int min, int max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Long value, long min, long max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Double value, double min, double max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}

	public static String MD5(String str) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(str.getBytes());
		byte[] byteData = md.digest();
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(0xFF & byteData[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static void PrintProgress(double progress, int width) {
		String output = "[";
		int prog = (int)(progress * width);
		if (prog > width) prog = width;
		int i = 0;
		for (; i < prog; i++) {
			output += ".";
		}
		for (; i < width; i++) {
			output += " ";
		}
		WebAuctionPlus.log.info(output + "]");
	}
	public static void PrintProgress(int count, int total, int width) {
		try {
			// finished 100%
			if (count == total)
				PrintProgress( 1D, width);
			// total to small - skip
			else if (total < (width / 2) ) {}
			// print only when adding a .
			else if ( (int)(count % (total / width)) == 0)
				PrintProgress( (double)count / (double)total, width);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void PrintProgress(int count, int total) {
		PrintProgress(count, total, 20);
	}

	// announce radius
	public static void BroadcastRadius(String msg, Location loc, int radius) {
		final Player[] playerList = WebAuctionPlus.getOnlinePlayers();
		Double x = loc.getX();
		Double z = loc.getZ();
		for(Player player : playerList) {
			Double playerX = player.getLocation().getX();
			Double playerZ = player.getLocation().getZ();
			if( (playerX < x + (double)radius ) &&
				(playerX > x - (double)radius ) &&
				(playerZ < z + (double)radius ) &&
				(playerZ > z - (double)radius ) )
					player.sendMessage(WebAuctionPlus.chatPrefix+msg);
		}
	}


	public void onLoadMetrics() {
		// usage stats
		try {
			metrics = new Metrics(this);
			if(metrics.isOptOut()) {
				log.info(logPrefix+"Plugin metrics are disabled, you bum");
				return;
			}
			log.info(logPrefix+"Starting metrics");
			// Create graphs for total Buy Nows / Auctions
			final Metrics.Graph lineGraph  = metrics.createGraph("Stacks For Sale");
			final Metrics.Graph pieGraph   = metrics.createGraph("Selling Method");
			final Metrics.Graph stockTrend = metrics.createGraph("Stock Trend");
			// buy now count
			Metrics.Plotter plotterBuyNows = new Metrics.Plotter("Buy Nows") {
				@Override
				public int getValue(){
					return stats.getTotalBuyNows();
				}
			};
			// auction count
			Metrics.Plotter plotterAuctions = new Metrics.Plotter("Auctions") {
				@Override
				public int getValue(){
					return stats.getTotalAuctions();
				}
			};
			// total selling
			lineGraph.addPlotter(plotterBuyNows);
			lineGraph.addPlotter(plotterAuctions);
			// selling ratio
			pieGraph.addPlotter(plotterBuyNows);
			pieGraph.addPlotter(plotterAuctions);
			// stock trends
			stockTrend.addPlotter(new Metrics.Plotter("New") {
				@Override
				public int getValue() {
					return stats.getNewAuctionsCount();
				}
			});
			stockTrend.addPlotter(new Metrics.Plotter("Ended") {
				@Override
				public int getValue() {
					return stats.getEndedAuctionsCount();
				}
			});
			// start reporting
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
			if(WebAuctionPlus.isDebug) {
				log.severe(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// updateCheck() from MilkBowl's Vault
	// modified for my compareVersions() function
	private static String doUpdateCheck() throws Exception {
		String pluginUrlString = "http://dev.bukkit.org/server-mods/webauctionplus/files.rss";
		try {
			URL url = new URL(pluginUrlString);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();
				String version = firstNodes.item(0).getNodeValue();
				return version.substring(version.lastIndexOf(" ")+1);
			}
		} catch (Exception ignored) {}
		return null;
	}

	// compare versions
	public static String compareVersions(String oldVersion, String newVersion) {
		if(oldVersion == null || newVersion == null) return null;
		oldVersion = normalisedVersion(oldVersion);
		newVersion = normalisedVersion(newVersion);
		int cmp = oldVersion.compareTo(newVersion);
		return cmp<0 ? "<" : cmp>0 ? ">" : "=";
	}
	public static String normalisedVersion(String version) {
		String delim = ".";
		int maxWidth = 5;
		String[] split = Pattern.compile(delim, Pattern.LITERAL).split(version);
		String output = "";
		for(String s : split) {
			output += String.format("%"+maxWidth+'s', s);
		}
		return output;
	}

//	private static boolean CheckJavaVersion(final String requiredVersion) {
//		final String javaVersion;
//		{
//			final String vers = System.getProperty("java.version");
//			if(vers == null || vers.isEmpty()) throw new NullPointerException("Failed to get java version");
//			javaVersion = vers.replace('_', '.');
//		}
//		return !(compareVersions(javaVersion, requiredVersion).equals("<"));
//	}

	// check for an updated version
	private void checkUpdateAvailable() {
		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				try {
					newVersion = doUpdateCheck();
					String cmp = compareVersions(currentVersion, newVersion);
					if(cmp == "<") {
						newVersionAvailable = true;
						log.warning(logPrefix+"An update is available!");
						log.warning(logPrefix+"You're running "+currentVersion+" new version available is "+newVersion);
						log.warning(logPrefix+"http://dev.bukkit.org/server-mods/webauctionplus");
					}
				} catch (Exception ignored) {}
			}
		}, 5 * 20, 14400 * 20); // run every 4 hours
	}


	// Bukkit.getOnlinePlayers() changed the api from returning an array to a Collection in 1.7.10 and broke backward compatibility
	public static Player[] getOnlinePlayers() {
		try {
			if(Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class)
				return ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0])).toArray(new Player[0]);
			else
				return ((Player[])Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}
		return null;
	}


}
