package me.lorenzop.webauctionplus.tasks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import me.lorenzop.webauctionplus.WebAuctionPlus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.xTime;


/**
 * This class disables an account after 15 minutes if the temporary password has not been changed.
 * @author lorenzop
 */
public class TempPasswordTimeoutTask {

	private static final xTime CHECK_PERIOD      = xTime.get("1m");
	private static final xTime TEMP_PASS_TIMEOUT = xTime.get("15m");

	private final WebAuctionPlus plugin;
	private final CopyOnWriteArraySet<TempPassDAO> tasks = new CopyOnWriteArraySet<TempPassDAO>();
	private volatile boolean stopping = false;



	private static class TempPassDAO {
		public final CoolDown cool;
		public final UUID uuid;

		public TempPassDAO(final UUID uuid) {
			this.cool = CoolDown.get(TEMP_PASS_TIMEOUT);
			this.cool.resetRun();
			this.uuid = uuid;
		}

	}



	public TempPasswordTimeoutTask(final WebAuctionPlus plugin) {
		this.plugin = plugin;
		// start task
		final BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskTimerAsynchronously(
				plugin,
				new Runnable() {
					private volatile TempPasswordTimeoutTask task = null;
					public Runnable init(final TempPasswordTimeoutTask task) {
						this.task = task;
						return this;
					}
					@Override
					public void run() {
						this.task.checkAll();
					}
				}.init(this),
				TEMP_PASS_TIMEOUT.getMS() / 50,
				CHECK_PERIOD.getMS()      / 50
		);
	}
	public void shutdown() {
		this.stopping = true;
		// check timeouts one last time
		final Set<TempPassDAO> remove = new HashSet<TempPassDAO>();
		final Iterator<TempPassDAO> it = this.tasks.iterator();
		while(it.hasNext()) {
			final TempPassDAO dao = it.next();
			this.timeoutReached(dao);
			remove.add(dao);
		}
		synchronized(this.tasks) {
			for(final TempPassDAO dao : remove)
				this.tasks.remove(dao);
		}
	}



	public boolean isStopped() {
		return this.stopping;
	}



	public void register(final UUID uuid) {
		if(this.stopping)
			return;
		final TempPassDAO dao = new TempPassDAO(uuid);
		this.tasks.add(dao);
	}



	public void checkAll() {
		if(this.tasks.isEmpty())
			return;
		final Set<TempPassDAO> remove = new HashSet<TempPassDAO>();
		final Iterator<TempPassDAO> it = this.tasks.iterator();
		while(it.hasNext()) {
			final TempPassDAO dao = it.next();
			// timeout reached
			if(dao.cool.runAgain()) {
				this.timeoutReached(dao);
				remove.add(dao);
			}
		}
		synchronized(this.tasks) {
			for(final TempPassDAO dao : remove)
				this.tasks.remove(dao);
		}
	}
	private void timeoutReached(final TempPassDAO dao) {
		final Boolean result = WebAuctionPlus.dataQueries.queryTempPassword(dao.uuid);
		// account still has temp password
		if(result != null && result.booleanValue() == true) {
			WebAuctionPlus.log.info(WebAuctionPlus.logPrefix+"Temporary password timeout for player: "+
					Bukkit.getOfflinePlayer(dao.uuid).getName());
			WebAuctionPlus.dataQueries.clearTempPassword(dao.uuid);
			// tell the player
			if(!this.stopping) {
				final BukkitScheduler scheduler = Bukkit.getScheduler();
				scheduler.runTask(
						this.plugin,
						new Runnable() {
							private volatile UUID uuid = null;
							public Runnable init(final UUID uuid) {
								this.uuid = uuid;
								return this;
							}
							@Override
							public void run() {
								final Player player = Bukkit.getPlayer(this.uuid);
								if(player != null)
									player.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("temp_password_expired"));
							}
						}.init(dao.uuid)
				);
			}
		}
	}



}
