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

import static net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig.log;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;


/**
 * Table to be used for addresses - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class AddressTable
	extends DataTable  implements IConfig
{
	public AddressTable(final Object[][] rowData, final Object[] columnNames,
			            final ZCashClientCaller caller)
	{
		super(rowData, columnNames);
		final int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
		final JMenuItem obtainPrivateKey = new JMenuItem("Obtain private key...");
		obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));
        this.popupMenu.add(obtainPrivateKey);
        
        obtainPrivateKey.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if ((AddressTable.this.lastRow >= 0) && (AddressTable.this.lastColumn >= 0))
				{
					try
					{
						final String address = AddressTable.this.getModel().getValueAt(AddressTable.this.lastRow, 2).toString();
						final boolean isZAddress = Util.isZAddress(address);
						
						// Check for encrypted wallet
						final boolean bEncryptedWallet = caller.isWalletEncrypted();
						if (bEncryptedWallet)
						{
							final PasswordDialog pd = new PasswordDialog((JFrame)(AddressTable.this.getRootPane().getParent()));
							pd.setVisible(true);
							
							if (!pd.isOKPressed())
							{
								return;
							}
							
							caller.unlockWallet(pd.getPassword());
						}
						
						final String privateKey = isZAddress ?
							caller.getZPrivateKey(address) : caller.getTPrivateKey(address);
							
						// Lock the wallet again
						if (bEncryptedWallet)
						{
							caller.lockWallet();
						}
							
						final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(new StringSelection(privateKey), null);
						
						JOptionPane.showMessageDialog(
							AddressTable.this.getRootPane().getParent(),
							(isZAddress ? "Z (Private)" : "T (Transparent)") +  " address:\n" +
							address + "\n" +
							"has private key:\n" +
							privateKey + "\n\n" +
							"The private key has also been copied to the clipboard.",
							"Private key information", JOptionPane.INFORMATION_MESSAGE);

						
					} catch (final Exception ex)
					{
						log.error("Unexpected error: ", ex);
						// TODO: report exception to user
					}
				} else
				{
					// Log perhaps
				}
			}
		});
	} // End constructor

}
