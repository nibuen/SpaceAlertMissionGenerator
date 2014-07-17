package com.boarbeard;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import android.os.Handler;
import android.os.SystemClock;

public class MissionTimer {

	private static class Job implements Comparable<Job> {

		private static Job pool = null;
		private final static Object poolLock = new Object();

		/* package */static Job create(long time, Runnable toDo) {
			Job result = null;
			synchronized (poolLock) {
				result = pool;
				if (result != null) {
					pool = result.next;
					return result.init(time, toDo);
				}
			}
			return new Job(time, toDo);
		}

		/* package */long time;

		/* package */Runnable toDo;

		private Job next;

		private Job(long time, Runnable toDo) {
			super();
			init(time, toDo);
		}

		private Job init(long time, Runnable toDo) {
			this.time = time;
			this.toDo = toDo;
			this.next = null;
			return this;
		}

		public int compareTo(Job another) {

			return Long.signum(time - another.time);
		}

		/* package */Runnable recycle() {
			Runnable result = toDo;
			toDo = null;
			synchronized (poolLock) {
				next = pool;
				pool = this;
			}
			return result;
		}
	}

	private volatile long startTime;
	private volatile long pauseTime;

	private final Handler handler = new Handler();
	private final Queue<Job> jobQueue = new PriorityBlockingQueue<Job>();

	private final Runnable jobRunner = new Runnable() {
		/**
		 * {@link SystemClock#elapsedRealtime()} when the runner is planed to run
		 * next
		 */
		private long nextRuntime = Long.MAX_VALUE;

		public void run() {
			if (jobQueue.isEmpty()) {
				return;
			}
			if (SystemClock.elapsedRealtime() >= nextRuntime) {
				// Handler won't run again
				nextRuntime = Long.MAX_VALUE;
			}

			Job job;
			for (job = jobQueue.peek(); job != null
					&& job.time <= missionTimeMillis(); job = jobQueue.peek()) {

				jobQueue.remove();
				handler.post(job.recycle());
			}

			if (job != null && isRunning()) {
				long uptimeMillis = job.time + startTime;
				if (uptimeMillis < nextRuntime) {
					handler.postAtTime(this, uptimeMillis);
					nextRuntime = uptimeMillis;
				}
			}
		}
	};

	public MissionTimer() {
		super();
		reset();
	}

	public boolean isPaused() {
		return pauseTime > startTime;
	}

	public boolean isRunning() {
		return pauseTime < startTime;
	}

	public long missionTimeMillis() {
		if (isPaused()) {
			return pauseTime - startTime;
		} else if (isRunning()) {
			return SystemClock.elapsedRealtime() - startTime;
		} else {
			return 0;
		}
	}

	public void pause() {
		pauseTime = SystemClock.elapsedRealtime();
		handler.removeCallbacks(jobRunner);
	}

	public void reset() {
		startTime = Long.MIN_VALUE;
		pauseTime = Long.MIN_VALUE;

		handler.removeCallbacks(jobRunner);
		jobQueue.clear();
	}

	public void start() {
		startTime = SystemClock.elapsedRealtime() - missionTimeMillis();
		pauseTime = Long.MIN_VALUE;
		handler.post(jobRunner);
	}

	public void runAtTime(Runnable r, long atTimeMillis) {
		if (missionTimeMillis() < atTimeMillis) {
			jobQueue.add(Job.create(atTimeMillis, r));
			handler.post(jobRunner);
		} else {
			handler.post(r);
		}
	}

	public void runDelayed(Runnable r, long delayMillis) {
		runAtTime(r, missionTimeMillis() + delayMillis);
	}

	public void removeCallbacks(final Runnable r) {
		handler.post(new Runnable() {

			public void run() {
				handler.removeCallbacks(jobRunner);
				for (Iterator<Job> it = jobQueue.iterator(); it.hasNext();) {
					Job job = it.next();
					if (job.toDo == r) {
						it.remove();
						job.recycle();
					}
				}
				handler.post(jobRunner);
			}
		});
		;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MissionTimer [missionTimeMillis()=" + missionTimeMillis()
				+ ", isPaused()=" + isPaused() + "]";
	}

}
