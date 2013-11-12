package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

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
	public Long id;

	@Required
	@NonEmpty
	@Email
	public String email;

	@Required
	public String name;

	@Required(groups = {})
	public String password;

	public String emailConfirmToken;
	public String passwordChangeToken;
	@Temporal(TemporalType.TIMESTAMP)
	public Date passwordChangeTokenDate;

	public Boolean locked;
	public Boolean needPasswordChange;

	@Temporal(TemporalType.TIMESTAMP)
	public Date created;
	@Temporal(TemporalType.TIMESTAMP)
	public Date loggedIn;
	@Temporal(TemporalType.TIMESTAMP)
	public Date changed;

	public String url;
	public String streetaddress1;
	public String streetaddress2;
	public String streetaddress3;
	public String state;
	public String country;
	public String telephone;

	@Transient
	public String pwdClear;
	@Transient
	public String pwdVerify;

	@MaxLength(512)
	public String admin_roles;

	public Admin(String name, String email) {
		this(email, name, null);
	}

	public Admin(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.admin_roles = "";
	}

	public String getIdString() {
		return this.id != null ? Long.toString(this.id) : "";
	}

	public static Admin authenticate(String email, String password) {
        email = email.toLowerCase();
		for (final Admin admin : find.where().eq("email", email).findList()) {
			if (admin.checkPwd(password)) {
				return admin;
			}
		}
		return null;
	}

    public static Admin forgotPassword(String email) {
        email = email.toLowerCase();
		for (final Admin admin : find.where().eq("email", email).findList()) {
            // TODO: send password change link
			return admin;
		}
		return null;
	}

	public static Finder<Long, Admin> find = new Finder<Long, Admin>(
			Long.class, Admin.class);

	/**
	 * Retrieve all admins.
	 */
	public static List<Admin> findAll() {
		return find.all();
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

	public static Admin updateBasic(Long id, Admin admin) {
		return updateBasic(find.byId(id), admin);
	}

	public static Admin updateBasic(Admin current, Admin admin) {
		if (current != null) {
			current.name = admin.name;
			current.email = admin.email;
			current.save();
		}
		return current;
	}

	public static Admin updatePassword(Long id, Admin admin) {
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

	public static boolean hasRole(String adminid, String role) {
		final Admin admin = findById(adminid);
		if (admin != null) {
			return admin.getRoles().contains(role);
		}
		return false;
	}

	public static Admin findById(String id) {
		if (id != null && !id.isEmpty()) {
			return find.byId(Long.parseLong(id));
		}
		return null;
	}

	public static Admin findByToken(String id) {
		if (id != null && !id.isEmpty()) {
			return find.byId(Long.parseLong(id));
		}
		return null;
	}

	private static final String TOKEN_ALPHABET = "ABCDEFGHKLMNPQRSTUWXYZ23456789";

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

	public static List<Admin> findByAdmin(Admin admin) {
		final List<Admin> admins = new ArrayList<Admin>();
		if (admin != null) {
			if (admin.isSysAdmin()) {
				return findAll();
			}
			admins.add(admin);
		}
		return admins;
	}

	public static void delete(Long id) {
		final Admin admin = find.byId(id);
		admin.delete();
	}

	public static Option<Admin> findByEmailOption(String email) {
		final Admin admin = findByEmail(email);
		return new Some<Admin>(admin);
	}

	public boolean checkPwd(String password) {
		return UnixMD5Crypt.check(this.password, password);
	}

	public boolean isSysAdmin() {
		return getRoles().contains("sysadmin");
	}

	public List<String> getRoles() {
		final List<String> roles = new ArrayList<String>();
		if (this.admin_roles != null) {
			for (final String role : this.admin_roles.split("[,]")) {
				roles.add(role);
			}
		}
		return roles;
	}

	@Override
	public String toString() {
		return "Admin(" + email + ")";
	}
}