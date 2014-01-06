package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import scala.Option;
import scala.Some;
import services.UnixMD5Crypt;

@Entity
public class Admin extends Model {
	private static final long serialVersionUID = 2627475585121741565L;

	@Id
	private Long id;

	@Required
	@NonEmpty
	@Email
	private String email;

	@Required
	private String name;

	@Required(groups = {})
	private String password;

	private String emailConfirmToken;
	private String passwordChangeToken;
	@Temporal(TemporalType.TIMESTAMP)
	private Date passwordChangeTokenDate;

	private Boolean locked;
	private Boolean needPasswordChange;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	private Date loggedIn;
	@Temporal(TemporalType.TIMESTAMP)
	private Date changed;

	private String url;
	private String streetaddress1;
	private String streetaddress2;
	private String streetaddress3;
	private String state;
	private String country;
	private String telephone;

	@Transient
	private String pwdClear;
	@Transient
	private String pwdVerify;

	@MaxLength(512)
	private String adminRoles;

	@ManyToOne(fetch = FetchType.LAZY)
	private Publisher publisher;

	@Transient
	private List<Publisher> publishers = new ArrayList<Publisher>();

	public static Finder<Long, Admin> find = new Finder<Long, Admin>(
			Long.class, Admin.class);

	private static final String TOKEN_ALPHABET = "ABCDEFGHKLMNPQRSTUWXYZ23456789";

	public static Option<Admin> authenticate(String email, String password) {
		email = email.toLowerCase();
		for (final Admin admin : find.where().eq("email", email).findList()) {
			if (admin.checkPwd(password)) {
				return new Some<Admin>(admin);
			}
		}
		return Option.empty();
	}

	public static Option<Publisher> changePublisher(String publisherid,
			Admin admin) {
		final Long id = publisherid != null ? Long.parseLong(publisherid) : 0L;
		for (final Publisher publisher : Publisher.findByAdmin(admin)) {
			if (id.equals(publisher.id)) {
				admin.setPublisher(publisher);
				admin.save();
				publisher.setActive(true);
				return new Some<Publisher>(publisher);
			}
		}
		return Option.empty();
	}

