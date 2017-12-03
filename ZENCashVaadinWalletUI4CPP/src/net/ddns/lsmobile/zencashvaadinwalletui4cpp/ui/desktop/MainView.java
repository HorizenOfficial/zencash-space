
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.desktop;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaklinov.zcashui.Log;
import com.vaklinov.zcashui.OSUtil;
import com.vaklinov.zcashui.OSUtil.OS_TYPE;
import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevMenuBar;
import com.xdev.ui.XdevMenuBar.XdevMenuItem;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevView;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.Servlet;

public class MainView extends XdevView implements IConfig{

	/**
	 * 
	 */
	public MainView() {
		super();
		this.initUI();
		
		try {
			final String[][] transactions = getTransactionsDataFromWallet();

			final Grid gridTransactions = new Grid("Transactions:");
//			final HeaderRow headerWallets = gridTransactions.prependHeaderRow();

			// Formats
//			final DecimalFormat formatUsd = new DecimalFormat(UsdToHtmlConverter.FORMAT_USD);

			// Columns
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_TYPE, String.class).setRenderer(new HtmlRenderer())/*.setHeaderCaption("")*/;
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_DIRECTION, String.class).setRenderer(new HtmlRenderer());
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_CONFIRMED, String.class).setRenderer(new HtmlRenderer());
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_AMOUNT, String.class/*Double.class*/).setRenderer(new HtmlRenderer()/*NumberRenderer(formatUsd), new UsdToHtmlConverter()*/);
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_DATE, String.class/*Date.class*/).setRenderer(new HtmlRenderer/*DateRenderer*/());
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_DESTINATION_ADDRESS, String.class).setRenderer(new HtmlRenderer()).setSortable(false);
			gridTransactions.addColumn(TRANSACTIONS_COLUMN_DESTINATION_TRANSACTION, String.class).setRenderer(new HtmlRenderer()).setSortable(false)/*.setWidth(0)*/;
			
//			gridTransactions.setFrozenColumnCount(2);

			// Rows (Values)
			for (final String[] transactionsRow : transactions) {
					gridTransactions.addRow(transactionsRow);
			}

