import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href=
 *      "http://www.ibm.com/developerworks/library/j-jtp0730/index.html">Java
 *      Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {

	/**
	 * Pool of worker threads that will wait in the background until work is
	 * available.
	 */
	private final PoolWorker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;

	private int pending;

	private static final Logger logger = LogManager.getLogger();

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads
	 *            number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];
		pending = 0;
		shutdown = false;

		// start the threads so they are waiting in the background
		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
		}
	}

	/**
	 * Indicates that we now have additional "pending" work to wait for.
	 */
	private void increasePending() {
		synchronized (queue) {
			pending++;
			logger.debug("increasing, Pending is now {}", pending);
		}
	}

	/**
	 * Indicates that we now have one less "pending" work, and will notify any
	 * waiting threads if we no longer have any more pending work left.
	 */
	private void decreasePending() {
		synchronized (queue) {
			pending--;
			logger.debug("decreasing, Pending is now {}", pending);
			if (pending <= 0) {
				queue.notifyAll();
			}
		}
	}

	/**
	 * Adds a work request to the queue. A thread will process this request when
	 * available.
	 *
	 * @param r
	 *            work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable r) {
		synchronized (queue) {
			queue.addLast(r);
			increasePending();
			queue.notifyAll();
		}
	}

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done. This is useful for shutting down the work queue.
	 */
	public void finish() {
		try {
			synchronized (queue) {
				while (pending > 0) {
					// logger.debug("Waiting until finished");
					queue.wait();
					// logger.debug("waiting");
				}
			}
		} catch (InterruptedException e) {
			logger.debug("Finish interrupted", e);
		}
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}
		logger.debug("work queue been shut down.");
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will
	 * remove the work from the queue and run it. If a shutdown is detected,
	 * will exit instead of grabbing new work from the queue. These threads will
	 * continue running in the background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {
			Runnable r = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {
							queue.wait();
						} catch (InterruptedException ex) {
							System.err
									.println("Warning: Work queue interrupted "
											+ "while waiting.");
							Thread.currentThread().interrupt();
						}
					}

					// exit while for one of two reasons:
					// (a) queue has work, or (b) shutdown has been called

					if (shutdown) {
						break;
					}
					else {
						r = queue.removeFirst();
					}
				}

				try {
					r.run();
				} catch (RuntimeException ex) {
					// catch runtime exceptions to avoid leaking threads
					// ex.printStackTrace();
					System.err.println("Warning: Work queue encountered an "
							+ "exception while running." + ex.getMessage());
					ex.printStackTrace();
					// }
				} finally {
					decreasePending();
				}
			}
		}
	}
}
