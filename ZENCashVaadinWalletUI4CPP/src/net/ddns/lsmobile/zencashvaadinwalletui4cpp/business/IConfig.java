package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.text.DecimalFormat;

public interface IConfig {
	
	//TODO LS: in config file
	
	public final static String ZEN_DIRECTORY = "C:\\Program Files (x86)\\ZENCashDesktopGUIWallet_0.74.2\\app";

	public final static String TRANSACTIONS_COLUMN_TYPE = "Type";
	public final static String TRANSACTIONS_COLUMN_DIRECTION = "Direction";
	public final static String TRANSACTIONS_COLUMN_CONFIRMED = "Confirmed?";
	public final static String TRANSACTIONS_COLUMN_AMOUNT = "Amount";
	public final static String TRANSACTIONS_COLUMN_DATE = "Date";
	public final static String TRANSACTIONS_COLUMN_DESTINATION_ADDRESS = "Destination Address";
	public final static String TRANSACTIONS_COLUMN_DESTINATION_TRANSACTION = "Transaction";
	
	public final static String ADDRESSES_COLUMN_BALANCE = "Balance";
	public final static String ADDRESSES_COLUMN_CONFIRMED = "Confirmed?";
	public final static String ADDRESSES_COLUMN_ADDRESS = "Address";
	
	// Format double numbers - else sometimes we get exponential notation 1E-4 ZEN
	//TODO LS
	public final static DecimalFormat decimalFormat = new DecimalFormat("########0.00######");
}
