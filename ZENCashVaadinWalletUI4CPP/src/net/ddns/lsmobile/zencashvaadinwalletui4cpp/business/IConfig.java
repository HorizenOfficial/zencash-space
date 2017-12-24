package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface IConfig {

	final Logger log = LogManager.getLogger(IConfig.class);

	// TODO LS: in config file

	public final static String ZEN_BIN = "ZEN_BIN";

	public final static String ADDRESSES_COLUMN_BALANCE = "Balance";
	public final static String ADDRESSES_COLUMN_CONFIRMED = "Confirmed?";
	public final static String ADDRESSES_COLUMN_ADDRESS = "Address";

	public final static int MAXIMUM_FRACTION_DIGITS = 8;

	public final NumberFormat defaultNumberFormat = NumberFormat.getNumberInstance();
	public final NumberFormat usNumberFormat = java.text.NumberFormat.getNumberInstance(Locale.US);

}
