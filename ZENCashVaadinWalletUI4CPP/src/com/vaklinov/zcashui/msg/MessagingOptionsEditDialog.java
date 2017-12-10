/************************************************************************************************
 *   ____________ _   _  _____          _      _____ _    _ _______          __   _ _      _
 *  |___  /  ____| \ | |/ ____|        | |    / ____| |  | |_   _\ \        / /  | | |    | |
 *     / /| |__  |  \| | |     __ _ ___| |__ | |  __| |  | | | |  \ \  /\  / /_ _| | | ___| |_
 *    / / |  __| | . ` | |    / _` / __| '_ \| | |_ | |  | | | |   \ \/  \/ / _` | | |/ _ \ __|
 *   / /__| |____| |\  | |___| (_| \__ \ | | | |__| | |__| |_| |_   \  /\  / (_| | | |  __/ |_
 *  /_____|______|_| \_|\_____\__,_|___/_| |_|\_____|\____/|_____|   \/  \/ \__,_|_|_|\___|\__|
 * 
 * Copyright (c) 2017 Ivan Vaklinov <ivan@vaklinov.com>
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
package com.vaklinov.zcashui.msg;

import static net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.vaklinov.zcashui.StatusUpdateErrorReporter;
import com.vaklinov.zcashui.Util;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;


/**
 * Dialog showing the messaging options and allowing them to be edited.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class MessagingOptionsEditDialog
	extends JDialog implements IConfig
{
	protected JFrame parentFrame;
	protected MessagingStorage storage;
	protected StatusUpdateErrorReporter errorReporter;
	
	protected JLabel infoLabel;
	protected JPanel buttonPanel;
	
	protected JTextField amountTextField;
	protected JTextField transactionFeeTextField;
	protected JCheckBox  automaticallyAddUsers;
	
	public MessagingOptionsEditDialog(final JFrame parentFrame, final MessagingStorage storage, final StatusUpdateErrorReporter errorReporter)
		throws IOException
	{
		this.parentFrame   = parentFrame;
		this.storage       = storage;
		this.errorReporter = errorReporter;
		
		this.setTitle("Messaging options");
		this.setModal(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		final MessagingOptions options = this.storage.getMessagingOptions();
			
		this.getContentPane().setLayout(new BorderLayout(0, 0));
			
		final JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
		tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.infoLabel = new JLabel(
				"<html><span style=\"font-size:0.93em;\">" +
				"The options below pertain to messaging. It is posisble to set the amount of ZEN<br/>" +
				"to be sent with every messaging transaction and also the transaction fee. It is<br/>" +
			    "also possible to decide if users are to be automatically added to the contact list.<br/><br/>" +
			    "</span>");
	    tempPanel.add(this.infoLabel, BorderLayout.CENTER);
		this.getContentPane().add(tempPanel, BorderLayout.NORTH);
			
		final JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
		
		addFormField(detailsPanel, "Automatically add users to contact list:",
				     this.automaticallyAddUsers = new JCheckBox());
		addFormField(detailsPanel, "ZEN amount to send with every message:",   this.amountTextField = new JTextField(12));
		addFormField(detailsPanel, "Transaction fee:",  this.transactionFeeTextField = new JTextField(12));
		
		final DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);
		this.automaticallyAddUsers.setSelected(options.isAutomaticallyAddUsersIfNotExplicitlyImported());
		this.amountTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getAmountToSend()));
		this.transactionFeeTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getTransactionFee()));
		
		detailsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.getContentPane().add(detailsPanel, BorderLayout.CENTER);

		// Lower buttons - by default only close is available
		this.buttonPanel = new JPanel();
		this.buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 3));
		final JButton closeButon = new JButton("Close");
		this.buttonPanel.add(closeButon);
		this.getContentPane().add(this.buttonPanel, BorderLayout.SOUTH);

		closeButon.addActionListener(new ActionListener()
		{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					MessagingOptionsEditDialog.this.setVisible(false);
					MessagingOptionsEditDialog.this.dispose();
				}
		});
		
		final JButton saveButon = new JButton("Save & close");
		this.buttonPanel.add(saveButon);
		saveButon.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					final String amountToSend = MessagingOptionsEditDialog.this.amountTextField.getText();
					final String transactionFee = MessagingOptionsEditDialog.this.transactionFeeTextField.getText();
					
					if ((!MessagingOptionsEditDialog.this.verifyNumericField("amount to send", amountToSend)) ||
						(!MessagingOptionsEditDialog.this.verifyNumericField("transaction fee", transactionFee)))
					{
						return;
					}
					
					final MessagingOptions options = MessagingOptionsEditDialog.this.storage.getMessagingOptions();
					
					options.setAmountToSend(Double.parseDouble(amountToSend));
					options.setTransactionFee(Double.parseDouble(transactionFee));
					options.setAutomaticallyAddUsersIfNotExplicitlyImported(
						MessagingOptionsEditDialog.this.automaticallyAddUsers.isSelected());
					
					MessagingOptionsEditDialog.this.storage.updateMessagingOptions(options);
					
					MessagingOptionsEditDialog.this.setVisible(false);
					MessagingOptionsEditDialog.this.dispose();
				} catch (final Exception ex)
				{
					log.error("Unexpected error in editing own messaging identity!", ex);
					MessagingOptionsEditDialog.this.errorReporter.reportError(ex, false);
				}
			}
		});

		this.pack();
		this.setLocation(100, 100);
		this.setLocationRelativeTo(parentFrame);
	}

	
	private boolean verifyNumericField(final String name, final String value)
	{
		if (Util.stringIsEmpty(value))
		{
	        JOptionPane.showMessageDialog(
        		this.parentFrame,
        		"Field \"" + name + "\" is empty. It is mandatory. Please fill it.",
                "Mandatory data missing", JOptionPane.ERROR_MESSAGE);
	        return false;
		}
		
		try
		{
			final double dVal = Double.parseDouble(value);
			
			if (dVal < 0)
			{
		        JOptionPane.showMessageDialog(
		        	this.parentFrame,
		        	"Field \"" + name + "\" has a value that is negative. Please enter a positive number!",
		            "Field is negative", JOptionPane.ERROR_MESSAGE);
		        return false;
			}
		} catch (final NumberFormatException nfe)
		{
	        JOptionPane.showMessageDialog(
	        	this.parentFrame,
	        	"Field \"" + name + "\" has a value that is not numeric. Please enter a number!",
	            "Field is not numeric", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		
		return true;
	}
	
	
	private void addFormField(final JPanel detailsPanel, final String name, final JComponent field)
	{
		final JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		final JLabel tempLabel = new JLabel(name, JLabel.RIGHT);
		// TODO: hard sizing of labels may not scale!
		final int width = new JLabel("ZEN amount to send with every message:").getPreferredSize().width + 30;
		tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
		tempPanel.add(tempLabel);
		tempPanel.add(field);
		detailsPanel.add(tempPanel);
	}
	
}
