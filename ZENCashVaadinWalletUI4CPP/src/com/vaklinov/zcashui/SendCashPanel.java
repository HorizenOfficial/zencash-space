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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;


/**
 * Provides the functionality for sending cash
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class SendCashPanel
	extends WalletTabPanel
{
	private ZCashClientCaller clientCaller;
	private StatusUpdateErrorReporter errorReporter;
	
	private JComboBox  balanceAddressCombo     = null;
	private JPanel     comboBoxParentPanel     = null;
	private String[][] lastAddressBalanceData  = null;
	private String[]   comboBoxItems           = null;
	private DataGatheringThread<String[][]> addressBalanceGatheringThread = null;
	
	private JTextField destinationAddressField = null;
	private JTextField destinationAmountField  = null;
	private JTextField destinationMemoField    = null;
	private JTextField transactionFeeField     = null;
	
	private JButton    sendButton              = null;
	
	private JPanel       operationStatusPanel        = null;
	private JLabel       operationStatusLabel        = null;
	private JProgressBar operationStatusProhgressBar = null;
	private Timer        operationStatusTimer        = null;
	private String       operationStatusID           = null;
	private int          operationStatusCounter      = 0;
	

	public SendCashPanel(final ZCashClientCaller clientCaller,  final StatusUpdateErrorReporter errorReporter)
		throws IOException, InterruptedException, WalletCallException
	{
		this.timers = new ArrayList<>();
		this.threads = new ArrayList<>();
		
		this.clientCaller = clientCaller;
		this.errorReporter = errorReporter;

		// Build content
		this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		this.setLayout(new BorderLayout());
		final JPanel sendCashPanel = new JPanel();
		this.add(sendCashPanel, BorderLayout.NORTH);
		sendCashPanel.setLayout(new BoxLayout(sendCashPanel, BoxLayout.Y_AXIS));
		sendCashPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(new JLabel("Send cash from:       "));
		tempPanel.add(new JLabel(
			"<html><span style=\"font-size:0.8em;\">" +
			"* Only addresses with a confirmed balance are shown as sources for sending!" +
		    "</span>  "));
		sendCashPanel.add(tempPanel);

		this.balanceAddressCombo = new JComboBox<>(new String[] { "" });
		this.comboBoxParentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.comboBoxParentPanel.add(this.balanceAddressCombo);
		sendCashPanel.add(this.comboBoxParentPanel);
		
		JLabel dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
		sendCashPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(new JLabel("Destination address:"));
		sendCashPanel.add(tempPanel);
		
		this.destinationAddressField = new JTextField(73);
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(this.destinationAddressField);
		sendCashPanel.add(tempPanel);
				
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
		sendCashPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(new JLabel("Memo (optional):     "));
		tempPanel.add(new JLabel(
				"<html><span style=\"font-size:0.8em;\">" +
				"* Memo may be specified only if the destination is a Z (Private) address!" +
			    "</span>  "));
		sendCashPanel.add(tempPanel);
		
		this.destinationMemoField = new JTextField(73);
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(this.destinationMemoField);
		sendCashPanel.add(tempPanel);
		
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
		sendCashPanel.add(dividerLabel);

		// Construct a more complex panel for the amount and transaction fee
		final JPanel amountAndFeePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		final JPanel amountPanel = new JPanel(new BorderLayout());
		amountPanel.add(new JLabel("Amount to send:"), BorderLayout.NORTH);
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(this.destinationAmountField = new JTextField(13));
		this.destinationAmountField.setHorizontalAlignment(SwingConstants.RIGHT);
		tempPanel.add(new JLabel(" ZEN    "));
		amountPanel.add(tempPanel, BorderLayout.SOUTH);

		final JPanel feePanel = new JPanel(new BorderLayout());
		feePanel.add(new JLabel("Transaction fee:"), BorderLayout.NORTH);
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(this.transactionFeeField = new JTextField(13));
		this.transactionFeeField.setText("0.0001"); // Default value
		this.transactionFeeField.setHorizontalAlignment(SwingConstants.RIGHT);
		tempPanel.add(new JLabel(" ZEN"));
		feePanel.add(tempPanel, BorderLayout.SOUTH);

		amountAndFeePanel.add(amountPanel);
		amountAndFeePanel.add(feePanel);
		sendCashPanel.add(amountAndFeePanel);
		
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
		sendCashPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(this.sendButton = new JButton("Send   \u27A4\u27A4\u27A4"));
		sendCashPanel.add(tempPanel);

		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 5));
		sendCashPanel.add(dividerLabel);
		
		final JPanel warningPanel = new JPanel();
		warningPanel.setLayout(new BorderLayout(7, 3));
		final JLabel warningL = new JLabel(
				"<html><span style=\"font-size:0.8em;\">" +
				" * When sending cash from a T (Transparent) address, the remining unspent balance is sent to another " +
				"auto-generated T address. When sending from a Z (Private) address, the remining unspent balance remains with " +
				"the Z address. In both cases the original sending address cannot be used for sending again until the " +
				"transaction is confirmed. The address is temporarily removed from the list! Freshly mined coins may only "+
				"be sent to a Z (Private) address." +
			    "</span>");
		warningPanel.add(warningL, BorderLayout.NORTH);
		sendCashPanel.add(warningPanel);
		
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 15));
		sendCashPanel.add(dividerLabel);
		
		// Build the operation status panel
		this.operationStatusPanel = new JPanel();
		sendCashPanel.add(this.operationStatusPanel);
		this.operationStatusPanel.setLayout(new BoxLayout(this.operationStatusPanel, BoxLayout.Y_AXIS));
		
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(new JLabel("Last operation status: "));
        tempPanel.add(this.operationStatusLabel = new JLabel("N/A"));
        this.operationStatusPanel.add(tempPanel);
		
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 6));
		this.operationStatusPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(new JLabel("Progress: "));
        tempPanel.add(this.operationStatusProhgressBar = new JProgressBar(0, 200));
        this.operationStatusProhgressBar.setPreferredSize(new Dimension(250, 17));
        this.operationStatusPanel.add(tempPanel);
        
		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 13));
		this.operationStatusPanel.add(dividerLabel);
		
		// Wire the buttons
		this.sendButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
			    {
					SendCashPanel.this.sendCash();
				} catch (final Exception ex)
				{
					Log.error("Unexpected error: ", ex);
					
					String errMessage = "";
					if (ex instanceof WalletCallException)
					{
						errMessage = ((WalletCallException)ex).getMessage().replace(",", ",\n");
					}
					
					JOptionPane.showMessageDialog(
							SendCashPanel.this.getRootPane().getParent(),
							"An unexpected error occurred when sending cash!\n" +
							"Please ensure that the ZENCash daemon is running and\n" +
							"parameters are correct. You may try again later...\n" +
							errMessage,
							"Error in sending cash", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// Update the balances via timer and data gathering thread
		this.addressBalanceGatheringThread = new DataGatheringThread<>(
			new DataGatheringThread.DataGatherer<String[][]>()
			{
				@Override
				public String[][] gatherData()
					throws Exception
				{
					final long start = System.currentTimeMillis();
					final String[][] data = SendCashPanel.this.getAddressPositiveBalanceDataFromWallet();
					final long end = System.currentTimeMillis();
					Log.info("Gathering of address/balance table data done in " + (end - start) + "ms." );
					
					return data;
				}
			},
			this.errorReporter, 10000, true);
		this.threads.add(this.addressBalanceGatheringThread);
		
		final ActionListener alBalancesUpdater = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					// TODO: if the user has opened the combo box - this closes it (maybe fix)
					SendCashPanel.this.updateWalletAddressPositiveBalanceComboBox();
				} catch (final Exception ex)
				{
					Log.error("Unexpected error: ", ex);
					SendCashPanel.this.errorReporter.reportError(ex);
				}
			}
		};
		final Timer timerBalancesUpdater = new Timer(15000, alBalancesUpdater);
		timerBalancesUpdater.setInitialDelay(3000);
		timerBalancesUpdater.start();
		this.timers.add(timerBalancesUpdater);
		
		// Add a popup menu to the destination address field - for convenience
		final JMenuItem paste = new JMenuItem("Paste address");
		final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(paste);
        paste.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					final String address = (String)Toolkit.getDefaultToolkit().getSystemClipboard().
							         getData(DataFlavor.stringFlavor);
					if ((address != null) && (address.trim().length() > 0))
					{
						SendCashPanel.this.destinationAddressField.setText(address);
					}
				} catch (final Exception ex)
				{
					Log.error("Unexpected error", ex);
					// TODO: clipboard exception handling - do it better
					// java.awt.datatransfer.UnsupportedFlavorException: Unicode String
					//SendCashPanel.this.errorReporter.reportError(ex);
				}
			}
		});
        
        this.destinationAddressField.addMouseListener(new MouseAdapter()
        {
        	@Override
			public void mousePressed(final MouseEvent e)
        	{
                if ((!e.isConsumed()) && e.isPopupTrigger())
                {
                    popupMenu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
                    e.consume();
                };
        	}
        	
            @Override
			public void mouseReleased(final MouseEvent e)
            {
            	if ((!e.isConsumed()) && e.isPopupTrigger())
            	{
            		mousePressed(e);
            	}
            }
        });
		
	}
	
	
	private void sendCash()
		throws WalletCallException, IOException, InterruptedException
	{
		if (this.balanceAddressCombo.getItemCount() <= 0)
		{
			JOptionPane.showMessageDialog(
				SendCashPanel.this.getRootPane().getParent(),
				"There are no addresses with a positive balance to send\n" +
				"cash from!",
				"No funds available", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (this.balanceAddressCombo.getSelectedIndex() < 0)
		{
			JOptionPane.showMessageDialog(
				SendCashPanel.this.getRootPane().getParent(),
				"Please select a source address with a current positive\n" +
				"balance to send cash from!",
				"Please select source address", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		final String sourceAddress = this.lastAddressBalanceData[this.balanceAddressCombo.getSelectedIndex()][1];
		final String destinationAddress = this.destinationAddressField.getText();
		final String memo = this.destinationMemoField.getText();
		final String amount = this.destinationAmountField.getText();
		final String fee = this.transactionFeeField.getText();

		// Verify general correctness.
		String errorMessage = null;
		
		if ((sourceAddress == null) || (sourceAddress.trim().length() <= 20))
		{
			errorMessage = "Source address is invalid; it is too short or missing.";
		} else if (sourceAddress.length() > 512)
		{
			errorMessage = "Source address is invalid; it is too long.";
		}
		
		// TODO: full address validation
		if ((destinationAddress == null) || (destinationAddress.trim().length() <= 0))
		{
			errorMessage = "Destination address is invalid; it is missing.";
		} else if (destinationAddress.trim().length() <= 20)
		{
			errorMessage = "Destination address is invalid; it is too short.";
		} else if (destinationAddress.length() > 512)
		{
			errorMessage = "Destination address is invalid; it is too long.";
		}
		
		if ((amount == null) || (amount.trim().length() <= 0))
		{
			errorMessage = "Amount to send is invalid; it is missing.";
		} else
		{
			try
			{
				final double d = Double.valueOf(amount);
			} catch (final NumberFormatException nfe)
			{
				errorMessage = "Amount to send is invalid; it is not a number.";
			}
		}
		
		if ((fee == null) || (fee.trim().length() <= 0))
		{
			errorMessage = "Transaction fee is invalid; it is missing.";
		} else
		{
			try
			{
				final double d = Double.valueOf(fee);
			} catch (final NumberFormatException nfe)
			{
				errorMessage = "Transaction fee is invalid; it is not a number.";
			}
		}


		if (errorMessage != null)
		{
			JOptionPane.showMessageDialog(
				SendCashPanel.this.getRootPane().getParent(),
				errorMessage, "Sending parameters are incorrect", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Check for encrypted wallet
		final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
		if (bEncryptedWallet)
		{
			final PasswordDialog pd = new PasswordDialog((JFrame)(SendCashPanel.this.getRootPane().getParent()));
			pd.setVisible(true);
			
			if (!pd.isOKPressed())
			{
				return;
			}
			
			this.clientCaller.unlockWallet(pd.getPassword());
		}
		
		// Call the wallet send method
		this.operationStatusID = this.clientCaller.sendCash(sourceAddress, destinationAddress, amount, memo, fee);
				
		// Disable controls after send
		this.sendButton.setEnabled(false);
		this.balanceAddressCombo.setEnabled(false);
		this.destinationAddressField.setEnabled(false);
		this.destinationAmountField.setEnabled(false);
		this.destinationMemoField.setEnabled(false);
		this.transactionFeeField.setEnabled(false);
		
		// Start a data gathering thread specific to the operation being executed - this is done is a separate
		// thread since the server responds more slowly during JoinSPlits and this blocks he GUI somewhat.
		final DataGatheringThread<Boolean> opFollowingThread = new DataGatheringThread<>(
			new DataGatheringThread.DataGatherer<Boolean>()
			{
				@Override
				public Boolean gatherData()
					throws Exception
				{
					final long start = System.currentTimeMillis();
					final Boolean result = SendCashPanel.this.clientCaller.isSendingOperationComplete(SendCashPanel.this.operationStatusID);
					final long end = System.currentTimeMillis();
					Log.info("Checking for operation " + SendCashPanel.this.operationStatusID + " status done in " + (end - start) + "ms." );
					
					return result;
				}
			},
			this.errorReporter, 2000, true);
		
		// Start a timer to update the progress of the operation
		this.operationStatusCounter = 0;
		this.operationStatusTimer = new Timer(2000, new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					// TODO: Handle errors in case of restarted server while wallet is sending ...
					final Boolean opComplete = opFollowingThread.getLastData();
					
					if ((opComplete != null) && opComplete.booleanValue())
					{
						// End the special thread used to follow the operation
						opFollowingThread.setSuspended(true);
						
						if (SendCashPanel.this.clientCaller.isCompletedOperationSuccessful(SendCashPanel.this.operationStatusID))
						{
							SendCashPanel.this.operationStatusLabel.setText(
								"<html><span style=\"color:green;font-weight:bold\">SUCCESSFUL</span></html>");
							JOptionPane.showMessageDialog(
									SendCashPanel.this.getRootPane().getParent(),
									"Succesfully sent " + amount + " ZEN from address: \n" +
									sourceAddress + "\n" +
									"to address: \n" +
									destinationAddress + "\n",
									"Cash sent successfully", JOptionPane.INFORMATION_MESSAGE);
						} else
						{
							final String errorMessage = SendCashPanel.this.clientCaller.getOperationFinalErrorMessage(SendCashPanel.this.operationStatusID);
							SendCashPanel.this.operationStatusLabel.setText(
								"<html><span style=\"color:red;font-weight:bold\">ERROR: " + errorMessage + "</span></html>");

							JOptionPane.showMessageDialog(
									SendCashPanel.this.getRootPane().getParent(),
									"An error occurred when sending cash. Error message is:\n" +
									errorMessage + "\n\n" +
									"Please ensure that sending parameters are correct. You may try again later...\n",
									"Error in sending cash", JOptionPane.ERROR_MESSAGE);

						}
						
						// Lock the wallet again
						if (bEncryptedWallet)
						{
							SendCashPanel.this.clientCaller.lockWallet();
						}
						
						// Restore controls etc.
						SendCashPanel.this.operationStatusCounter = 0;
						SendCashPanel.this.operationStatusID      = null;
						SendCashPanel.this.operationStatusTimer.stop();
						SendCashPanel.this.operationStatusTimer = null;
						SendCashPanel.this.operationStatusProhgressBar.setValue(0);
						
						SendCashPanel.this.sendButton.setEnabled(true);
						SendCashPanel.this.balanceAddressCombo.setEnabled(true);
						SendCashPanel.this.destinationAddressField.setEnabled(true);
						SendCashPanel.this.destinationAmountField.setEnabled(true);
						SendCashPanel.this.transactionFeeField.setEnabled(true);
						SendCashPanel.this.destinationMemoField.setEnabled(true);
					} else
					{
						// Update the progress
						SendCashPanel.this.operationStatusLabel.setText(
							"<html><span style=\"color:orange;font-weight:bold\">IN PROGRESS</span></html>");
						SendCashPanel.this.operationStatusCounter += 2;
						int progress = 0;
						if (SendCashPanel.this.operationStatusCounter <= 100)
						{
							progress = SendCashPanel.this.operationStatusCounter;
						} else
						{
							progress = 100 + (((SendCashPanel.this.operationStatusCounter - 100) * 6) / 10);
						}
						SendCashPanel.this.operationStatusProhgressBar.setValue(progress);
					}
					
					SendCashPanel.this.repaint();
				} catch (final Exception ex)
				{
					Log.error("Unexpected error: ", ex);
					SendCashPanel.this.errorReporter.reportError(ex);
				}
			}
		});
		this.operationStatusTimer.setInitialDelay(0);
		this.operationStatusTimer.start();
	}

	
	public void prepareForSending(final String address)
	{
	    this.destinationAddressField.setText(address);
	}
	
	
	private void updateWalletAddressPositiveBalanceComboBox()
		throws WalletCallException, IOException, InterruptedException
	{
		final String[][] newAddressBalanceData = this.addressBalanceGatheringThread.getLastData();
		
		// The data may be null if nothing is yet obtained
		if (newAddressBalanceData == null)
		{
			return;
		}
		
		this.lastAddressBalanceData = newAddressBalanceData;
		
		this.comboBoxItems = new String[this.lastAddressBalanceData.length];
		for (int i = 0; i < this.lastAddressBalanceData.length; i++)
		{
			// Do numeric formatting or else we may get 1.1111E-5
			this.comboBoxItems[i] =
				new DecimalFormat("########0.00######").format(Double.valueOf(this.lastAddressBalanceData[i][0]))  +
				" - " + this.lastAddressBalanceData[i][1];
		}
		
		final int selectedIndex = this.balanceAddressCombo.getSelectedIndex();
		final boolean isEnabled = this.balanceAddressCombo.isEnabled();
		this.comboBoxParentPanel.remove(this.balanceAddressCombo);
		this.balanceAddressCombo = new JComboBox<>(this.comboBoxItems);
		this.comboBoxParentPanel.add(this.balanceAddressCombo);
		if ((this.balanceAddressCombo.getItemCount() > 0) &&
			(selectedIndex >= 0) &&
			(this.balanceAddressCombo.getItemCount() > selectedIndex))
		{
			this.balanceAddressCombo.setSelectedIndex(selectedIndex);
		}
		this.balanceAddressCombo.setEnabled(isEnabled);

		this.validate();
		this.repaint();
	}


	private String[][] getAddressPositiveBalanceDataFromWallet()
		throws WalletCallException, IOException, InterruptedException
	{
		// Z Addresses - they are OK
		final String[] zAddresses = this.clientCaller.getWalletZAddresses();
		
		// T Addresses created inside wallet that may be empty
		final String[] tAddresses = this.clientCaller.getWalletAllPublicAddresses();
		final Set<String> tStoredAddressSet = new HashSet<>();
		for (final String address : tAddresses)
		{
			tStoredAddressSet.add(address);
		}
		
		// T addresses with unspent outputs (even if not GUI created)...
		final String[] tAddressesWithUnspentOuts = this.clientCaller.getWalletPublicAddressesWithUnspentOutputs();
		final Set<String> tAddressSetWithUnspentOuts = new HashSet<>();
		for (final String address : tAddressesWithUnspentOuts)
		{
			tAddressSetWithUnspentOuts.add(address);
		}
		
		// Combine all known T addresses
		final Set<String> tAddressesCombined = new HashSet<>();
		tAddressesCombined.addAll(tStoredAddressSet);
		tAddressesCombined.addAll(tAddressSetWithUnspentOuts);
		
		final String[][] tempAddressBalances = new String[zAddresses.length + tAddressesCombined.size()][];
		
		int count = 0;

		for (final String address : tAddressesCombined)
		{
			final String balance = this.clientCaller.getBalanceForAddress(address);
			if (Double.valueOf(balance) > 0)
			{
				tempAddressBalances[count++] = new String[]
				{
					balance, address
				};
			}
		}
		
		for (final String address : zAddresses)
		{
			final String balance = this.clientCaller.getBalanceForAddress(address);
			if (Double.valueOf(balance) > 0)
			{
				tempAddressBalances[count++] = new String[]
				{
					balance, address
				};
			}
		}

		final String[][] addressBalances = new String[count][];
		System.arraycopy(tempAddressBalances, 0, addressBalances, 0, count);
		
		return addressBalances;
	}
	
	
}
