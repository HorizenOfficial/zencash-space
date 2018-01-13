/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 * 
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;


/**
 * Provides miscellaneous operations for the wallet file.
 * 
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class WalletOperations implements IConfig
{
	private final ZCashUI parent;
	private final JTabbedPane tabs;
	
	private final ZCashInstallationObserver installationObserver;
	private final ZCashClientCaller         clientCaller;


	public WalletOperations(final ZCashUI parent,
			                final JTabbedPane tabs,
			                
			                final ZCashInstallationObserver installationObserver,
			                final ZCashClientCaller clientCaller)
        throws IOException, InterruptedException, WalletCallException
	{
		this.parent    = parent;
		this.tabs      = tabs;
		
		this.installationObserver = installationObserver;
		this.clientCaller = clientCaller;
	}

	
	public void exportWalletPrivateKeys()
	{
		// TODO: Will need corrections once encryption is reenabled!!!
		
		try
		{
			this.issueBackupDirectoryWarning();
			
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Export wallet private keys to file...");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setCurrentDirectory(OSUtil.getUserHomeDirectory());
			 
			final int result = fileChooser.showSaveDialog(this.parent);
			 
			if (result != JFileChooser.APPROVE_OPTION)
			{
			    return;
			}
			
			final File f = fileChooser.getSelectedFile();
			
			final Cursor oldCursor = this.parent.getCursor();
			String path = null;
			try
			{
				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							
				path = this.clientCaller.exportWallet(f.getName());
				
				this.parent.setCursor(oldCursor);
			} catch (final WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);
				log.error("Unexpected error: ", wce);
				
				JOptionPane.showMessageDialog(
					this.parent,
					"An unexpected error occurred while exporting wallet private keys!" +
					"\n" + wce.getMessage().replace(",", ",\n"),
					"Error in exporting wallet private keys...", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			JOptionPane.showMessageDialog(
				this.parent,
				"The wallet private keys have been exported successfully to file:\n" +
				f.getName() + "\n" +
				"in the backup directory provided to zend (-exportdir=<dir>).\nFull path is: " +
				path + "\n" +
				"You need to protect this file from unauthorized access. Anyone who\n" +
				"has access to the private keys can spend the ZENCash balance!",
				"Wallet private key export...", JOptionPane.INFORMATION_MESSAGE);
			
		} catch (final Exception e)
		{
			log.error(e);
		}
	}

	
	public void importWalletPrivateKeys()
	{
		// TODO: Will need corrections once encryption is re-enabled!!!
		
	    final int option = JOptionPane.showConfirmDialog(
		    this.parent,
		    "Private key import is a potentially slow operation. It may take\n" +
		    "several minutes during which the GUI will be non-responsive.\n" +
		    "The data to import must be in the format used by the option:\n" +
		    "\"Export private keys...\"\n\n" +
		    "Are you sure you wish to import private keys?",
		    "Private key import notice...",
		    JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.NO_OPTION)
		{
		  	return;
		}
		
		try
		{
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Import wallet private keys from file...");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			 
			final int result = fileChooser.showOpenDialog(this.parent);
			 
			if (result != JFileChooser.APPROVE_OPTION)
			{
			    return;
			}
			
			final File f = fileChooser.getSelectedFile();
			
			final Cursor oldCursor = this.parent.getCursor();
			try
			{
				this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							
				this.clientCaller.importWallet(f.getCanonicalPath());
				
				this.parent.setCursor(oldCursor);
			} catch (final WalletCallException wce)
			{
				this.parent.setCursor(oldCursor);
				log.error("Unexpected error: ", wce);
				
				JOptionPane.showMessageDialog(
					this.parent,
					"An unexpected error occurred while importing wallet private keys!" +
					"\n" + wce.getMessage().replace(",", ",\n"),
					"Error in importing wallet private keys...", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			JOptionPane.showMessageDialog(
				this.parent,
				"Wallet private keys have been imported successfully from location:\n" +
				f.getCanonicalPath() + "\n\n",
				"Wallet private key import...", JOptionPane.INFORMATION_MESSAGE);
			
		} catch (final Exception e)
		{
			log.error(e);
		}
	}
	
	
	public void importSinglePrivateKey()
	{
		try
		{
			final SingleKeyImportDialog kd = new SingleKeyImportDialog(this.parent, this.clientCaller);
			kd.setVisible(true);
			
		} catch (final Exception ex)
		{
			log.error(ex);
		}
	}
	
	
	private void issueBackupDirectoryWarning()
		throws IOException
	{
        final String userDir = OSUtil.getSettingsDirectory();
        final File warningFlagFile = new File(userDir + File.separator + "backupInfoShownNG.flag");
        if (warningFlagFile.exists())
        {
            return;
        }
            
        final int reply = JOptionPane.showOptionDialog(
            this.parent,
            "For security reasons the wallet may be backed up/private keys exported only if\n" +
            "the zend parameter -exportdir=<dir> has been set. If you started zend \n" +
            "manually, you ought to have provided this parameter. When zend is started \n" +
            "automatically by the GUI wallet the directory provided as parameter to -exportdir\n" +
            "is the user home directory: " + OSUtil.getUserHomeDirectory().getCanonicalPath() +"\n" +
            "Please navigate to the directory provided as -exportdir=<dir> and select a\n"+
            "filename in it to backup/export private keys. If you select another directory\n" +
            "instead, the destination file will still end up in the directory provided as \n" +
            "-exportdir=<dir>. If this parameter was not provided to zend, the process\n" +
            "will fail with a security check error. The filename needs to consist of only\n" +
            "alphanumeric characters (e.g. dot is not allowed).\n",
            "Wallet backup directory information",
	        JOptionPane.YES_NO_OPTION,
	        JOptionPane.INFORMATION_MESSAGE,
	        null, new String[] { "Do not show this again", "OK" },
	        JOptionPane.NO_OPTION);
	        
	    if (reply == JOptionPane.NO_OPTION)
	    {
	    	return;
	    }
	    
	    warningFlagFile.createNewFile();
	}
}
