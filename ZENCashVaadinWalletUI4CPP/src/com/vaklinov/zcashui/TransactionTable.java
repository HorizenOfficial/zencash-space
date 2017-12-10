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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;


/**
 * Table to be used for transactions - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class TransactionTable
	extends DataTable implements IConfig
{
	public TransactionTable(final Object[][] rowData, final Object[] columnNames,
			                final JFrame parent, final ZCashClientCaller caller,
			                final ZCashInstallationObserver installationObserver)
	{
		super(rowData, columnNames);
		final int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		final JMenuItem showDetails = new JMenuItem("Show details...");
		showDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelaratorKeyMask));
        this.popupMenu.add(showDetails);
        
        showDetails.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if ((TransactionTable.this.lastRow >= 0) && (TransactionTable.this.lastColumn >= 0))
				{
					try
					{
						String txID = TransactionTable.this.getModel().getValueAt(TransactionTable.this.lastRow, 6).toString();
						txID = txID.replaceAll("\"", ""); // In case it has quotes
						
						log.info("Transaction ID for detail dialog is: " + txID);
						final Map<String, String> details = caller.getRawTransactionDetails(txID);
						final String rawTrans = caller.getRawTransaction(txID);
						
						final DetailsDialog dd = new DetailsDialog(parent, details);
						dd.setVisible(true);
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
        
        
		final JMenuItem showInExplorer = new JMenuItem("Show in block explorer");
		showInExplorer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelaratorKeyMask));
        this.popupMenu.add(showInExplorer);
        
        showInExplorer.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if ((TransactionTable.this.lastRow >= 0) && (TransactionTable.this.lastColumn >= 0))
				{
					try
					{
						String txID = TransactionTable.this.getModel().getValueAt(TransactionTable.this.lastRow, 6).toString();
						txID = txID.replaceAll("\"", ""); // In case it has quotes
						
						log.info("Transaction ID for block explorer is: " + txID);
						// https://explorer.zcha.in/transactions/<ID>
						String urlPrefix = "https://explorer.zensystem.io/tx/";
						if (installationObserver.isOnTestNet())
						{
							urlPrefix = "https://explorer-testnet.zen-solutions.io/tx/";
						}
						
						Desktop.getDesktop().browse(new URL(urlPrefix + txID).toURI());
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
		
        final JMenuItem showMemoField = new JMenuItem("Get transaction memo");
        showMemoField.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelaratorKeyMask));
	    this.popupMenu.add(showMemoField);
    
        showMemoField.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if ((TransactionTable.this.lastRow >= 0) && (TransactionTable.this.lastColumn >= 0))
				{
					final Cursor oldCursor = parent.getCursor();
					try
					{
						String txID = TransactionTable.this.getModel().getValueAt(TransactionTable.this.lastRow, 6).toString();
						txID = txID.replaceAll("\"", ""); // In case it has quotes
						
						String acc = TransactionTable.this.getModel().getValueAt(TransactionTable.this.lastRow, 5).toString();
						acc = acc.replaceAll("\"", ""); // In case it has quotes
						
						final boolean isZAddress = Util.isZAddress(acc);
						if (!isZAddress)
						{
					        JOptionPane.showMessageDialog(
						            parent,
						            "The selected transaction does not have as destination a Z (private) \n" +
						            "address or it is unkonwn (not listed) and thus no memo information \n" +
						            "about this transaction is available.",
						            "Memo information is unavailable",
						            JOptionPane.ERROR_MESSAGE);
						    return;
						}
						
						
						log.info("Transaction ID for Memo field is: " + txID);
						log.info("Account for Memo field is: " + acc);
						parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						// TODO: some day support outgoing Z transactions
 						String MemoField = caller.getMemoField(acc, txID);
 						parent.setCursor(oldCursor);
 						log.info("Memo field is: " + MemoField);
 						
 						if (MemoField != null)
 						{
 							final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 							clipboard.setContents(new StringSelection(MemoField), null);
 							
 							MemoField = Util.blockWrapString(MemoField, 80);
 							JOptionPane.showMessageDialog(
 								parent,
 								"The memo contained in the transaction is: \n" + MemoField +
 								"\n\n" +
 								"(The memo has also been copied to the clipboard.)",
 								"Memo", JOptionPane.PLAIN_MESSAGE);
 						} else
 						{
					        JOptionPane.showMessageDialog(
						            parent,
						            "The selected transaction does not contain a memo field.",
						            "Memo field is not available...",
						            JOptionPane.ERROR_MESSAGE);
 						}
					} catch (final Exception ex)
					{
						parent.setCursor(oldCursor);
						log.error("", ex);
						// TODO: report exception to user
					}
				} else
				{
					// Log perhaps
				}
			}
        });
		
	} // End constructor


	
	
	private static class DetailsDialog
		extends JDialog
	{
		public DetailsDialog(final JFrame parent, final Map<String, String> details)
			throws UnsupportedEncodingException
		{
			this.setTitle("Transaction details...");
			this.setSize(600,  310);
		    this.setLocation(100, 100);
			this.setLocationRelativeTo(parent);
			this.setModal(true);
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			this.getContentPane().setLayout(new BorderLayout(0, 0));
			
			final JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
			tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			final JLabel infoLabel = new JLabel(
					"<html><span style=\"font-size:0.97em;\">" +
					"The table shows the information about the transaction with technical details as " +
					"they appear at ZENCash network level." +
				    "</span>");
			infoLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			tempPanel.add(infoLabel, BorderLayout.CENTER);
			this.getContentPane().add(tempPanel, BorderLayout.NORTH);
			
			final String[] columns = new String[] { "Name", "Value" };
			final String[][] data = new String[details.size()][2];
			int i = 0;
			int maxPreferredWidht = 400;
			for (final Entry<String, String> ent : details.entrySet())
			{
				if (maxPreferredWidht < (ent.getValue().length() * 6))
				{
					maxPreferredWidht = ent.getValue().length() * 6;
				}
				
				data[i][0] = ent.getKey();
				data[i][1] = ent.getValue();
				i++;
			}
			
			Arrays.sort(data, new Comparator<String[]>()
			{
			    @Override
				public int compare(final String[] o1, final String[] o2)
			    {
			    	return o1[0].compareTo(o2[0]);
			    }

			    @Override
				public boolean equals(final Object obj)
			    {
			    	return false;
			    }
			});
			
			final DataTable table = new DataTable(data, columns);
			table.getColumnModel().getColumn(0).setPreferredWidth(200);
			table.getColumnModel().getColumn(1).setPreferredWidth(maxPreferredWidht);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			final JScrollPane tablePane = new JScrollPane(
				table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			this.getContentPane().add(tablePane, BorderLayout.CENTER);

			// Lower close button
			final JPanel closePanel = new JPanel();
			closePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
			final JButton closeButon = new JButton("Close");
			closePanel.add(closeButon);
			this.getContentPane().add(closePanel, BorderLayout.SOUTH);

			closeButon.addActionListener(new ActionListener()
			{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						DetailsDialog.this.setVisible(false);
						DetailsDialog.this.dispose();
					}
			});

		}
		
		
	}
}