	public static String createToken() {
		final int l = TOKEN_ALPHABET.length();
		final char[] c = TOKEN_ALPHABET.toCharArray();
		final Random random = new Random();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 40; i++) {
			sb.append(c[random.nextInt(l)]);
		}
		return sb.toString();
	}

	public static void delete(Long id) {
		final Admin admin = find.byId(id);
		admin.delete();
	}

	/**
	 * Retrieve all admins.
	 */
	public static List<Admin> findAll() {
		return find.all();
	}

	public static List<Admin> findByAdmin(Admin admin) {
		final List<Admin> admins = new ArrayList<Admin>();
		if (admin != null) {
			if (admin.isSysAdmin()) {
				admins.addAll(findAll());
			} else {
				admins.add(admin);
			}
		}
		for (final Admin c : admins) {
			final List<Publisher> p = Publisher.findByAdmin(c);
			if (p != null) {
				c.setPublishers(p);
			} else {
				c.setPublishers(Collections.<Publisher>emptyList());
			}
		}
		return admins;
	}

	/**
	 * Retrieve a Admin from email.
	 */
	public static Admin findByEmail(String email) {
		if (email != null && !email.isEmpty()) {
			return find.where().eq("email", email).findUnique();
		}
		return null;
	}

	public static Option<Admin> findByEmailOption(String email) {
		final Admin admin = findByEmail(email);
		return new Some<Admin>(admin);
	}

	public static Option<Admin> findById(Long id) {
		if (id != null) {
			final Admin admin = find.byId(id);
			if (admin != null) {
				return new Some<Admin>(admin);
			}
		}
		return Option.empty();
	}

	public static Option<Admin> findById(String id) {
		if (id != null && !id.isEmpty()) {
			return findById(Long.parseLong(id));
		}
		return Option.empty();
	}

	public static Option<Admin> findByToken(String id) {
		if (id != null && !id.isEmpty()) {
			for (final Admin admin : find.where().eq("emailConfirmToken", id)
					.findList()) {
				return new Some<Admin>(admin);
			}
			for (final Admin admin : find.where().eq("passwordChangeToken", id)
					.findList()) {
				return new Some<Admin>(admin);
			}
		}
		return Option.empty();
	}

	public static Admin forgotPassword(String email) {
		email = email.toLowerCase();
		for (final Admin admin : find.where().eq("email", email).findList()) {
			// TODO: send password change link
			return admin;
		}
		return null;
	}

	public static Admin fromMap(Map<String, Object> data) {
		final Admin admin = new Admin("New Admin", "");
		admin.updateFromMap(data);
		return admin;
	}

	public static boolean hasRole(String adminid, String role) {
		final Option<Admin> o = findById(adminid);
		final Admin admin = o.nonEmpty() ? o.get() : null;
		if (admin != null) {
			return admin.getRoles().contains(role);
		}
		return false;
	}

	public static Admin newAdmin(Admin admin) {
		if (admin != null && admin.isSysAdmin()) {
			final Admin newAdmin = new Admin("email@provider.com", "New Login",
					"");
			admin.created = new Date();
			admin.locked = true;
			admin.emailConfirmToken = createToken();
			admin.save();
			return newAdmin;
		}
		return null;
	}

	public static Admin updateBasic(Admin current, Admin admin) {
		if (current != null) {
			current.name = admin.name;
			current.email = admin.email;
			current.save();
		}
		return current;
	}

	public static Admin updateBasic(Long id, Admin admin) {
		return updateBasic(find.byId(id), admin);
	}

	public static Admin updatePassword(Admin current, Admin admin) {
		if (current != null) {
			if (admin.pwdClear != null && admin.pwdVerify != null
					&& admin.pwdClear.equals(admin.pwdVerify)) {
				final String salt = UnixMD5Crypt.generateSalt(4);
				admin.password = UnixMD5Crypt.crypt(admin.pwdClear, salt);
			}
			current.save();
		}
		return current;
	}

	public static Admin updatePassword(Long id, Admin admin) {
		return updatePassword(find.byId(id), admin);
	}

	public Admin() {
		created = new Date();
	}

	public Admin(String name, String email) {
		this(email, name, null);
	}

	public Admin(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
		adminRoles = "";
		created = new Date();
	}

	public boolean checkPwd(String password) {
		return UnixMD5Crypt.check(getPassword(), password);
	}

	public String getAdminRoles() {
		return adminRoles;
	}

	public Date getChanged() {
		return changed;
	}

	public String getCountry() {
		return country;
	}

	public Date getCreated() {
		return created;
	}

	public String getEmail() {
		return email;
	}

	public String getEmailConfirmToken() {
		return emailConfirmToken;
	}

	public Long getId() {
		return id;
	}

	public Boolean getLocked() {
		return locked;
	}

	public Date getLoggedIn() {
		return loggedIn;
	}

	public String getName() {
		return name;
	}

	public Boolean getNeedPasswordChange() {
		return needPasswordChange;
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordChangeToken() {
		return passwordChangeToken;
	}

	public Date getPasswordChangeTokenDate() {
		return passwordChangeTokenDate;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public List<Publisher> getPublishers() {
		return publishers != null ? publishers : new ArrayList<Publisher>();
	}

	public String getPwdClear() {
		return pwdClear;
	}

	public String getPwdVerify() {
		return pwdVerify;
	}

	public List<String> getRoles() {
		final List<String> roles = new ArrayList<String>();
		if (adminRoles != null) {
			for (final String role : adminRoles.split("[,]")) {
				roles.add(role);
			}
		}
		return roles;
	}

	public String getState() {
		return state;
	}

	public String getStreetaddress1() {
		return streetaddress1;
	}

	public String getStreetaddress2() {
		return streetaddress2;
	}

	public String getStreetaddress3() {
		return streetaddress3;
	}

	public String getTelephone() {
		return telephone;
	}

	public String getUrl() {
		return url;
	}

	public boolean is(String role) {
		return getRoles().contains(role);
	}

	public boolean isSysAdmin() {
		return getRoles().contains("sysadmin");
	}

	public void login(String session) {
	}

	public List<Message> remove() {
		return Collections.emptyList();
	}

	public void setAdminRoles(String adminRoles) {
		this.adminRoles = adminRoles;
	}

	public void setChanged(Date changed) {
		this.changed = changed;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEmailConfirmToken(String emailConfirmToken) {
		this.emailConfirmToken = emailConfirmToken;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public void setLoggedIn(Date loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNeedPasswordChange(Boolean needPasswordChange) {
		this.needPasswordChange = needPasswordChange;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPasswordChangeToken(String passwordChangeToken) {
		this.passwordChangeToken = passwordChangeToken;
	}

	public void setPasswordChangeTokenDate(Date passwordChangeTokenDate) {
		this.passwordChangeTokenDate = passwordChangeTokenDate;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public void setPublishers(List<Publisher> publishers) {
		this.publishers = publishers;
	}

	public void setPwdClear(String pwdClear) {
		this.pwdClear = pwdClear;
	}

	public void setPwdVerify(String pwdVerify) {
		this.pwdVerify = pwdVerify;
	}

	public void setRoles(List<String> roles) {
		if (roles != null) {
			adminRoles = StringUtils.join(roles, ",");
		}
		adminRoles = "";
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setStreetaddress1(String streetaddress1) {
		this.streetaddress1 = streetaddress1;
	}

	public void setStreetaddress2(String streetaddress2) {
		this.streetaddress2 = streetaddress2;
	}

	public void setStreetaddress3(String streetaddress3) {
		this.streetaddress3 = streetaddress3;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Admin(" + email + ")";
	}

	public Admin updateFromMap(Map<String, Object> data) {
		return this;
	}

	public List<Message> write() {
		save();
		update();
		return Collections.emptyList();
	}
}