//			gridBalance.sort(Sort.by(COLUMN_SECURITY_DEGREE, SortDirection.ASCENDING)
//			          .then(COLUMN_SUM, SortDirection.DESCENDING));;

			gridTransactions.setSizeFull();
			this.panelGridTransactions.setContent(gridTransactions);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private String[][] getTransactionsDataFromWallet()
			throws WalletCallException, IOException, InterruptedException
		{
			final Servlet servlet = (Servlet) Servlet.getCurrent();
		
			// Get available public+private transactions and unify them.
			final String[][] publicTransactions = servlet.clientCaller.getWalletPublicTransactions();
			final String[][] zReceivedTransactions = servlet.clientCaller.getWalletZReceivedTransactions();

			final String[][] allTransactions = new String[publicTransactions.length + zReceivedTransactions.length][];

			int i  = 0;

			for (final String[] t : publicTransactions)
			{
				allTransactions[i++] = t;
			}

			for (final String[] t : zReceivedTransactions)
			{
				allTransactions[i++] = t;
			}
			
			// Sort transactions by date
			Arrays.sort(allTransactions, new Comparator<String[]>() {
				@Override
				public int compare(final String[] o1, final String[] o2)
				{
					Date d1 = new Date(0);
					if (!o1[4].equals("N/A"))
					{
						d1 = new Date(Long.valueOf(o1[4]).longValue() * 1000L);
					}

					Date d2 = new Date(0);
					if (!o2[4].equals("N/A"))
					{
						d2 = new Date(Long.valueOf(o2[4]).longValue() * 1000L);
					}

					if (d1.equals(d2))
					{
						return 0;
					} else
					{
						return d2.compareTo(d1);
					}
				}
			});
			
			
			// Confirmation symbols
			String confirmed    = "\u2690";
			String notConfirmed = "\u2691";
			
			// Windows does not support the flag symbol (Windows 7 by default)
			// TODO: isolate OS-specific symbol codes in a separate class
			final OS_TYPE os = OSUtil.getOSType();
			if (os == OS_TYPE.WINDOWS)
			{
				confirmed = " \u25B7";
				notConfirmed = " \u25B6";
			}

			final DecimalFormat df = new DecimalFormat("########0.00######");
			
			// Change the direction and date etc. attributes for presentation purposes
			for (final String[] trans : allTransactions)
			{
				// Direction
				if (trans[1].equals("receive"))
				{
					trans[1] = "\u21E8 IN";
				} else if (trans[1].equals("send"))
				{
					trans[1] = "\u21E6 OUT";
				} else if (trans[1].equals("generate"))
				{
					trans[1] = "\u2692\u2699 MINED";
				} else if (trans[1].equals("immature"))
				{
					trans[1] = "\u2696 Immature";
				};

				// Date
				if (!trans[4].equals("N/A"))
				{
					trans[4] = new Date(Long.valueOf(trans[4]).longValue() * 1000L).toLocaleString();
				}
				
				// Amount
				try
				{
					double amount = Double.valueOf(trans[3]);
					if (amount < 0d)
					{
						amount = -amount;
					}
					trans[3] = df.format(amount);
				} catch (final NumberFormatException nfe)
				{
					Log.error("Error occurred while formatting amount: " + trans[3] +
							           " - " + nfe.getMessage() + "!");
				}
				
				// Confirmed?
				try
				{
					final boolean isConfirmed = !trans[2].trim().equals("0");
					
					trans[2] = isConfirmed ? ("Yes " + confirmed) : ("No  " + notConfirmed);
				} catch (final NumberFormatException nfe)
				{
					Log.error("Error occurred while formatting confirmations: " + trans[2] +
							           " - " + nfe.getMessage() + "!");
				}
			}


			return allTransactions;
		}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.gridLayout = new XdevGridLayout();
		this.menuBar = new XdevMenuBar();
		this.menuItemMain = this.menuBar.addItem("Main", null);
		this.menuItemAbout = this.menuItemMain.addItem("About...", null);
		this.menuItemWallet = this.menuBar.addItem("Wallet", null);
		this.menuItemBackup = this.menuItemWallet.addItem("Backup...", null);
		this.menuItemEncrypt = this.menuItemWallet.addItem("Encrypt...", null);
		this.menuItemExportPrivateKeys = this.menuItemWallet.addItem("Export private keys...", null);
		this.menuItemImportPrivateKeys = this.menuItemWallet.addItem("Import private keys...", null);
		this.menuItemShowPrivateKey = this.menuItemWallet.addItem("Show private key...", null);
		this.menuItemImportOnePrivateKey = this.menuItemWallet.addItem("Import one private key...", null);
		this.menuItemMessaging = this.menuBar.addItem("Messaging", null);
		this.menuItemOwnIdentity = this.menuItemMessaging.addItem("Own identity...", null);
		this.menuItemExportOwnIdentity = this.menuItemMessaging.addItem("Export own identity...", null);
		this.menuItemAddMessagingGroup = this.menuItemMessaging.addItem("Add messaging group...", null);
		this.menuItemImportContactIdentity = this.menuItemMessaging.addItem("Import contact identity...", null);
		this.menuItemRemoveContact = this.menuItemMessaging.addItem("Remove contact...", null);
		this.menuItemOptions = this.menuItemMessaging.addItem("Options...", null);
		this.tabSheet = new XdevTabSheet();
		this.tabOverview = new XdevGridLayout();
		this.panelGridTransactions = new XdevPanel();
		this.tabOwnAddresses = new XdevGridLayout();
		this.tabSendCash = new XdevGridLayout();
		this.tabAddressBook = new XdevGridLayout();
		this.tabMessaging = new XdevGridLayout();
	
		this.menuItemEncrypt.setEnabled(false);
		this.tabSheet.setStyleName("framed");
	
		this.tabOverview.setColumns(1);
		this.tabOverview.setRows(1);
		this.panelGridTransactions.setSizeFull();
		this.tabOverview.addComponent(this.panelGridTransactions, 0, 0);
		this.tabOverview.setColumnExpandRatio(0, 10.0F);
		this.tabOverview.setRowExpandRatio(0, 10.0F);
		this.tabOverview.setSizeFull();
		this.tabSheet.addTab(this.tabOverview, "Overview", null);
		this.tabOwnAddresses.setSizeFull();
		this.tabSheet.addTab(this.tabOwnAddresses, "Own addresses", null);
		this.tabSendCash.setSizeFull();
		this.tabSheet.addTab(this.tabSendCash, "Send cash", null);
		this.tabAddressBook.setSizeFull();
		this.tabSheet.addTab(this.tabAddressBook, "Address book", null);
		this.tabMessaging.setSizeFull();
		this.tabSheet.addTab(this.tabMessaging, "Messaging", null);
		this.tabSheet.setSelectedTab(this.tabOverview);
		this.gridLayout.setColumns(1);
		this.gridLayout.setRows(2);
		this.menuBar.setWidth(100, Unit.PERCENTAGE);
		this.menuBar.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.menuBar, 0, 0);
		this.tabSheet.setSizeFull();
		this.gridLayout.addComponent(this.tabSheet, 0, 1);
		this.gridLayout.setColumnExpandRatio(0, 100.0F);
		this.gridLayout.setRowExpandRatio(1, 100.0F);
		this.gridLayout.setSizeFull();
		this.setContent(this.gridLayout);
		this.setSizeFull();
	} // </generated-code>

	// <generated-code name="variables">
	private XdevMenuBar menuBar;
	private XdevMenuItem menuItemMain, menuItemAbout, menuItemWallet, menuItemBackup, menuItemEncrypt,
			menuItemExportPrivateKeys, menuItemImportPrivateKeys, menuItemShowPrivateKey, menuItemImportOnePrivateKey,
			menuItemMessaging, menuItemOwnIdentity, menuItemExportOwnIdentity, menuItemAddMessagingGroup,
			menuItemImportContactIdentity, menuItemRemoveContact, menuItemOptions;
	private XdevTabSheet tabSheet;
	private XdevPanel panelGridTransactions;
	private XdevGridLayout gridLayout, tabOverview, tabOwnAddresses, tabSendCash, tabAddressBook, tabMessaging;
	// </generated-code>

}
