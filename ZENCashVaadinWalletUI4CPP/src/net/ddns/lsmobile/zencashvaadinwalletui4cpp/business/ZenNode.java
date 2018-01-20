package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.OSUtil.OS_TYPE;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZCashClientCaller.NetworkAndBlockchainInfo;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZCashClientCaller.WalletCallException;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZCashInstallationObserver.DAEMON_STATUS;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZCashInstallationObserver.DaemonInfo;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.ZCashInstallationObserver.InstallationDetectionException;

public class ZenNode implements IConfig{
	
    private static final int POLL_PERIOD = 1500;
    private static final int STARTUP_ERROR_CODE = -28;

	public ZCashInstallationObserver installationObserver;
	public ZCashClientCaller         clientCaller;
	public volatile boolean connected = false;

	public void connect () {

        try
        {
        	final OS_TYPE os = OSUtil.getOSType();
        	
        	if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS))
        	{
        		possiblyCreateZENConfigFile();
        	}
        	
        	log.info("Starting ZENCash Swing Wallet ...");
        	log.info("OS: " + System.getProperty("os.name") + " = " + os);
        	log.info("Current directory: " + new File(".").getCanonicalPath());//TODO LS --C:\Program Files\XDEV Software\RapidClipse
        	log.info("Class path: " + System.getProperty("java.class.path"));
        	log.info("Environment PATH: " + System.getenv("PATH"));
            
            // If zend is currently not running, do a startup of the daemon as a child process
            // It may be started but not ready - then also show dialog
        	this.installationObserver =
            	ZCashInstallationObserver.getInstance();
            final DaemonInfo zcashdInfo = this.installationObserver.getDaemonInfo();
            
            this.clientCaller = ZCashClientCaller.getInstance();
            boolean daemonStartInProgress = false;
            try
            {
            	if (zcashdInfo.status == DAEMON_STATUS.RUNNING)
            	{
            		final NetworkAndBlockchainInfo info = this.clientCaller.getNetworkAndBlockchainInfo();
            		// If more than 20 minutes behind in the blockchain - startup in progress
            		if ((System.currentTimeMillis() - info.lastBlockDate.getTime()) > (20 * 60 * 1000))
            		{
            			log.info("Current blockchain synchronization date is "  +
            		                       new Date(info.lastBlockDate.getTime()));
            			daemonStartInProgress = true;
            		}
            	}
            } catch (final WalletCallException wce)
            {
                if ((wce.getMessage().indexOf("{\"code\":-28") != -1) || // Started but not ready
                	(wce.getMessage().indexOf("error code: -28") != -1))
                {
                	log.info("zend is currently starting...");
                	daemonStartInProgress = true;
                }
            }
            
            if ((zcashdInfo.status != DAEMON_STATUS.RUNNING) || (daemonStartInProgress))
            {
            	log.info(
            		"zend is not runing at the moment or has not started/synchronized 100% - showing splash...");
	            waitForStartup();
            }
            
            // Main GUI is created here
//            final ZCashUI ui = new ZCashUI(startupBar);
//            ui.setVisible(true);
            
