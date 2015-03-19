package com.poixson.commonjava.Utils;

import java.util.concurrent.atomic.AtomicLong;


public class CoolDown {

	protected final xTime duration = xTime.get();
	protected final AtomicLong last = new AtomicLong(-1L);



	public static CoolDown get() {
		return new CoolDown();
	}
	public static CoolDown get(final long ms) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(ms);
		return cool;
	}
	public static CoolDown get(final String time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	public static CoolDown get(final xTime time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	private CoolDown() {
	}



	/**
	 * Checks the state of the cooldown period.
	 * @return true if period reached and reset, false if not yet reached.
	 */
	public boolean runAgain() {
		final long current = this.getCurrent();
		// first run
		if(this.last.compareAndSet(-1L, current))
			return true;
		// run again
		final long expected = this.last.get();
		if(expected == -1L)
			return false;
		if(current - expected >= this.duration.getMS())
			return this.last.compareAndSet(expected, current);
		// cooling
		return false;
	}



	public long getCurrent() {
		return utils.getSystemMillis();
	}
	public long getLast() {
		return this.last.get();
	}



	public long getTimeSince() {
		final long last = this.last.get();
		if(last == -1L)
			return -1L;
		return this.getCurrent() - last;
	}
	public long getTimeUntil() {
		final long last = this.last.get();
		if(last == -1L)
			return -1L;
		return (last + this.duration.getMS()) - this.getCurrent();
	}



	public void reset() {
		this.last.set(-1L);
	}
	public void resetRun() {
		this.last.set(utils.getSystemMillis());
	}



	// set duration
	public void setDuration(final long ms) {
		this.duration.set(ms, xTimeU.MS);
	}
	public void setDuration(final String time) {
		this.duration.set(time);
	}
	public void setDuration(final xTime time) {
		this.duration.set(time);
	}
	// get duration
	public xTime getDuration() {
		return this.duration.clone();
	}



}
