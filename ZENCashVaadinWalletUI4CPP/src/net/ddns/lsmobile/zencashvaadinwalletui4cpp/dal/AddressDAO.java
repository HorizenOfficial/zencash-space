
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal;

import com.xdev.dal.JPADAO;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.entities.Address;

/**
 * Home object for domain model class Address.
 * 
 * @see Address
 */
public class AddressDAO extends JPADAO<Address, Integer> {
	public AddressDAO() {
		super(Address.class);
	}
}