            this.connected = true;
            
        } catch (final InstallationDetectionException ide)
        {
        	log.error("Unexpected error: ", ide);
//        	Notification.show("Installation error",
//        			"This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
//                    ide.getMessage() + "\n" +
//                    "See the console output for more detailed error information!",
//                     Type.ERROR_MESSAGE);
        	//TODO LS throw ide;
//            System.exit(1);
        } catch (final WalletCallException wce)
        {
        	log.error("Unexpected error: ", wce);

            if ((wce.getMessage().indexOf("{\"code\":-28,\"message\"") != -1) ||
            	(wce.getMessage().indexOf("error code: -28") != -1))
            {
            	log.error("Wallet communication error. It appears that zend has been started but is not ready to accept wallet " +
                        "connections. It is still loading the wallet and blockchain. Please try to " +
                        "start the GUI wallet later...");
//            	Notification.show("Wallet communication error", "It appears that zend has been started but is not ready to accept wallet\n" +
//                        "connections. It is still loading the wallet and blockchain. Please try to \n" +
//                        "start the GUI wallet later...", Type.ERROR_MESSAGE);
            } else
            {
            	log.error("Wallet communication error, There was a problem communicating with the ZENCash daemon/wallet. " +
                        "Please ensure that the ZENCash server zend is started (e.g. via " +
                        "command  \"zend --daemon\"). Error message is: " +
                         wce.getMessage() +
                        "See the console output for more detailed error information!");
//            	Notification.show("Wallet communication error", "There was a problem communicating with the ZENCash daemon/wallet. \n" +
//                        "Please ensure that the ZENCash server zend is started (e.g. via \n" +
//                        "command  \"zend --daemon\"). Error message is: \n" +
//                         wce.getMessage() +
//                        "See the console output for more detailed error information!", Type.ERROR_MESSAGE);
            }
          //TODO LS throw wce;
//            System.exit(2);
        } catch (final Exception e)
        {
        	log.error("Unexpected error: ", e);
//        	Notification.show("Error", "A general unexpected critical error has occurred: \n" + e.getMessage() + "\n" +
//                    "See the console output for more detailed error information!", Type.ERROR_MESSAGE);
        	//TODO LS throw e;
//            System.exit(3);
        } catch (final Error err)
        {
        	// Last resort catch for unexpected problems - just to inform the user
        	log.error("Unexpected error: ", err);
            err.printStackTrace();
//            Notification.show("Error", "A general unexpected critical/unrecoverable error has occurred: \n" + err.getMessage() + "\n" +
//                    "See the console output for more detailed error information!", Type.ERROR_MESSAGE);
          //TODO LS throw err;
//            System.exit(4);
        }
	}
	
    public static void possiblyCreateZENConfigFile()
            throws IOException
        {
        	final String blockchainDir = OSUtil.getBlockchainDirectory();
        	final File dir = new File(blockchainDir);
        	
    		if (!dir.exists())
    		{
    			if (!dir.mkdirs())
    			{
    				log.error("ERROR: Could not create settings directory: " + dir.getCanonicalPath());
    				throw new IOException("Could not create settings directory: " + dir.getCanonicalPath());
    			}
    		}
    		
    		final File zenConfigFile = new File(dir, "zen.conf");
    		
    		if (!zenConfigFile.exists())
    		{
    			log.info("ZEN configuration file " + zenConfigFile.getCanonicalPath() +
    					 " does not exist. It will be created with default settings.");
    			
    			final Random r = new Random(System.currentTimeMillis());
    			
    			final PrintStream configOut = new PrintStream(new FileOutputStream(zenConfigFile));
    			
    			configOut.println("#############################################################################");
    			configOut.println("#                         ZEN configuration file                            #");
    			configOut.println("#############################################################################");
    			configOut.println("# This file has been automatically generated by the ZENCash GUI wallet with #");
    			configOut.println("# default settings. It may be further cutsomized by hand only.              #");
    			configOut.println("#############################################################################");
    			configOut.println("# Creation date: " + new Date().toString());
    			configOut.println("#############################################################################");
    			configOut.println("");
    			configOut.println("# The rpcuser/rpcpassword are used for the local call to zend");
    			configOut.println("rpcuser=User" + Math.abs(r.nextInt()));
    			configOut.println("rpcpassword=Pass" + Math.abs(r.nextInt()) + "" +
    			                                       Math.abs(r.nextInt()) + "" +
    					                               Math.abs(r.nextInt()));
    			configOut.println("");
    			
    			/*
    			 * This is not necessary as of release:
    			 *  https://github.com/ZencashOfficial/zen/releases/tag/v2.0.9-3-b8d2ebf
    			configOut.println("# Well-known nodes to connect to - to speed up acquiring initial connections");
    			configOut.println("addnode=zpool.blockoperations.com");
    			configOut.println("addnode=luckpool.org:8333");
    			configOut.println("addnode=zencash.cloud");
    			configOut.println("addnode=zen.suprnova.cc");
    			configOut.println("addnode=zen.bitfire.one");
    			configOut.println("addnode=zenmine.pro");
    			*/
    			
    			configOut.close();
    		}
        }
	
    
    
    public void waitForStartup() throws IOException,
        InterruptedException,WalletCallException,InvocationTargetException {
        
        // special handling of Windows/Mac OS app launch
    	final OS_TYPE os = OSUtil.getOSType();
        if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS))
        {
            final ProvingKeyFetcher keyFetcher = new ProvingKeyFetcher();
            keyFetcher.fetchIfMissing(/*this*/);
        }
        
        log.info("Splash: checking if zend is already running...");
        boolean shouldStartZCashd = false;
        try {
            this.clientCaller.getDaemonRawRuntimeInfo();
        } catch (final IOException e) {
        	// Relying on a general exception may be unreliable
        	// may be thrown for an unexpected reason!!! - so message is checked
        	if (e.getMessage() != null &&
        		e.getMessage().toLowerCase(Locale.ROOT).contains("error: couldn't connect to server"))
        	{
        		shouldStartZCashd = true;
        	}
        }
        
        if (!shouldStartZCashd) {
        	log.info("Splash: zend already running...");
            // What if started by hand but taking long to initialize???
//            doDispose();
//            return;
        } else
        {
        	new InstallationDetectionException ("Splash: zend must be started...");
        }
    }
    
    
    // Custom code - to allow JDK7 compilation.
    public boolean isAlive(final Process p)
    {
    	if (p == null)
    	{
    		return false;
    	}
    	
        try
        {
            final int val = p.exitValue();
            
            return false;
        } catch (final IllegalThreadStateException itse)
        {
            return true;
        }
    }
    
    
    // Custom code - to allow JDK7 compilation.
    public boolean waitFor(final Process p, final long interval)
    {
		synchronized (this)
		{
			final long startWait = System.currentTimeMillis();
			long endWait = startWait;
			do
			{
				final boolean ended = !isAlive(p);
				
				if (ended)
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
