package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import com.vaklinov.zcashui.DataGatheringThread;

public interface IWallet extends IConfig {
	// Lists of threads and timers that may be stopped if necessary
	static List<Timer> timers                   = new ArrayList<>();
	static List<DataGatheringThread<?>> threads = new ArrayList<>();

	default void stopThreadsAndTimers()
	{
		for (final Timer t : timers)
		{
			t.stop();
		}
		
		for (final DataGatheringThread<?> t : threads)
		{
			t.setSuspended(true);
		}
	}
	
	
	// Interval is in milliseconds
	// Returns true if all threads have ended, else false
	default boolean waitForEndOfThreads(final long interval)
	{
		synchronized (this)
		{
			final long startWait = System.currentTimeMillis();
			long endWait = startWait;
			do
			{
				boolean allEnded = true;
				for (final DataGatheringThread<?> t : threads)
				{
					if (t.isAlive())
					{
						allEnded = false;
					}
				}
				
				if (allEnded)
				{
					return true; // End here
				}
				
				try
				{
					this.wait(100);
				} catch (final InterruptedException ie)
				{
					// One of the rare cases where we do nothing
					log.error("Unexpected error: ", ie);
				}
				
				endWait = System.currentTimeMillis();
			} while ((endWait - startWait) <= interval);
		}
		
		return false;
	}
	
}
