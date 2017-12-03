// Code was originally written by developer - https://github.com/zlatinb
// Taken from repository https://github.com/zlatinb/zcash-swing-wallet-ui under an MIT license
package com.vaklinov.zcashui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.vaklinov.zcashui.OSUtil.OS_TYPE;
import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;


public class StartupProgressDialog /*extends JFrame*/ {
    

    private static final int POLL_PERIOD = 1500;
    private static final int STARTUP_ERROR_CODE = -28;
    
//    private final BorderLayout borderLayout1 = new BorderLayout();
//    private final JLabel imageLabel = new JLabel();
//    private final JLabel progressLabel = new JLabel();
//    private final JPanel southPanel = new JPanel();
//    private final BorderLayout southPanelLayout = new BorderLayout();
//    private final JProgressBar progressBar = new JProgressBar();
//    private final ImageIcon imageIcon;
    
    private final ZCashClientCaller clientCaller;
    
    public StartupProgressDialog(final ZCashClientCaller clientCaller)
    {
        this.clientCaller = clientCaller;
        
//        final URL iconUrl = this.getClass().getClassLoader().getResource("images/ZEN-yellow.orange-logo.png");
//        this.imageIcon = new ImageIcon(iconUrl);
//        this.imageLabel.setIcon(this.imageIcon);
//        this.imageLabel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
//        final Container contentPane = getContentPane();
//        contentPane.setLayout(this.borderLayout1);
//        this.southPanel.setLayout(this.southPanelLayout);
//        this.southPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
//        contentPane.add(this.imageLabel, BorderLayout.NORTH);
//		final JLabel zcashWalletLabel = new JLabel(
//			"<html><span style=\"font-style:italic;font-weight:bold;font-size:2.2em\">" +
//		    "ZENCash Wallet</span></html>");
//		zcashWalletLabel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
//		// todo - place in a panel with flow center
//		final JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
//		tempPanel.add(zcashWalletLabel);
//		contentPane.add(tempPanel, BorderLayout.CENTER);
//        contentPane.add(this.southPanel, BorderLayout.SOUTH);
//        this.progressBar.setIndeterminate(true);
//        this.southPanel.add(this.progressBar, BorderLayout.NORTH);
//        this.progressLabel.setText("Starting...");
//        this.southPanel.add(this.progressLabel, BorderLayout.SOUTH);
//        pack();
//        setLocationRelativeTo(null);
//
//        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        
        Log.info("Splash: checking if zend is already running...");
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
        	Log.info("Splash: zend already running...");
            // What if started by hand but taking long to initialize???
//            doDispose();
//            return;
        } else
        {
        	Log.info("Splash: zend will be started...");
        }
        
        final Process daemonProcess =
        	shouldStartZCashd ? this.clientCaller.startDaemon() : null;
        
        Thread.sleep(POLL_PERIOD); // just a little extra
        
        int iteration = 0;
        while(true) {
        	iteration++;
            Thread.sleep(POLL_PERIOD);
            
            JsonObject info = null;
            
            try
            {
            	info = this.clientCaller.getDaemonRawRuntimeInfo();
            } catch (final IOException e)
            {
            	if (iteration > 4)
            	{
            		throw e;
            	} else
            	{
            		continue;
            	}
            }
            
            final JsonValue code = info.get("code");
            if (code == null || (code.asInt() != STARTUP_ERROR_CODE)) {
				break;
			}
            final String message = info.getString("message", "???");
//            setProgressText(message);
            
        }

        // doDispose(); - will be called later by the main GUI
        
        if (daemonProcess != null) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    @Override
				public void run() {
			    	Log.info("Stopping zend because we started it - now it is alive: " +
			        		           StartupProgressDialog.this.isAlive(daemonProcess));
			        try
			        {
			            StartupProgressDialog.this.clientCaller.stopDaemon();
			            final long start = System.currentTimeMillis();
			            
			            while (!StartupProgressDialog.this.waitFor(daemonProcess, 3000))
			            {
			            	final long end = System.currentTimeMillis();
			            	Log.info("Waiting for " + ((end - start) / 1000) + " seconds for zend to exit...");
			            	
			            	if (end - start > 10 * 1000)
			            	{
			            		StartupProgressDialog.this.clientCaller.stopDaemon();
			            		daemonProcess.destroy();
			            	}
			            	
			            	if (end - start > 1 * 60 * 1000)
			            	{
			            		break;
			            	}
			            }
			        
			            if (StartupProgressDialog.this.isAlive(daemonProcess)) {
			            	Log.info("zend is still alive although we tried to stop it. " +
			                                       "Hopefully it will stop later!");
			                    //System.out.println("zend is still alive, killing forcefully");
			                    //daemonProcess.destroyForcibly();
			                } else {
							Log.info("zend shut down successfully");
						}
			        } catch (final Exception bad) {
			        	Log.error("Couldn't stop zend!", bad);
			        }
			    }
			});
		}
        
    }
    
//    public void doDispose() {
//        SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				setVisible(false);
//				dispose();
//			}
//		});
//    }
    
//    public void setProgressText(final String text) {
//        SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				StartupProgressDialog.this.progressLabel.setText(text);
//			}
//	     });
//    }
    
    
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
					Log.error("Unexpected error: ", ie);
				}
				
				endWait = System.currentTimeMillis();
			} while ((endWait - startWait) <= interval);
		}
		
		return false;
    }
}
