package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

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

	@Required
	public String password;

	@MaxLength(512)
	public String admin_roles;

	public Admin(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.admin_roles = "";
	}

	public static Admin authenticate(String email, String password) {
		return find.where().eq("email", email).eq("password", password)
				.findUnique();
	}

	public static Finder<String, Admin> find = new Finder<String, Admin>(
			String.class, Admin.class);

	/**
	 * Retrieve all users.
	 */
	public static List<Admin> findAll() {
		return find.all();
	}

	/**
	 * Retrieve a User from email.
	 */
	public static Admin findByEmail(String email) {
		return find.where().eq("email", email).findUnique();
	}

	public String toString() {
		return "Admin(" + email + ")";
	}

}