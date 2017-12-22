
package net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.entities.User;
import com.xdev.dal.JPADAO;

/**
 * Home object for domain model class User.
 * 
 * @see User
 */
public class UserDAO extends JPADAO<User, Integer> {
	public UserDAO() {
		super(User.class);
	}
}