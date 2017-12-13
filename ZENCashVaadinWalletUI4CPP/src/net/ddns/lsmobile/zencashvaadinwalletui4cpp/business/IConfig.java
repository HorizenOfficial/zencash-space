package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.text.DecimalFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface IConfig {
	
	final Logger log = LogManager.getLogger(IConfig.class);

	
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
	public final static String DECIMAL_FORMAT = "########0.00######";
	public final static DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
	
	public final static int MAXIMUM_FRACTION_DIGITS = 8;
}
