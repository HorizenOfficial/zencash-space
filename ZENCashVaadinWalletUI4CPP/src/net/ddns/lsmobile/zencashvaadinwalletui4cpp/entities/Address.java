package net.ddns.lsmobile.zencashvaadinwalletui4cpp.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;

import com.xdev.dal.DAO;
import com.xdev.util.Caption;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import net.ddns.lsmobile.zencashvaadinwalletui4cpp.dal.AddressDAO;

/**
 * Address
 */
@DAO(daoClass = AddressDAO.class)
@Caption("{%address}")
@Entity
@Table(name = "addresses", catalog = "zenwallet", uniqueConstraints = @UniqueConstraint(columnNames = "address"))
public class Address implements java.io.Serializable {

	private int id;
	private User user1;
	private String address;

	public Address() {
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

	@Caption("User1")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user", columnDefinition = "INT")
	public User getUser1() {
		return this.user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	@Caption("Address")
	@Column(name = "address", unique = true, nullable = false, columnDefinition = "VARCHAR")
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
