package net.ddns.lsmobile.zencashvaadinwalletui4cpp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;

import com.xdev.dal.DAO;
import com.xdev.security.authentication.CredentialsUsernamePassword;
import com.xdev.util.Caption;
import javax.persistence.Id;
import javax.persistence.Table;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal.UserDAO;

/**
 * User
 */
@DAO(daoClass = UserDAO.class)
@Caption("{%username}")
@Entity
@Table(name = "users", catalog = "zenwallet")
public class User implements java.io.Serializable, CredentialsUsernamePassword {

	private int id;
	private String username;
	private byte[] password;
	private byte[] wallet;

	public User() {
	}

	@Caption("Id")
	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "id", unique = true, nullable = false, columnDefinition = "INT")
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Caption("Username")
	@Column(name = "username", nullable = false, columnDefinition = "VARCHAR")
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Caption("Password")
	@Column(name = "password", columnDefinition = "BLOB")
	public byte[] getPassword() {
		return this.password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	@Caption("Wallet")
	@Column(name = "wallet", columnDefinition = "BLOB")
	public byte[] getWallet() {
		return this.wallet;
	}

	public void setWallet(byte[] wallet) {
		this.wallet = wallet;
	}

	@Override
	public String username() {
		return this.getUsername();
	}

	@Override
	public byte[] password() {
		return this.getPassword();
	}

}
