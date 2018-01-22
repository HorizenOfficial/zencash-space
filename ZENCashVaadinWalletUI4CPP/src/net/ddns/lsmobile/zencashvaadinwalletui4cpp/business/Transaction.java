package net.ddns.lsmobile.zencashvaadinwalletui4cpp.business;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.OSUtil.OS_TYPE;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.ui.Servlet;

public class Transaction implements IConfig {

	public final static String COLUMN_TYPE = "Type";
	public final static String COLUMN_DIRECTION = "Direction";
	public final static String COLUMN_CONFIRMED = "Confirmed?";
	public final static String COLUMN_AMOUNT = "Amount";
	public final static String COLUMN_DATE = "Date";
	public final static String COLUMN_DESTINATION_ADDRESS = "Destination Address";
	public final static String COLUMN_DESTINATION_TRANSACTION = "Transaction";

	public final static String CONFIRMED = "\u2690";
	public final static String NOT_CONFIRMED = "\u2691";
	public final static String CONFIRMED_WINDOWS = " \u25B7";
	public final static String NOT_CONFIRMED_WINDOWS = " \u25B6";
	
	public static enum Type {PUBLIC, PRIVATE}
	public static enum Direction {RECEIVE, SEND, GENERATE, IMMATURE}
	
	protected Type type;
	protected Direction direction;
	protected Boolean isConfirmed;
	protected BigDecimal amount;
	protected Date date;
	protected String destinationAddress;
	protected String transaction;

	public Transaction() {
	}

	public Type getType() {
		return this.type;
	}

	public String getTypeAsString() {
		switch (this.type) {
		case PUBLIC:
			return "\u2606T (Public)";
		case PRIVATE:
			return "\u2605Z (Private)";
		default:
			return "N/A";
		}
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public String getDirectionAsString() {
		switch (this.direction) {
		case RECEIVE:
			return "\u21E8 IN";
		case SEND:
			return "\u21E6 OUT";
		case GENERATE:
			return "\u2692\u2699 MINED";
		case IMMATURE:
			return "\u2696 Immature";
		default:
			return "ERROR!";
		}
	}

	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	public void setDirection(final String rawDirection) {
		switch (rawDirection) {
		case "receive":
			this.direction = Direction.RECEIVE;
			break;
		case "send":
			this.direction = Direction.SEND;
			break;
		case "generate":
			this.direction = Direction.GENERATE;
			break;
		case "immature":
			this.direction = Direction.IMMATURE;
			break;
		default:
			this.direction = null;
		}

	}

	public Boolean getIsConfirmed() {
		return this.isConfirmed;
	}
	
	public String getIsConfirmedAsString() {
		// Confirmation symbols
		String confirmed    = CONFIRMED;
		String notConfirmed = NOT_CONFIRMED;
		
		// Windows does not support the flag symbol (Windows 7 by default)
		// TODO: isolate OS-specific symbol codes in a separate class
		final OS_TYPE os = OSUtil.getOSType();
		if (os == OS_TYPE.WINDOWS)
		{
			confirmed = CONFIRMED_WINDOWS;
			notConfirmed = NOT_CONFIRMED_WINDOWS;
		}


		return this.isConfirmed ? ("Yes " + confirmed) : ("No  " + notConfirmed);
	}

	public void setIsConfirmed(final Boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	public void setIsConfirmed(final String rawIsConfirmed) {
		try
		{
			this.isConfirmed = !rawIsConfirmed.trim().equals("0");
		} catch (final NumberFormatException nfe)
		{
			log.error("Error occurred while formatting confirmations: " + rawIsConfirmed +
					           " - " + nfe.getMessage() + "!");
			this.isConfirmed = null;
		}
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public void setAmount(final String rawAmount) {
		try {
			this.amount = BigDecimal.valueOf(Math.abs(Servlet.usNumberFormat.parse(rawAmount).doubleValue()));
		} catch (final ParseException nfe) {
			log.error("Error occurred while formatting amount: " + rawAmount + " - " + nfe.getMessage() + "!");
			this.amount = null;
		}
	}

	public Date getDate() {
		return this.date;
	}

	public String getDateAsString() {
		if (this.date != null) {
			return this.date.toLocaleString();
		}
		return "N/A";
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public void setDate(final String rawDate) {
		if (!rawDate.equals("N/A"))
		{
			this.date = new Date(Long.valueOf(rawDate).longValue() * 1000L);
		}
		else {
			this.date = null;
		}
	}

	public String getDestinationAddress() {
		return this.destinationAddress;
	}

	public String getDestinationAddressAsString() {
		String notListed = "\u26D4";
		
		final OS_TYPE os = OSUtil.getOSType();
		if (os == OS_TYPE.WINDOWS)
		{
			notListed = " \u25B6";
		}
		
		return this.destinationAddress != null ? this.destinationAddress : notListed + " (Z Address not listed by wallet!)";
	}

	public void setDestinationAddress(final String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getTransaction() {
		return this.transaction;
	}

	public String getTransactionAsString() {
		return "<a href=\"https://explorer.zensystem.io/tx/" +  this.transaction + "\" target=\"_blank\">" +  this.transaction + "</a>";
	}

	public void setTransaction(final String transaction) {
		this.transaction = transaction;
	}
}
