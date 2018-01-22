package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal.AddressDAO;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal.UserDAO;

public interface IConfig {

	final Logger log = LogManager.getLogger(IConfig.class);

	// TODO LS: in config file

	public final static String ZEN_BIN = "ZEN_BIN";
	
	public final static String AUTHENTICATION_RESULT = "AUTHENTICATION_RESULT";

	public final static String ADDRESSES_COLUMN_BALANCE = "Balance";
	public final static String ADDRESSES_COLUMN_CONFIRMED = "Confirmed?";
	public final static String ADDRESSES_COLUMN_ADDRESS = "Address";

	public final static Double DEFAULT_FEE = 0.0001;
	public final static int MAXIMUM_FRACTION_DIGITS = 8;

	public final AddressDAO addressDAO = new AddressDAO ();
	public final UserDAO userDAO = new UserDAO ();
	
	public final static String PLEASE_WAIT = "Please wait...";

}
