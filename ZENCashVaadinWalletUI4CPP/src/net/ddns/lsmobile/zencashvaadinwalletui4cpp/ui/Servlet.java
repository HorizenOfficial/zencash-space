
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;

import javax.servlet.annotation.WebServlet;

import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaklinov.zcashui.Log;
import com.vaklinov.zcashui.OSUtil;
import com.vaklinov.zcashui.OSUtil.OS_TYPE;
import com.vaklinov.zcashui.StartupProgressDialog;
import com.vaklinov.zcashui.StatusUpdateErrorReporter;
import com.vaklinov.zcashui.ZCashClientCaller;
import com.vaklinov.zcashui.ZCashClientCaller.NetworkAndBlockchainInfo;
import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;
import com.vaklinov.zcashui.ZCashInstallationObserver;
import com.vaklinov.zcashui.ZCashInstallationObserver.DAEMON_STATUS;
import com.vaklinov.zcashui.ZCashInstallationObserver.DaemonInfo;
import com.vaklinov.zcashui.ZCashInstallationObserver.InstallationDetectionException;
import com.xdev.communication.XdevServlet;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.desktop.DesktopUI;

@WebServlet(value = "/*", asyncSupported = true)
public class Servlet extends XdevServlet implements IConfig {

	public ZCashInstallationObserver installationObserver;
	public ZCashClientCaller         clientCaller;
	public StatusUpdateErrorReporter errorReporter = new StatusUpdateErrorReporter(/*this*/);

	public Servlet() throws IOException {
		super();
		
        try
        {
        	final OS_TYPE os = OSUtil.getOSType();
        	
        	if ((os == OS_TYPE.WINDOWS) || (os == OS_TYPE.MAC_OS))
        	{
        		possiblyCreateZENConfigFile();
        	}
        	
        	Log.info("Starting ZENCash Swing Wallet ...");
        	Log.info("OS: " + System.getProperty("os.name") + " = " + os);
        	Log.info("Current directory: " + new File(".").getCanonicalPath());//LS TODO --C:\Program Files\XDEV Software\RapidClipse
        	Log.info("Class path: " + System.getProperty("java.class.path"));
        	Log.info("Environment PATH: " + System.getenv("PATH"));
            
            // If zend is currently not running, do a startup of the daemon as a child process
            // It may be started but not ready - then also show dialog
        	this.installationObserver =
            	new ZCashInstallationObserver(ZEN_DIRECTORY/*OSUtil.getProgramDirectory()*/);
            final DaemonInfo zcashdInfo = this.installationObserver.getDaemonInfo();
            
            this.clientCaller = new ZCashClientCaller(ZEN_DIRECTORY/*OSUtil.getProgramDirectory()*/);
            boolean daemonStartInProgress = false;
            try
            {
            	if (zcashdInfo.status == DAEMON_STATUS.RUNNING)
            	{
            		final NetworkAndBlockchainInfo info = this.clientCaller.getNetworkAndBlockchainInfo();
            		// If more than 20 minutes behind in the blockchain - startup in progress
            		if ((System.currentTimeMillis() - info.lastBlockDate.getTime()) > (20 * 60 * 1000))
            		{
            			Log.info("Current blockchain synchronization date is "  +
            		                       new Date(info.lastBlockDate.getTime()));
            			daemonStartInProgress = true;
            		}
            	}
            } catch (final WalletCallException wce)
            {
                if ((wce.getMessage().indexOf("{\"code\":-28") != -1) || // Started but not ready
                	(wce.getMessage().indexOf("error code: -28") != -1))
                {
                	Log.info("zend is currently starting...");
                	daemonStartInProgress = true;
                }
            }
            
            StartupProgressDialog startupBar = null;
            if ((zcashdInfo.status != DAEMON_STATUS.RUNNING) || (daemonStartInProgress))
            {
            	Log.info(
            		"zend is not runing at the moment or has not started/synchronized 100% - showing splash...");
	            startupBar = new StartupProgressDialog(this.clientCaller);
//	            startupBar.setVisible(true);
	            startupBar.waitForStartup();
            }
            
            // Main GUI is created here
//            final ZCashUI ui = new ZCashUI(startupBar);
//            ui.setVisible(true);

        } catch (final InstallationDetectionException ide)
        {
        	Log.error("Unexpected error: ", ide);
        	Notification.show("This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
                    ide.getMessage() + "\n" +
                    "See the console output for more detailed error information!",
                    "Installation error", Type.HUMANIZED_MESSAGE);
//            System.exit(1);
        } catch (final WalletCallException wce)
        {
        	Log.error("Unexpected error: ", wce);

            if ((wce.getMessage().indexOf("{\"code\":-28,\"message\"") != -1) ||
            	(wce.getMessage().indexOf("error code: -28") != -1))
            {
            	Notification.show("It appears that zend has been started but is not ready to accept wallet\n" +
                        "connections. It is still loading the wallet and blockchain. Please try to \n" +
                        "start the GUI wallet later...", Type.HUMANIZED_MESSAGE);
            } else
            {
            	Notification.show("There was a problem communicating with the ZENCash daemon/wallet. \n" +
                        "Please ensure that the ZENCash server zend is started (e.g. via \n" +
                        "command  \"zend --daemon\"). Error message is: \n" +
                         wce.getMessage() +
                        "See the console output for more detailed error information!", Type.HUMANIZED_MESSAGE);
            }
//            System.exit(2);
        } catch (final Exception e)
        {
        	Log.error("Unexpected error: ", e);
        	Notification.show("A general unexpected critical error has occurred: \n" + e.getMessage() + "\n" +
                    "See the console output for more detailed error information!", Type.HUMANIZED_MESSAGE);
//            System.exit(3);
        } catch (final Error err)
        {
        	// Last resort catch for unexpected problems - just to inform the user
            err.printStackTrace();
            Notification.show("A general unexpected critical/unrecoverable error has occurred: \n" + err.getMessage() + "\n" +
                    "See the console output for more detailed error information!", Type.HUMANIZED_MESSAGE);
//            System.exit(4);
        }
    
	}

	@Override
	protected void initSession(final SessionInitEvent event) {
		super.initSession(event);

		event.getSession().addUIProvider(new ServletUIProvider());
	}

	/**
	 * UIProvider which provides different UIs depending on the caller's device.
	 */
	private static class ServletUIProvider extends UIProvider {
		@Override
		public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
//			final ClientInfo client = ClientInfo.getCurrent();
//			if (client != null) {
//				if (client.isMobile()) {
//					return PhoneUI.class;
//				}
//				if (client.isTablet()) {
//					return TabletUI.class;
//				}
//			}
			return DesktopUI.class;
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
    				Log.error("ERROR: Could not create settings directory: " + dir.getCanonicalPath());
    				throw new IOException("Could not create settings directory: " + dir.getCanonicalPath());
    			}
    		}
    		
    		final File zenConfigFile = new File(dir, "zen.conf");
    		
    		if (!zenConfigFile.exists())
    		{
    			Log.info("ZEN configuration file " + zenConfigFile.getCanonicalPath() +
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

}